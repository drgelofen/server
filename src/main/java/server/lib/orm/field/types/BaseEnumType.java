package server.lib.orm.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;

import server.lib.orm.field.FieldType;
import server.lib.orm.field.SqlType;

/**
 * Base class for the enum classes to provide a utility method.
 * 
 * @author graywatson
 */
public abstract class BaseEnumType extends BaseDataType {

	protected BaseEnumType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	protected BaseEnumType(SqlType sqlType) {
		super(sqlType);
	}

	protected static Enum<?> enumVal(FieldType fieldType, Object val, Enum<?> enumVal, Enum<?> unknownEnumVal)
			throws SQLException {
		if (enumVal != null) {
			return enumVal;
		} else if (unknownEnumVal == null) {
			throw new SQLException("Cannot get enum value of '" + val + "' for field " + fieldType);
		} else {
			return unknownEnumVal;
		}
	}

	@Override
	public boolean isValidForField(Field field) {
		return field.getType().isEnum();
	}
}
