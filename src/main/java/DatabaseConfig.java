import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * Created by sumeet
 * on 24/1/17.
 */
public class DatabaseConfig {
    public final String databaseName;
    public final DatabaseType databaseType;
    public final String driver;
    public final String url;
    public final String user;
    public final String password;
    public final boolean usePool;
    public final PoolProperties poolProperties;

    public DatabaseConfig(String databaseName, DatabaseType databaseType, String user, String password, String driver, String url, PoolProperties poolProperties) {
        this.databaseName = databaseName;
        this.databaseType = databaseType;
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.poolProperties = poolProperties;
        this.usePool = Util.isSet(poolProperties);
    }

    public DatabaseConfig(String databaseName, DatabaseType databaseType, String user, String password, String driver, String url) {
        this(databaseName, databaseType, user, password, driver, url, null);
    }
}
