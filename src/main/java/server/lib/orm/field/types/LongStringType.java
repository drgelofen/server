package server.lib.orm.field.types;

import server.lib.orm.field.SqlType;

/**
 * Persists the {@link String} Java class but with more storage in the database.
 * 
 * @author graywatson
 */
public class LongStringType extends StringType {

	private static final LongStringType singleTon = new LongStringType();

	public static LongStringType getSingleton() {
		return singleTon;
	}

	private LongStringType() {
		super(SqlType.LONG_STRING);
	}

	/**
	 * Here for others to subclass.
	 */
	protected LongStringType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}

	@Override
	public int getDefaultWidth() {
		return 0;
	}

	@Override
	public Class<?> getPrimaryClass() {
		return String.class;
	}
}
