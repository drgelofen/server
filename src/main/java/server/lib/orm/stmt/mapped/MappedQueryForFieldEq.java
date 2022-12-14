package server.lib.orm.stmt.mapped;

import java.sql.SQLException;

import server.lib.orm.dao.Dao;
import server.lib.orm.dao.ObjectCache;
import server.lib.orm.db.DatabaseType;
import server.lib.orm.field.FieldType;
import server.lib.orm.support.DatabaseConnection;
import server.lib.orm.table.TableInfo;

/**
 * Mapped statement for querying for an object by a single field.
 * 
 * @author graywatson
 */
public class MappedQueryForFieldEq<T, ID> extends BaseMappedQuery<T, ID> {

	private final String label;

	protected MappedQueryForFieldEq(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, String statement,
			FieldType[] argFieldTypes, FieldType[] resultsFieldTypes, String label) {
		super(dao, tableInfo, statement, argFieldTypes, resultsFieldTypes);
		this.label = label;
	}

	/**
	 * Query for an object in the database which matches the id argument.
	 */
	public T execute(DatabaseConnection databaseConnection, ID id, ObjectCache objectCache) throws SQLException {
		if (objectCache != null) {
			T result = objectCache.get(clazz, id);
			if (result != null) {
				return result;
			}
		}
		Object[] args = new Object[] { convertIdToFieldObject(id) };
		// @SuppressWarnings("unchecked")
		Object result = databaseConnection.queryForOne(statement, args, argFieldTypes, this, objectCache);
		if (result == null) {
			logger.debug("{} using '{}' and {} args, got no results", label, statement, args.length);
		} else if (result == DatabaseConnection.MORE_THAN_ONE) {
			logger.error("{} using '{}' and {} args, got >1 results", label, statement, args.length);
			logArgs(args);
			throw new SQLException(label + " got more than 1 result: " + statement);
		} else {
			logger.debug("{} using '{}' and {} args, got 1 result", label, statement, args.length);
		}
		logArgs(args);
		@SuppressWarnings("unchecked")
		T castResult = (T) result;
		return castResult;
	}

	public static <T, ID> MappedQueryForFieldEq<T, ID> build(Dao<T, ID> dao, TableInfo<T, ID> tableInfo,
			FieldType idFieldType) throws SQLException {
		if (idFieldType == null) {
			idFieldType = tableInfo.getIdField();
			if (idFieldType == null) {
				throw new SQLException("Cannot query-for-id with " + tableInfo.getDataClass()
						+ " because it doesn't have an id field");
			}
		}
		DatabaseType databaseType = dao.getConnectionSource().getDatabaseType();
		String statement = buildStatement(databaseType, tableInfo, idFieldType);
		return new MappedQueryForFieldEq<T, ID>(dao, tableInfo, statement, new FieldType[] { idFieldType },
				tableInfo.getFieldTypes(), "query-for-id");
	}

	protected static <T, ID> String buildStatement(DatabaseType databaseType, TableInfo<T, ID> tableInfo,
			FieldType idFieldType) {
		// build the select statement by hand
		StringBuilder sb = new StringBuilder(64);
		appendTableName(databaseType, sb, "SELECT * FROM ", tableInfo);
		appendWhereFieldEq(databaseType, idFieldType, sb, null);
		return sb.toString();
	}

	private void logArgs(Object[] args) {
		if (args.length > 0) {
			// need to do the (Object) cast to force args to be a single object and not an array
			logger.trace("{} arguments: {}", label, (Object) args);
		}
	}
}
