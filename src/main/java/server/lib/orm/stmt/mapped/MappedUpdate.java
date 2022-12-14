package server.lib.orm.stmt.mapped;

import java.sql.SQLException;

import server.lib.orm.dao.Dao;
import server.lib.orm.dao.ObjectCache;
import server.lib.orm.db.DatabaseType;
import server.lib.orm.field.FieldType;
import server.lib.orm.misc.SqlExceptionUtil;
import server.lib.orm.support.DatabaseConnection;
import server.lib.orm.table.TableInfo;

/**
 * Mapped statement for updating an object.
 * 
 * @author graywatson
 */
public class MappedUpdate<T, ID> extends BaseMappedStatement<T, ID> {

	private final FieldType versionFieldType;
	private final int versionFieldTypeIndex;

	private MappedUpdate(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes,
			FieldType versionFieldType, int versionFieldTypeIndex) {
		super(dao, tableInfo, statement, argFieldTypes);
		this.versionFieldType = versionFieldType;
		this.versionFieldTypeIndex = versionFieldTypeIndex;
	}

	public static <T, ID> MappedUpdate<T, ID> build(Dao<T, ID> dao, TableInfo<T, ID> tableInfo) throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException(
					"Cannot update " + tableInfo.getDataClass() + " because it doesn't have an id field");
		}
		StringBuilder sb = new StringBuilder(64);
		DatabaseType databaseType = dao.getConnectionSource().getDatabaseType();
		appendTableName(databaseType, sb, "UPDATE ", tableInfo);
		boolean first = true;
		int argFieldC = 0;
		FieldType versionFieldType = null;
		int versionFieldTypeIndex = -1;
		// first we count up how many arguments we are going to have
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			if (isFieldUpdatable(fieldType, idField)) {
				if (fieldType.isVersion()) {
					versionFieldType = fieldType;
					versionFieldTypeIndex = argFieldC;
				}
				argFieldC++;
			}
		}
		// one more for where id = ?
		argFieldC++;
		if (versionFieldType != null) {
			// one more for the AND version = ?
			argFieldC++;
		}
		FieldType[] argFieldTypes = new FieldType[argFieldC];
		argFieldC = 0;
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			if (!isFieldUpdatable(fieldType, idField)) {
				continue;
			}
			if (first) {
				sb.append("SET ");
				first = false;
			} else {
				sb.append(", ");
			}
			appendFieldColumnName(databaseType, sb, fieldType, null);
			argFieldTypes[argFieldC++] = fieldType;
			sb.append("= ?");
		}
		sb.append(' ');
		appendWhereFieldEq(databaseType, idField, sb, null);
		argFieldTypes[argFieldC++] = idField;
		if (versionFieldType != null) {
			sb.append(" AND ");
			appendFieldColumnName(databaseType, sb, versionFieldType, null);
			sb.append("= ?");
			argFieldTypes[argFieldC++] = versionFieldType;
		}
		return new MappedUpdate<T, ID>(dao, tableInfo, sb.toString(), argFieldTypes, versionFieldType,
				versionFieldTypeIndex);
	}

	/**
	 * Update the object in the database.
	 */
	public int update(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		try {
			// there is always and id field as an argument so just return 0 lines updated
			if (argFieldTypes.length <= 1) {
				return 0;
			}
			Object[] args = getFieldObjects(data);
			Object newVersion = null;
			if (versionFieldType != null) {
				newVersion = versionFieldType.extractJavaFieldValue(data);
				newVersion = versionFieldType.moveToNextValue(newVersion);
				args[versionFieldTypeIndex] = versionFieldType.convertJavaFieldToSqlArgValue(newVersion);
			}
			int rowC = databaseConnection.update(statement, args, argFieldTypes);
			if (rowC > 0) {
				if (newVersion != null) {
					// if we have updated a row then update the version field in our object to the new value
					versionFieldType.assignField(connectionSource, data, newVersion, false, null);
				}
				if (objectCache != null) {
					// if we've changed something then see if we need to update our cache
					Object id = idField.extractJavaFieldValue(data);
					T cachedData = objectCache.get(clazz, id);
					if (cachedData != null && cachedData != data) {
						// copy each field from the updated data into the cached object
						for (FieldType fieldType : tableInfo.getFieldTypes()) {
							if (fieldType != idField) {
								fieldType.assignField(connectionSource, cachedData,
										fieldType.extractJavaFieldValue(data), false, objectCache);
							}
						}
					}
				}
			}
			logger.debug("update data with statement '{}' and {} args, changed {} rows", statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("update arguments: {}", (Object) args);
			}
			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run update stmt on object " + data + ": " + statement, e);
		}
	}

	private static boolean isFieldUpdatable(FieldType fieldType, FieldType idField) {
		if (fieldType == idField || fieldType.isForeignCollection() || fieldType.isReadOnly()) {
			return false;
		} else {
			return true;
		}
	}
}
