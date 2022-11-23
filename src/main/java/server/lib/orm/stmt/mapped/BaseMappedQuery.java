package server.lib.orm.stmt.mapped;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import server.lib.orm.dao.BaseForeignCollection;
import server.lib.orm.dao.Dao;
import server.lib.orm.dao.DaoManager;
import server.lib.orm.dao.ObjectCache;
import server.lib.orm.field.FieldType;
import server.lib.orm.stmt.GenericRowMapper;
import server.lib.orm.support.DatabaseResults;
import server.lib.orm.table.DatabaseTable;
import server.lib.orm.table.TableInfo;

/**
 * Abstract mapped statement for queries which handle the creating of a new object and the row mapping functionality.
 * 
 * @author graywatson
 */
//  TODO: MODIFIED
public abstract class BaseMappedQuery<T, ID> extends BaseMappedStatement<T, ID> implements GenericRowMapper<T> {

    protected final FieldType[] resultsFieldTypes;
    // cache of column names to results position
    private Map<String, Integer> columnPositions = null;
    private Object parent = null;
    private Object parentId = null;
    private boolean joined = false;
    private String joinId, joinClass;

    protected BaseMappedQuery(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes,
                              FieldType[] resultsFieldTypes) {
        super(dao, tableInfo, statement, argFieldTypes);
        this.resultsFieldTypes = resultsFieldTypes;
        this.joined = statement.toUpperCase().contains("INNER JOIN") || statement.toUpperCase().contains("LEFT JOIN");
        if (this.joined) {
            this.joinId = getJoinId(statement);
            this.joinClass = getJoinClass(statement);
        }
//        System.out.println("BITCH:" + statement);
    }

    private String getJoinClass(String statement) {
        StringBuilder builder = new StringBuilder();
        if (statement.contains("INNER JOIN")) {
            builder.append(statement, statement.toUpperCase().indexOf(" INNER JOIN ") + " INNER JOIN ".length(), statement.toUpperCase().indexOf(" ON "));
        } else {
            builder.append(statement, statement.toUpperCase().indexOf(" LEFT JOIN ") + " LEFT JOIN ".length(), statement.toUpperCase().indexOf(" ON "));
        }
        String data = builder.toString();
        if (data.contains(" AS Y")) {
            data = data.replace(" AS Y", "");
        }
        return data.trim().replace("\"", "");
    }

    private String getJoinId(String statement) {
        StringBuilder builder = new StringBuilder();
        builder = builder.append(statement, 0, statement.toUpperCase().indexOf(" WHERE")).reverse();
        StringBuilder idBuilder = new StringBuilder();
        for (char c : builder.toString().toCharArray()) {
            if (c == '.') {
                break;
            } else {
                idBuilder.append(c);
            }
        }
        return idBuilder.reverse().toString().trim().replace("\"", "");
    }

    @Override
    public T mapRow(DatabaseResults results) throws SQLException {
        Map<String, Integer> colPosMap;
        if (columnPositions == null) {
            colPosMap = new HashMap<String, Integer>();
        } else {
            colPosMap = columnPositions;
        }
        ObjectCache objectCache = results.getObjectCacheForRetrieve();
        if (objectCache != null) {
            Object id = idField.resultToJava(results, colPosMap);
            T cachedInstance = objectCache.get(clazz, id);
            if (cachedInstance != null) {
                // if we have a cached instance for this id then return it
                return cachedInstance;
            }
        }
        // create our instance
        T instance = dao.createObjectInstance();
        // populate its fields
        Object id = null;
        boolean foreignCollections = false;
        for (FieldType fieldType : resultsFieldTypes) {
            if (fieldType.isForeignCollection()) {
                foreignCollections = true;
            } else {
                Object val = fieldType.resultToJava(results, colPosMap);
                /*
                 * This is pretty subtle. We introduced multiple foreign fields to the same type which use the {@link
                 * ForeignCollectionField} foreignColumnName field. The bug that was created was that all the fields
                 * were then set with the parent class. Only the fields that have a matching id value should be set to
                 * the parent. We had to add the val.equals logic.
                 */
                if (val != null && parent != null && fieldType.getField().getType() == parent.getClass()
                        && val.equals(parentId)) {
                    fieldType.assignField(connectionSource, instance, parent, true, objectCache);
                } else {
                    fieldType.assignField(connectionSource, instance, val, false, objectCache);
                }
                if (fieldType.isId()) {
                    id = val;
                }
            }
        }
        if (foreignCollections) {
            // go back and initialize any foreign collections
            for (FieldType fieldType : resultsFieldTypes) {
                if (fieldType.isForeignCollection()) {
                    BaseForeignCollection<?, ?> collection = fieldType.buildForeignCollection(instance, id);
                    if (collection != null) {
                        fieldType.assignField(connectionSource, instance, collection, false, objectCache);
                    }
                }
            }
        }
        // if we have a cache and we have an id then add it to the cache
        objectCache = results.getObjectCacheForStore();
        if (objectCache != null && id != null) {
            objectCache.put(clazz, id, instance);
        }
        if (columnPositions == null) {
            columnPositions = colPosMap;
        }
        checkJoined(instance, results);
        return instance;
    }

    private void checkJoined(T instance, DatabaseResults results) {
        if (!joined) {
            return;
        }
        try {
            int counter = 0;
            HashMap<String, Integer> newMap = new HashMap<>();
            for (String key : results.getColumnNames()) {
                if (resultsFieldTypes.length <= counter) {
                    newMap.put(key, counter);
                }
                counter++;
            }
            if (newMap.size() > 0) {
                Field[] declaredFields = instance.getClass().getDeclaredFields();
                for (Field field : declaredFields) {
                    if (field.getType().getName().startsWith("server.model.")) {
                        Class<?> type = field.getType();
                        if (type.getAnnotations() != null) {
                            DatabaseTable annotation = type.getAnnotation(DatabaseTable.class);
                            if (annotation != null && annotation.tableName().equals(joinClass)) {
                                field.setAccessible(true);
                                Object join = field.get(instance);
                                Dao<?, ?> dao = DaoManager.createDao(connectionSource, type);
                                FieldType[] fieldTypes = dao.getTableInfo().getFieldTypes();
                                for (FieldType fieldType : fieldTypes) {
                                    if (!fieldType.isForeignCollection()) {
                                        Object val = fieldType.resultToJava(results, newMap);
                                        fieldType.assignField(connectionSource, join, val, false, null);
                                    }
                                }
                                field.setAccessible(false);
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * If we have a foreign collection object then this sets the value on the foreign object in the class.
     */
    public void setParentInformation(Object parent, Object parentId) {
        this.parent = parent;
        this.parentId = parentId;
    }
}
