package server.lib.orm.stmt.query;

import java.sql.SQLException;
import java.util.List;

import server.lib.orm.db.DatabaseType;
import server.lib.orm.field.FieldType;
import server.lib.orm.stmt.ArgumentHolder;
import server.lib.orm.stmt.Where;

/**
 * Internal class handling the SQL 'IS NULL' comparison query part. Used by {@link Where#isNull}.
 * 
 * @author graywatson
 */
public class IsNull extends BaseComparison {

	public IsNull(String columnName, FieldType fieldType) throws SQLException {
		super(columnName, fieldType, null, false);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("IS NULL ");
	}

	@Override
	public void appendValue(DatabaseType databaseType, StringBuilder sb, List<ArgumentHolder> argList) {
		// there is no value
	}
}
