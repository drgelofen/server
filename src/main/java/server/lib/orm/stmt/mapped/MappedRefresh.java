package server.lib.orm.stmt.mapped;

import java.sql.SQLException;

import server.lib.orm.dao.Dao;
import server.lib.orm.dao.ObjectCache;
import server.lib.orm.db.DatabaseType;
import server.lib.orm.field.FieldType;
import server.lib.orm.support.DatabaseConnection;
import server.lib.orm.table.TableInfo;

/**
 * Mapped statement for refreshing the fields in an object.
 * 
 * @author graywatson
 */
public class MappedRefresh<T, ID> extends MappedQueryForFieldEq<T, ID> {

	private MappedRefresh(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes,
			FieldType[] resultFieldTypes) {
		super(dao, tableInfo, statement, argFieldTypes, resultFieldTypes, "refresh");
	}

	/**
	 * Execute our refresh query statement and then update all of the fields in data with the fields from the result.
	 * 
	 * @return 1 if we found the object in the table by id or 0 if not.
	 */
	public int executeRefresh(DatabaseConnection databaseConnection, T data, ObjectCache objectCache)
			throws SQLException {
		@SuppressWarnings("unchecked")
		ID id = (ID) idField.extractJavaFieldValue(data);
		// we don't care about the cache here
		T result = super.execute(databaseConnection, id, null);
		if (result == null) {
			return 0;
		}
		// copy each field from the result into the passed in object
		for (FieldType fieldType : resultsFieldTypes) {
			if (fieldType != idField) {
				fieldType.assignField(connectionSource, data, fieldType.extractJavaFieldValue(result), false,
						objectCache);
			}
		}
		return 1;
	}

	public static <T, ID> MappedRefresh<T, ID> build(Dao<T, ID> dao, TableInfo<T, ID> tableInfo) throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException(
					"Cannot refresh " + tableInfo.getDataClass() + " because it doesn't have an id field");
		}
		DatabaseType databaseType = dao.getConnectionSource().getDatabaseType();
		String statement = buildStatement(databaseType, tableInfo, idField);
		return new MappedRefresh<T, ID>(dao, tableInfo, statement, new FieldType[] { tableInfo.getIdField() },
				tableInfo.getFieldTypes());
	}
}
