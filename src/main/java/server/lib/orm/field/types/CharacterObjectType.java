package server.lib.orm.field.types;

import java.sql.SQLException;

import server.lib.orm.field.FieldType;
import server.lib.orm.field.SqlType;
import server.lib.orm.support.DatabaseResults;

/**
 * Type that persists a Character object.
 * 
 * @author graywatson
 */
public class CharacterObjectType extends BaseDataType {

	private static final CharacterObjectType singleTon = new CharacterObjectType();

	public static CharacterObjectType getSingleton() {
		return singleTon;
	}

	private CharacterObjectType() {
		super(SqlType.CHAR, new Class<?>[] { Character.class });
	}

	protected CharacterObjectType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		if (defaultStr.length() != 1) {
			throw new SQLException("Problems with field " + fieldType + ", default string to long for Character: '"
					+ defaultStr + "'");
		}
		return (Character) defaultStr.charAt(0);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return (Character) results.getChar(columnPos);
	}
}
