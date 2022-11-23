package server.lib.orm.support;

import java.sql.SQLException;

/**
 * The holder of a generated key so we can return the value of generated keys from update methods. Used by the
 * {@link DatabaseConnection#insert(String, Object[], server.lib.orm.field.FieldType[], GeneratedKeyHolder)} method.
 * 
 * @author graywatson
 */
public interface GeneratedKeyHolder {

	/**
	 * Return the name of the generated column we are interested in.
	 */
	public String getColumnName();

	/**
	 * Add the key number on the key holder. May be called multiple times.
	 */
	public void addKey(Number key) throws SQLException;
}
