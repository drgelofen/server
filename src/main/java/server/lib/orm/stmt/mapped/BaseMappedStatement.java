package server.lib.orm.stmt.mapped;

import java.sql.SQLException;
import java.util.List;

import server.lib.orm.dao.Dao;
import server.lib.orm.db.DatabaseType;
import server.lib.orm.field.FieldType;
import server.lib.orm.logger.Logger;
import server.lib.orm.logger.LoggerFactory;
import server.lib.orm.support.ConnectionSource;
import server.lib.orm.table.TableInfo;

/**
 * Abstract mapped statement which has common statements used by the subclasses.
 * 
 * @author graywatson
 */
public abstract class BaseMappedStatement<T, ID> {

	protected static Logger logger = LoggerFactory.getLogger(BaseMappedStatement.class);

	protected final Dao<T, ID> dao;
	protected final ConnectionSource connectionSource;
	protected final TableInfo<T, ID> tableInfo;
	protected final Class<T> clazz;
	protected final FieldType idField;
	protected final String statement;
	protected final FieldType[] argFieldTypes;

	protected BaseMappedStatement(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, String statement,
			FieldType[] argFieldTypes) {
		this.dao = dao;
		this.connectionSource = dao.getConnectionSource();
		this.tableInfo = tableInfo;
		this.clazz = tableInfo.getDataClass();
		this.idField = tableInfo.getIdField();
		this.statement = statement;
		this.argFieldTypes = argFieldTypes;
	}

	/**
	 * Return the array of field objects pulled from the data object.
	 */
	protected Object[] getFieldObjects(Object data) throws SQLException {
		Object[] objects = new Object[argFieldTypes.length];
		for (int i = 0; i < argFieldTypes.length; i++) {
			FieldType fieldType = argFieldTypes[i];
			if (fieldType.isAllowGeneratedIdInsert()) {
				objects[i] = fieldType.getFieldValueIfNotDefault(data);
			} else {
				objects[i] = fieldType.extractJavaFieldToSqlArgValue(data);
			}
			if (objects[i] == null) {
				// NOTE: the default value could be null as well
				objects[i] = fieldType.getDefaultValue();
			}
		}
		return objects;
	}

	/**
	 * Return a field object converted from an id.
	 */
	protected Object convertIdToFieldObject(ID id) throws SQLException {
		return idField.convertJavaFieldToSqlArgValue(id);
	}

	static void appendWhereFieldEq(DatabaseType databaseType, FieldType fieldType, StringBuilder sb,
			List<FieldType> fieldTypeList) {
		sb.append("WHERE ");
		appendFieldColumnName(databaseType, sb, fieldType, fieldTypeList);
		sb.append("= ?");
	}


	static void appendTableName(DatabaseType databaseType, StringBuilder sb, String prefix, String tableName) {
		if (prefix != null) {
			sb.append(prefix);
		}
		databaseType.appendEscapedEntityName(sb, tableName);
		sb.append(' ');
	}

	static void appendTableName(DatabaseType databaseType, StringBuilder sb, String prefix, TableInfo<?, ?> tableInfo) {
		if (prefix != null) {
			sb.append(prefix);
		}
		if (tableInfo.getSchemaName() != null && tableInfo.getSchemaName().length() > 0){
			databaseType.appendEscapedEntityName(sb, tableInfo.getSchemaName());
			sb.append('.');
		}
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(' ');
	}

	static void appendFieldColumnName(DatabaseType databaseType, StringBuilder sb, FieldType fieldType,
			List<FieldType> fieldTypeList) {
		databaseType.appendEscapedEntityName(sb, fieldType.getColumnName());
		if (fieldTypeList != null) {
			fieldTypeList.add(fieldType);
		}
		sb.append(' ');
	}

	@Override
	public String toString() {
		return statement;
	}
}
