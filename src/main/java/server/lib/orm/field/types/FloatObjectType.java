package server.lib.orm.field.types;

import java.sql.SQLException;

import server.lib.orm.field.FieldType;
import server.lib.orm.field.SqlType;
import server.lib.orm.support.DatabaseResults;

/**
 * Type that persists a boolean primitive.
 * 
 * @author graywatson
 */
public class FloatObjectType extends BaseDataType {

	private static final FloatObjectType singleTon = new FloatObjectType();

	public static FloatObjectType getSingleton() {
		return singleTon;
	}

	private FloatObjectType() {
		super(SqlType.FLOAT, new Class<?>[] { Float.class });
	}

	protected FloatObjectType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return (Float) results.getFloat(columnPos);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
		return Float.parseFloat(defaultStr);
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}
}
