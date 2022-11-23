package server.lib.orm.stmt.query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import server.lib.orm.db.DatabaseType;
import server.lib.orm.field.FieldType;
import server.lib.orm.stmt.ArgumentHolder;
import server.lib.orm.stmt.Where;

/**
 * Internal class handling the SQL 'in' query part. Used by {@link Where#in}.
 * 
 * @author graywatson
 */
//  TODO: MODIFIED
public class In extends BaseComparison {

	private Iterable<?> objects;
	private final boolean in;

	public In(String columnName, FieldType fieldType, Iterable<?> objects, boolean in) throws SQLException {
		super(columnName, fieldType, null, true);
		this.objects = wrap(objects);
		this.in = in;
	}

	public In(String columnName, FieldType fieldType, Object[] objects, boolean in) throws SQLException {
		super(columnName, fieldType, null, true);
		// grrrr, Object[] should be Iterable
		this.objects = wrap(Arrays.asList(objects));
		this.in = in;
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		if (in) {
			sb.append("IN ");
		} else {
			sb.append("NOT IN ");
		}
	}

	@Override
	public void appendValue(DatabaseType databaseType, StringBuilder sb, List<ArgumentHolder> columnArgList)
			throws SQLException {
		sb.append('(');
		boolean first = true;
		for (Object value : objects) {
			if (value == null) {
				throw new IllegalArgumentException("one of the IN values for '" + columnName + "' is null");
			}
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			// for each of our arguments, add it to the output
			super.appendArgOrValue(databaseType, fieldType, sb, columnArgList, value);
			// cut off a trailing space if there is one
			int len = sb.length();
			if (len > 0 && sb.charAt(len - 1) == ' ') {
				sb.setLength(len - 1);
			}
		}
		sb.append(") ");
	}
}
