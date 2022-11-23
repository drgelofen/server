package server.lib.orm.field.types;

import java.sql.SQLException;

import server.lib.orm.field.FieldType;
import server.lib.orm.field.SqlType;
import server.lib.orm.support.DatabaseResults;

/**
 * Type that persists a Boolean object.
 * 
 * @author graywatson
 */
public class BooleanObjectType extends BaseDataType {

	private static final BooleanObjectType singleTon = new BooleanObjectType();

	public static BooleanObjectType getSingleton() {
		return singleTon;
	}

	private BooleanObjectType() {
		super(SqlType.BOOLEAN, new Class<?>[] { Boolean.class });
	}

	protected BooleanObjectType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	protected BooleanObjectType(SqlType sqlType) {
		super(sqlType);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
		return Boolean.parseBoolean(defaultStr);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return (Boolean) results.getBoolean(columnPos);
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}
}
