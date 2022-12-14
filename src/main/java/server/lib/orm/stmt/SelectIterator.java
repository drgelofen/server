package server.lib.orm.stmt;

import java.io.IOException;
import java.sql.SQLException;

import server.lib.orm.dao.CloseableIterator;
import server.lib.orm.dao.Dao;
import server.lib.orm.dao.ObjectCache;
import server.lib.orm.logger.Logger;
import server.lib.orm.logger.LoggerFactory;
import server.lib.orm.misc.IOUtils;
import server.lib.orm.support.CompiledStatement;
import server.lib.orm.support.ConnectionSource;
import server.lib.orm.support.DatabaseConnection;
import server.lib.orm.support.DatabaseResults;

/**
 * Internal iterator so we can page through the class. This is used by the {@link Dao#iterator} methods.
 * 
 * @param <T>
 *            The class that the code will be operating on.
 * @param <ID>
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class SelectIterator<T, ID> implements CloseableIterator<T> {

	private final static Logger logger = LoggerFactory.getLogger(SelectIterator.class);

	private final Class<?> dataClass;
	private final Dao<T, ID> classDao;
	private final ConnectionSource connectionSource;
	private final DatabaseConnection connection;
	private final CompiledStatement compiledStmt;
	private final DatabaseResults results;
	private final GenericRowMapper<T> rowMapper;
	private boolean first = true;
	private boolean closed;
	private boolean alreadyMoved;
	private T last;
	private int rowC;

	/**
	 * If the statement parameter is null then this won't log information
	 */
	public SelectIterator(Class<?> dataClass, Dao<T, ID> classDao, GenericRowMapper<T> rowMapper,
			ConnectionSource connectionSource, DatabaseConnection connection, CompiledStatement compiledStmt,
			ObjectCache objectCache) throws SQLException {
		this.dataClass = dataClass;
		this.classDao = classDao;
		this.rowMapper = rowMapper;
		this.connectionSource = connectionSource;
		this.connection = connection;
		this.compiledStmt = compiledStmt;
		this.results = compiledStmt.runQuery(objectCache);
		logger.debug("starting iterator @{} for '{}'", hashCode(), compiledStmt);
	}

	/**
	 * Returns whether or not there are any remaining objects in the table. Can be called before next().
	 * 
	 * @throws SQLException
	 *             If there was a problem getting more results via SQL.
	 */
	public boolean hasNextThrow() throws SQLException {
		if (closed) {
			return false;
		}
		if (alreadyMoved) {
			// we do this so multiple hasNext() calls can be made, result would be true or closed is true
			return true;
		}
		boolean result;
		if (first) {
			first = false;
			result = results.first();
		} else {
			result = results.next();
		}
		if (!result) {
			IOUtils.closeThrowSqlException(this, "iterator");
		}
		alreadyMoved = true;
		return result;
	}

	/**
	 * Returns whether or not there are any remaining objects in the table. Can be called before next().
	 * 
	 * @throws IllegalStateException
	 *             If there was a problem getting more results via SQL.
	 */
	@Override
	public boolean hasNext() {
		try {
			return hasNextThrow();
		} catch (SQLException e) {
			last = null;
			closeQuietly();
			// unfortunately, can't propagate back the SQLException
			throw new IllegalStateException("Errors getting more results of " + dataClass, e);
		}
	}

	@Override
	public T first() throws SQLException {
		if (closed) {
			return null;
		}
		first = false;
		if (results.first()) {
			return getCurrent();
		} else {
			return null;
		}
	}

	@Override
	public T previous() throws SQLException {
		if (closed) {
			return null;
		}
		first = false;
		if (results.previous()) {
			return getCurrent();
		} else {
			return null;
		}
	}

	@Override
	public T current() throws SQLException {
		if (closed) {
			return null;
		}
		if (first) {
			return first();
		} else {
			return getCurrent();
		}
	}

	@Override
	public T nextThrow() throws SQLException {
		if (closed) {
			return null;
		}
		if (!alreadyMoved) {
			boolean hasResult;
			if (first) {
				first = false;
				hasResult = results.first();
			} else {
				hasResult = results.next();
			}
			// move forward
			if (!hasResult) {
				first = false;
				return null;
			}
		}
		first = false;
		return getCurrent();
	}

	/**
	 * Returns the next object in the table.
	 * 
	 * @throws IllegalStateException
	 *             If there was a problem extracting the object from SQL.
	 */
	@Override
	public T next() {
		SQLException sqlException = null;
		try {
			T result = nextThrow();
			if (result != null) {
				return result;
			}
		} catch (SQLException e) {
			sqlException = e;
		}
		// we have to throw if there is no next or on a SQLException
		last = null;
		closeQuietly();
		throw new IllegalStateException("Could not get next result for " + dataClass, sqlException);
	}

	@Override
	public T moveRelative(int offset) throws SQLException {
		if (closed) {
			return null;
		}
		first = false;
		if (results.moveRelative(offset)) {
			return getCurrent();
		} else {
			return null;
		}
	}

	@Override
	public T moveAbsolute(int position) throws SQLException {
		if (closed) {
			return null;
		}
		first = false;
		if (results.moveAbsolute(position)) {
			return getCurrent();
		} else {
			return null;
		}
	}

	/**
	 * Removes the last object returned by next() by calling delete on the dao associated with the object.
	 * 
	 * @throws IllegalStateException
	 *             If there was no previous next() call.
	 * @throws SQLException
	 *             If the delete failed.
	 */
	public void removeThrow() throws SQLException {
		if (last == null) {
			throw new IllegalStateException(
					"No last " + dataClass + " object to remove. Must be called after a call to next.");
		}
		if (classDao == null) {
			// we may never be able to get here since it should only be null for queryForAll methods
			throw new IllegalStateException("Cannot remove " + dataClass + " object because classDao not initialized");
		}
		try {
			classDao.delete(last);
		} finally {
			// if we've try to delete it, clear the last marker
			last = null;
		}
	}

	/**
	 * Removes the last object returned by next() by calling delete on the dao associated with the object.
	 * 
	 * @throws IllegalStateException
	 *             If there was no previous next() call or if delete() throws a SQLException (set as the cause).
	 */
	@Override
	public void remove() {
		try {
			removeThrow();
		} catch (SQLException e) {
			closeQuietly();
			// unfortunately, can't propagate back the SQLException
			throw new IllegalStateException("Could not delete " + dataClass + " object " + last, e);
		}
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			compiledStmt.close();
			closed = true;
			last = null;
			logger.debug("closed iterator @{} after {} rows", hashCode(), rowC);
			try {
				connectionSource.releaseConnection(connection);
			} catch (SQLException e) {
				throw new IOException("could not release connection", e);
			}
		}
	}

	@Override
	public void closeQuietly() {
		IOUtils.closeQuietly(this);
	}

	@Override
	public DatabaseResults getRawResults() {
		return results;
	}

	@Override
	public void moveToNext() {
		last = null;
		first = false;
		alreadyMoved = false;
	}

	private T getCurrent() throws SQLException {
		last = rowMapper.mapRow(results);
		alreadyMoved = false;
		rowC++;
		return last;
	}
}
