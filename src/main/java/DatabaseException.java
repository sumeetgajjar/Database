/**
 * Created by sumeet
 * on 24/1/17.
 */
public class DatabaseException extends Exception {

    private final String databaseName;
    private final String query;

    public DatabaseException(String databaseName, String query, Throwable cause) {
        super(cause);
        this.databaseName = databaseName;
        this.query = query;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getQuery() {
        return query;
    }
}
