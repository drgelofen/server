package server.lib.orm.field;

import server.lib.orm.support.DatabaseResults;

import java.sql.SQLException;

/**
 * Base class for field-converters.
 * 
 * @author graywatson
 */
public abstract class BaseFieldConverter implements FieldConverter {

	/**
	 * @throws SQLException
	 *             If there are problems with the conversion.
	 */
	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
		// noop pass-thru
		return javaObject;
	}

	@Override
	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		Object value = resultToSqlArg(fieldType, results, columnPos);
		if (value == null) {
			return null;
		} else {
			return sqlArgToJava(fieldType, value, columnPos);
		}
	}

	/**
	 * @throws SQLException
	 *             If there are problems with the conversion.
	 */
	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
		// noop pass-thru
		return sqlArg;
	}

	@Override
	public boolean isStreamType() {
		return false;
	}

	/**
	 * @See {@link FieldConverter#makeConfigObject(FieldType)}.
	 *
	 * @throws SQLException
	 *             If there are problems creating the config object. Needed for subclasses.
	 */
	@Override
	public Object makeConfigObject(FieldType fieldType) throws SQLException {
		return null;
	}
}
