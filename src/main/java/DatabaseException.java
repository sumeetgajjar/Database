/**
 * Created by sumeet
 * on 24/1/17.
 */
public class DatabaseException extends Exception {

    private final DatabaseConfig databaseConfig;
    private final String query;

    public DatabaseException(DatabaseConfig databaseConfig, String query, Throwable cause) {
        super(cause);
        this.databaseConfig = databaseConfig;
        this.query = query;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public String getQuery() {
        return query;
    }
}