package server.lib.orm.stmt;

import java.sql.SQLException;

import server.lib.orm.dao.Dao;
import server.lib.orm.dao.RawRowMapper;
import server.lib.orm.field.FieldType;
import server.lib.orm.table.TableInfo;

/**
 * Default row mapper when you are using the {@link Dao#queryRaw(String, RawRowMapper, String...)}.
 * 
 * @author graywatson
 */
public class RawRowMapperImpl<T, ID> implements RawRowMapper<T> {

	private final Dao<T, ID> dao;
	private final TableInfo<T, ID> tableInfo;

	public RawRowMapperImpl(Dao<T, ID> dao) {
		this.dao = dao;
		this.tableInfo = dao.getTableInfo();
	}

	@Override
	public T mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
		// create our object
		T rowObj = dao.createObjectInstance();
		for (int i = 0; i < columnNames.length; i++) {
			// sanity check, prolly will never happen but let's be careful out there
			if (i >= resultColumns.length) {
				continue;
			}
			// run through and convert each field
			FieldType fieldType = tableInfo.getFieldTypeByColumnName(columnNames[i]);
			Object fieldObj = fieldType.convertStringToJavaField(resultColumns[i], i);
			// assign it to the row object
			fieldType.assignField(dao.getConnectionSource(), rowObj, fieldObj, false, null);
		}
		return rowObj;
	}
}
