package server.lib.orm.stmt.mapped;

import java.sql.SQLException;

import server.lib.orm.dao.Dao;
import server.lib.orm.field.FieldType;
import server.lib.orm.field.SqlType;
import server.lib.orm.logger.Log.Level;
import server.lib.orm.misc.IOUtils;
import server.lib.orm.stmt.ArgumentHolder;
import server.lib.orm.stmt.PreparedDelete;
import server.lib.orm.stmt.PreparedQuery;
import server.lib.orm.stmt.PreparedUpdate;
import server.lib.orm.stmt.StatementBuilder;
import server.lib.orm.stmt.StatementBuilder.StatementType;
import server.lib.orm.support.CompiledStatement;
import server.lib.orm.support.DatabaseConnection;
import server.lib.orm.table.TableInfo;

/**
 * Mapped statement used by the {@link StatementBuilder#prepareStatement(Long, boolean)} method.
 * 
 * @author graywatson
 */
public class MappedPreparedStmt<T, ID> extends BaseMappedQuery<T, ID>
		implements PreparedQuery<T>, PreparedDelete<T>, PreparedUpdate<T> {

	private final ArgumentHolder[] argHolders;
	private final Long limit;
	private final StatementType type;
	private final boolean cacheStore;

	public MappedPreparedStmt(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes,
			FieldType[] resultFieldTypes, ArgumentHolder[] argHolders, Long limit, StatementType type,
			boolean cacheStore) {
		super(dao, tableInfo, statement, argFieldTypes, resultFieldTypes);
		this.argHolders = argHolders;
		// this is an Integer because it may be null
		this.limit = limit;
		this.type = type;
		this.cacheStore = cacheStore;
	}

	@Override
	public CompiledStatement compile(DatabaseConnection databaseConnection, StatementType type) throws SQLException {
		return compile(databaseConnection, type, DatabaseConnection.DEFAULT_RESULT_FLAGS);
	}

	@Override
	public CompiledStatement compile(DatabaseConnection databaseConnection, StatementType type, int resultFlags)
			throws SQLException {
		if (this.type != type) {
			throw new SQLException("Could not compile this " + this.type + " statement since the caller is expecting a "
					+ type + " statement.  Check your QueryBuilder methods.");
		}
		CompiledStatement stmt =
				databaseConnection.compileStatement(statement, type, argFieldTypes, resultFlags, cacheStore);
		// this may return null if the stmt had to be closed
		return assignStatementArguments(stmt);
	}

	@Override
	public String getStatement() {
		return statement;
	}

	@Override
	public StatementType getType() {
		return type;
	}

	@Override
	public void setArgumentHolderValue(int index, Object value) throws SQLException {
		if (index < 0) {
			throw new SQLException("argument holder index " + index + " must be >= 0");
		}
		if (argHolders.length <= index) {
			throw new SQLException("argument holder index " + index + " is not valid, only " + argHolders.length
					+ " in statement (index starts at 0)");
		}
		argHolders[index].setValue(value);
	}

	@Override
	public int getNumArgs() {
		if (argHolders == null) {
			return 0;
		} else {
			return argHolders.length;
		}
	}

	/**
	 * Assign arguments to the statement.
	 * 
	 * @return The statement passed in or null if it had to be closed on error.
	 */
	private CompiledStatement assignStatementArguments(CompiledStatement stmt) throws SQLException {
		boolean ok = false;
		try {
			if (limit != null) {
				// we use this if SQL statement LIMITs are not supported by this database type
				stmt.setMaxRows(limit.intValue());
			}
			// set any arguments if we are logging our object
			Object[] argValues = null;
			if (logger.isLevelEnabled(Level.TRACE) && argHolders.length > 0) {
				argValues = new Object[argHolders.length];
			}
			for (int i = 0; i < argHolders.length; i++) {
				Object argValue = argHolders[i].getSqlArgValue();
				FieldType fieldType = argFieldTypes[i];
				SqlType sqlType;
				if (fieldType == null) {
					sqlType = argHolders[i].getSqlType();
				} else {
					sqlType = fieldType.getSqlType();
				}
				stmt.setObject(i, argValue, sqlType);
				if (argValues != null) {
					argValues[i] = argValue;
				}
			}
			logger.debug("prepared statement '{}' with {} args", statement, argHolders.length);
			if (argValues != null) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("prepared statement arguments: {}", (Object) argValues);
			}
			ok = true;
			return stmt;
		} finally {
			if (!ok) {
				IOUtils.closeThrowSqlException(stmt, "statement");
			}
		}
	}
}
