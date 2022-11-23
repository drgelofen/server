package server.lib.orm.field.types;

import java.sql.SQLException;

import server.lib.orm.field.FieldType;
import server.lib.orm.field.SqlType;
import server.lib.orm.support.DatabaseResults;

/**
 * Type that persists a Double object.
 * 
 * @author graywatson
 */
public class DoubleObjectType extends BaseDataType {

	private static final DoubleObjectType singleTon = new DoubleObjectType();

	public static DoubleObjectType getSingleton() {
		return singleTon;
	}

	private DoubleObjectType() {
		super(SqlType.DOUBLE, new Class<?>[] { Double.class });
	}

	protected DoubleObjectType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
		return Double.parseDouble(defaultStr);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return (Double) results.getDouble(columnPos);
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}
}
