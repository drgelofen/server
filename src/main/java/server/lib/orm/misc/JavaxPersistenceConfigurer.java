package server.lib.orm.misc;

import java.lang.reflect.Field;
import java.sql.SQLException;

import server.lib.orm.db.DatabaseType;
import server.lib.orm.field.DatabaseFieldConfig;

/**
 * Interface that allows us to load and process javax.persistance annotations only if available.
 * 
 * @author graywatson
 */
public interface JavaxPersistenceConfigurer {

	/**
	 * Create and return a field config from the javax.persistence annotations associated with the field argument or
	 * null if no annotations present.
	 */
	public DatabaseFieldConfig createFieldConfig(DatabaseType databaseType, Field field) throws SQLException;

	/**
	 * Return the javax.persistence.Entity annotation name for the class argument or null if none or if there was no
	 * entity name.
	 */
	public String getEntityName(Class<?> clazz);
}
