package server.lib.orm.field.types;

import server.lib.orm.field.SqlType;

/**
 * Type that persists a float primitive.
 * 
 * @author graywatson
 */
public class FloatType extends FloatObjectType {

	private static final FloatType singleTon = new FloatType();

	public static FloatType getSingleton() {
		return singleTon;
	}

	private FloatType() {
		super(SqlType.FLOAT, new Class<?>[] { float.class });
	}

	/**
	 * Here for others to subclass.
	 */
	protected FloatType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}
}
