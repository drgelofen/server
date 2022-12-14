package server.lib.orm.stmt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import server.lib.orm.dao.CloseableIterator;
import server.lib.orm.dao.GenericRawResults;
import server.lib.orm.dao.ObjectCache;
import server.lib.orm.misc.IOUtils;
import server.lib.orm.support.CompiledStatement;
import server.lib.orm.support.ConnectionSource;
import server.lib.orm.support.DatabaseConnection;

/**
 * Handler for our raw results objects which does the conversion for various different results: String[], Object[], and
 * user defined T.
 * 
 * @author graywatson
 */
public class RawResultsImpl<T> implements GenericRawResults<T> {

	private SelectIterator<T, Void> iterator;
	private final String[] columnNames;

	public RawResultsImpl(ConnectionSource connectionSource, DatabaseConnection connection, Class<?> clazz,
			CompiledStatement compiledStmt, GenericRowMapper<T> rowMapper, ObjectCache objectCache)
			throws SQLException {
		iterator = new SelectIterator<T, Void>(clazz, null, rowMapper, connectionSource, connection, compiledStmt,
				objectCache);
		/*
		 * NOTE: we _have_ to get these here before the results object is closed if there are no results
		 */
		this.columnNames = iterator.getRawResults().getColumnNames();
	}

	@Override
	public int getNumberColumns() {
		return columnNames.length;
	}

	@Override
	public String[] getColumnNames() {
		return columnNames;
	}

	@Override
	public List<T> getResults() throws SQLException {
		List<T> results = new ArrayList<T>();
		try {
			while (iterator.hasNext()) {
				results.add(iterator.next());
			}
			return results;
		} finally {
			IOUtils.closeThrowSqlException(this, "raw results iterator");
		}
	}

	@Override
	public T getFirstResult() throws SQLException {
		try {
			if (iterator.hasNextThrow()) {
				return iterator.nextThrow();
			} else {
				return null;
			}
		} finally {
			IOUtils.closeThrowSqlException(this, "raw results iterator");
		}
	}

	@Override
	public CloseableIterator<T> iterator() {
		return iterator;
	}

	@Override
	public CloseableIterator<T> closeableIterator() {
		return iterator;
	}

	@Override
	public void close() throws IOException {
		if (iterator != null) {
			iterator.close();
			iterator = null;
		}
	}
}
