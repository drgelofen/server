package server.lib.orm.stmt.query;

import java.sql.SQLException;
import java.util.List;

import server.lib.orm.db.DatabaseType;
import server.lib.orm.stmt.ArgumentHolder;
import server.lib.orm.stmt.QueryBuilder.InternalQueryBuilderWrapper;
import server.lib.orm.stmt.Where;

/**
 * Internal class handling the SQL 'EXISTS' query part. Used by {@link Where#exists}.
 * 
 * @author graywatson
 */
public class Exists implements Clause {

	private final InternalQueryBuilderWrapper subQueryBuilder;

	public Exists(InternalQueryBuilderWrapper subQueryBuilder) {
		this.subQueryBuilder = subQueryBuilder;
	}

	@Override
	public void appendSql(DatabaseType databaseType, String tableName, StringBuilder sb, List<ArgumentHolder> argList,
			Clause outer) throws SQLException {
		sb.append("EXISTS (");
		subQueryBuilder.appendStatementString(sb, argList);
		// cut off a trailing space if there is one
		int len = sb.length();
		if (len > 0 && sb.charAt(len - 1) == ' ') {
			sb.setLength(len - 1);
		}
		sb.append(") ");
	}
}
