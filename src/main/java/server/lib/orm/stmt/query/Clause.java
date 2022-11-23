package server.lib.orm.stmt.query;

import java.sql.SQLException;
import java.util.List;

import server.lib.orm.db.DatabaseType;
import server.lib.orm.stmt.ArgumentHolder;

/**
 * Internal marker class for query clauses.
 * 
 * @author graywatson
 */
public interface Clause {

	/**
	 * Add to the string-builder the appropriate SQL for this clause.
	 * 
	 * @param tableName
	 *            Name of the table to prepend to any column names or null to be ignored.
	 * @param outer
	 *            Outer clause used for query generation optimization.  May be null.
	 */
	public void appendSql(DatabaseType databaseType, String tableName, StringBuilder sb, List<ArgumentHolder> argList,
                          Clause outer) throws SQLException;
}
