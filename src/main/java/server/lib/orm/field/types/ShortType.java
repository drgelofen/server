package server.lib.orm.field.types;

import server.lib.orm.field.SqlType;

/**
 * Type that persists a short primitive.
 * 
 * @author graywatson
 */
public class ShortType extends ShortObjectType {

	private static final ShortType singleTon = new ShortType();

	public static ShortType getSingleton() {
		return singleTon;
	}

	private ShortType() {
		super(SqlType.SHORT, new Class<?>[] { short.class });
	}

	/**
	 * Here for others to subclass.
	 */
	protected ShortType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}
}
