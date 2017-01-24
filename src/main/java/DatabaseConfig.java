import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * Created by sumeet
 * on 24/1/17.
 */
public class DatabaseConfig {
    public final String databaseName;
    public final String driver;
    public final String url;
    public final String user;
    public final String password;
    public final boolean usePool;
    public final PoolProperties poolProperties;

    public DatabaseConfig(String databaseName, String driver, String url, String user, String password, PoolProperties poolProperties) {
        this.databaseName = databaseName;
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.poolProperties = poolProperties;
        this.usePool = Util.isSet(poolProperties);
    }

    public DatabaseConfig(String databaseName, String driver, String url, String user, String password) {
        this(databaseName, driver, url, user, password, null);
    }
}
