import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Types;
import java.util.List;

/**
 * Created by sumeet
 * on 25/4/17.
 */
public class DatabaseTest extends Database {

    private static final PoolProperties POOL_PROPERTIES = new PoolProperties() {{
        setTestOnBorrow(true);
        setTestOnReturn(true);

    }};
    private static final DatabaseConfig MYSQL_DATABASE_CONFIG = new DatabaseConfig("TEST_DATABASE", DatabaseTypeImpl.MYSQL, "test_user", "test_password", "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/TEST_DATABASE");
    private static final DatabaseConfig MYSQL_POOLED_DATABASE_CONFIG = new DatabaseConfig("TEST_DATABASE", DatabaseTypeImpl.MYSQL, "test_user", "test_password", "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/TEST_DATABASE", POOL_PROPERTIES);
    private static final DatabaseConfig MSSQL_DATABASE_CONFIG = new DatabaseConfig("TEST_DATABASE", DatabaseTypeImpl.MSSQL, "test_user", "test_password", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://localhost;databaseName=TEST_DATABASE");
    private static final DatabaseConfig MSSQL_POOLED_DATABASE_CONFIG = new DatabaseConfig("TEST_DATABASE", DatabaseTypeImpl.MSSQL, "test_user", "test_password", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://localhost;databaseName=KEYWORD_MASTER", POOL_PROPERTIES);


    public DatabaseTest(DatabaseConfig databaseConfig) {
        super(databaseConfig);
    }


    public static void testSingleDirectConnection(DatabaseTest database) throws Exception {
        StoredProcedureCall<String> storedProcedureCall = new StoredProcedureCall<>("sp_server_info", resultSet -> (resultSet.getString("attribute_name")));
        List<String> values = database.executeQuery(storedProcedureCall);
        System.out.println(values);

    }

    public static void testPooledConnection(DatabaseTest database) throws Exception {
        List<String> v = database.executeQuery(connection -> connection.prepareStatement("SELECT @@VERSION as v"), resultSet -> resultSet.getString("v"));
        System.out.println(v);
    }

    public static void testRawQuery(DatabaseTest database) throws Exception {
        List<String> v = database.executeQuery(connection -> connection.prepareStatement("SELECT @@VERSION as v"), resultSet -> resultSet.getString("v"));
        System.out.println(v);
    }

    public static void testExecuteUpdate(DatabaseTest database) throws Exception {
        StoredProcedureCall<Integer> storedProcedureCall = new StoredProcedureCall<>("insert_user");
        storedProcedureCall.addParameter("name", "test_user", Types.VARCHAR);
        int affectedRows = database.executeUpdate(storedProcedureCall);
        System.out.println(affectedRows);
    }

    @Override
    protected String getLogPrefix() {
        return "Media_Vala_Aywa_Che_Dhoom_Dhadaka_laywa_Che|";
    }

    public static void main(String[] args) {
        try {
            DatabaseTest mysqlDB = new DatabaseTest(MYSQL_DATABASE_CONFIG);
            DatabaseTest mysqlPooledDB = new DatabaseTest(MYSQL_POOLED_DATABASE_CONFIG);

            DatabaseTest mssqlDB = new DatabaseTest(MSSQL_DATABASE_CONFIG);
            DatabaseTest mssqlPooledDB = new DatabaseTest(MSSQL_POOLED_DATABASE_CONFIG);

            testSingleDirectConnection(mssqlPooledDB);
            testPooledConnection(mssqlDB);
            testRawQuery(mssqlDB);
            testExecuteUpdate(mssqlDB);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
