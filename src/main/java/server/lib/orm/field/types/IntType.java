package server.lib.orm.field.types;

import server.lib.orm.field.SqlType;

/**
 * Type that persists a integer primitive.
 * 
 * @author graywatson
 */
public class IntType extends IntegerObjectType {

	private static final IntType singleTon = new IntType();

	public static IntType getSingleton() {
		return singleTon;
	}

	private IntType() {
		super(SqlType.INTEGER, new Class<?>[] { int.class });
	}

	/**
	 * Here for others to subclass.
	 */
	protected IntType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}
}
