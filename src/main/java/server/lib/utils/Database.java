package server.lib.utils;

import server.lib.orm.db.DatabaseType;
import server.lib.orm.jdbc.JdbcPooledConnectionSource;

public class Database extends JdbcPooledConnectionSource {

    public Database(String url, DatabaseType databaseType) throws Throwable {
        super(url, databaseType);
    }
}
