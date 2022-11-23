package server.lib.orm.table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import server.lib.orm.dao.Dao;
import server.lib.orm.dao.DaoManager;
import server.lib.orm.field.FieldType;
import server.lib.orm.db.DatabaseType;
import server.lib.orm.logger.Logger;
import server.lib.orm.logger.LoggerFactory;
import server.lib.orm.misc.IOUtils;
import server.lib.orm.misc.SqlExceptionUtil;
import server.lib.orm.stmt.StatementBuilder.StatementType;
import server.lib.orm.support.CompiledStatement;
import server.lib.orm.support.ConnectionSource;
import server.lib.orm.support.DatabaseConnection;
import server.lib.orm.support.DatabaseResults;

/**
 * Couple utility methods for the creating, dropping, and maintenance of schemas.
 */
public class SchemaUtils {

	private static Logger logger = LoggerFactory.getLogger(SchemaUtils.class);
	private static final FieldType[] noFieldTypes = new FieldType[0];

	/**
	 * For static methods only.
	 */
	private SchemaUtils() {
	}

	/**
	 * Issue the database statements to create the schema associated with a class.
	 *
	 * @param connectionSource
	 *            Associated connection source.
	 * @param dataClass
	 *            The class for which a schema will be created.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int createSchema(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
		Dao<T, ?> dao = DaoManager.createDao(connectionSource, dataClass);
		return doCreateSchema(connectionSource, dao.getTableInfo().getSchemaName(), false);
	}

	/**
	 * Issue the database statements to create the schema associated with a table configuration.
	 *
	 * @param dao
	 *            Associated dao.
	 * @return The number of statements executed to do so.
	 */
	public static int createSchema(Dao<?, ?> dao) throws SQLException {
		return doCreateSchema(dao.getConnectionSource(), dao.getTableInfo().getSchemaName(), false);
	}

	/**
	 * Create a schema if it does not already exist. This is not supported by all databases.
	 */
	public static <T> int createSchemaIfNotExists(ConnectionSource connectionSource, Class<T> dataClass)
			throws SQLException {
		Dao<T, ?> dao = DaoManager.createDao(connectionSource, dataClass);
		return doCreateSchema(dao.getConnectionSource(), dao.getTableInfo().getSchemaName(), true);
	}

	/**
	 * Issue the database statements to create the schema associated with a schema configuration.
	 *
	 * @param connectionSource
	 *            connectionSource Associated connection source.
	 * @param schemaName
	 *            schema name
	 * @return The number of statements executed to do so.
	 */
	public static <T> int createSchema(ConnectionSource connectionSource, String schemaName) throws SQLException {
		return doCreateSchema(connectionSource, schemaName, false);
	}

	/**
	 * Create a schema if it does not already exist. This is not supported by all databases.
	 */
	public static <T> int createSchemaIfNotExists(ConnectionSource connectionSource, String schemaName)
			throws SQLException {
		return doCreateSchema(connectionSource, schemaName, true);
	}

	/**
	 * Return an list of SQL statements that need to be run to create a schema. To do the work of creating, you should
	 * call {@link #createSchema}.
	 *
	 * @param databaseType
	 *            The type of database which will be executing the create schema statements.
	 * @param schemaName
	 *            Schema Name.
	 * @return A list of schema create statements.
	 */
	public static <T> List<String> getCreateSchemaStatements(DatabaseType databaseType, String schemaName) {
		List<String> statementList = new ArrayList<String>();
		addCreateSchemaStatements(databaseType, schemaName, statementList, statementList, false, false);
		return statementList;
	}

	/**
	 * Issue the database statements to drop the schema associated with a class.
	 *
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and is unrecoverable.
	 * </p>
	 *
	 * @param connectionSource
	 *            Associated connection source.
	 * @param dataClass
	 *            The class for which a schema will be dropped.
	 * @param ignoreErrors
	 *            If set to true then try each statement regardless of {@link SQLException} thrown previously.
	 * @return The number of statements executed to do so.
	 */
	public static <T, ID> int dropSchema(ConnectionSource connectionSource, Class<T> dataClass, boolean ignoreErrors)
			throws SQLException {
		Dao<T, ID> dao = DaoManager.createDao(connectionSource, dataClass);
		return dropSchema(dao.getConnectionSource(), dao.getTableInfo().getSchemaName(), ignoreErrors);
	}

	/**
	 * Issue the database statements to drop the schema associated with a schema configuration.
	 *
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and is unrecoverable.
	 * </p>
	 *
	 * @param connectionSource
	 *            Associated connection source.
	 * @param schemaName
	 *            schema name
	 * @param ignoreErrors
	 *            If set to true then try each statement regardless of {@link SQLException} thrown previously.
	 * @return The number of statements executed to do so.
	 */
	public static <T, ID> int dropSchema(ConnectionSource connectionSource, String schemaName, boolean ignoreErrors)
			throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		return doDropSchema(databaseType, connectionSource, schemaName, ignoreErrors);
	}

	private static <T, ID> int doDropSchema(DatabaseType databaseType, ConnectionSource connectionSource,
			String schemaName, boolean ignoreErrors) throws SQLException {
		List<String> statements = new ArrayList<String>();
		addDropSchemaStatements(databaseType, schemaName, statements, true);
		DatabaseConnection connection = connectionSource.getReadWriteConnection(schemaName);
		try {
			return doStatements(connection, "drop", statements, ignoreErrors,
					databaseType.isCreateSchemaReturnsNegative(), false);
		} finally {
			connectionSource.releaseConnection(connection);
		}
	}

	/**
	 * Generate and return the list of statements to drop a database schema.
	 */
	private static <T, ID> void addDropSchemaStatements(DatabaseType databaseType, String schemaName,
			List<String> statements, boolean logDetails) {
		StringBuilder sb = new StringBuilder(64);
		if (logDetails) {
			logger.info("dropping schema '{}'", schemaName);
		}
		sb.append("DROP SCHEMA ");
		databaseType.appendEscapedEntityName(sb, schemaName);
		sb.append(' ');
		statements.add(sb.toString());
	}

	private static <T, ID> int doCreateSchema(ConnectionSource connectionSource, String schemaName, boolean ifNotExists)
			throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		List<String> statements = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		addCreateSchemaStatements(databaseType, schemaName, statements, queriesAfter, ifNotExists, true);
		DatabaseConnection connection = connectionSource.getReadWriteConnection(schemaName);
		try {
			int stmtC = doStatements(connection, "create", statements, false,
					databaseType.isCreateSchemaReturnsNegative(), databaseType.isCreateSchemaReturnsZero());
			stmtC += doCreateTestQueries(connection, databaseType, queriesAfter);
			return stmtC;
		} finally {
			connectionSource.releaseConnection(connection);
		}
	}

	private static int doStatements(DatabaseConnection connection, String label, Collection<String> statements,
			boolean ignoreErrors, boolean returnsNegative, boolean expectingZero) throws SQLException {
		int stmtC = 0;
		for (String statement : statements) {
			int rowC = 0;
			CompiledStatement compiledStmt = null;
			try {
				compiledStmt = connection.compileStatement(statement, StatementType.EXECUTE, noFieldTypes,
						DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
				rowC = compiledStmt.runExecute();
				logger.info("executed {} schema statement changed {} rows: {}", label, rowC, statement);
			} catch (SQLException e) {
				if (ignoreErrors) {
					logger.info("ignoring {} error '{}' for statement: {}", label, e, statement);
				} else {
					throw SqlExceptionUtil.create("SQL statement failed: " + statement, e);
				}
			} finally {
				IOUtils.closeThrowSqlException(compiledStmt, "compiled statement");
			}
			// sanity check
			if (rowC < 0) {
				if (!returnsNegative) {
					throw new SQLException(
							"SQL statement " + statement + " updated " + rowC + " rows, we were expecting >= 0");
				}
			} else if (rowC > 0 && expectingZero) {
				throw new SQLException("SQL statement updated " + rowC + " rows, we were expecting == 0: " + statement);
			}
			stmtC++;
		}
		return stmtC;
	}

	private static int doCreateTestQueries(DatabaseConnection connection, DatabaseType databaseType,
			List<String> queriesAfter) throws SQLException {
		int stmtC = 0;
		// now execute any test queries which test the newly created schema
		for (String query : queriesAfter) {
			CompiledStatement compiledStmt = null;
			try {
				compiledStmt = connection.compileStatement(query, StatementType.SELECT, noFieldTypes,
						DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
				// we don't care about an object cache here
				DatabaseResults results = compiledStmt.runQuery(null);
				int rowC = 0;
				// count the results
				for (boolean isThereMore = results.first(); isThereMore; isThereMore = results.next()) {
					rowC++;
				}
				logger.info("executing create schema after-query got {} results: {}", rowC, query);
			} catch (SQLException e) {
				// we do this to make sure that the statement is in the exception
				throw SqlExceptionUtil.create("executing create schema after-query failed: " + query, e);
			} finally {
				// result set is closed by the statement being closed
				IOUtils.closeThrowSqlException(compiledStmt, "compiled statement");
			}
			stmtC++;
		}
		return stmtC;
	}

	/**
	 * Generate and return the list of statements to create a database schema and any associated features.
	 */
	private static <T, ID> void addCreateSchemaStatements(DatabaseType databaseType, String schemaName,
			List<String> statements, List<String> queriesAfter, boolean ifNotExists, boolean logDetails) {
		StringBuilder sb = new StringBuilder(256);
		if (logDetails) {
			logger.info("creating schema '{}'", schemaName);
		}
		sb.append("CREATE SCHEMA ");
		if (ifNotExists && databaseType.isCreateIfNotExistsSupported()) {
			sb.append("IF NOT EXISTS ");
		}
		databaseType.appendEscapedEntityName(sb, schemaName);
		databaseType.appendCreateSchemaSuffix(sb);
		statements.add(sb.toString());
	}
}
