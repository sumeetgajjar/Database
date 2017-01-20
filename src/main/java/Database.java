import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by sumeet
 * on 20/1/17.
 */
public abstract class Database {
    private final String databaseName;
    private final String driver;
    private final String url;
    private final String user;
    private final String password;

    private final boolean usePool;
    private final static Map<String, DataSource> dataSourceMap = new HashMap<>();
    private final Logger log;

    public Database(String databaseName, PoolProperties poolProperties) {
        this.log = getLogger();
        this.databaseName = databaseName;
        this.driver = SysProperties.getProperty(databaseName + "_DRIVER");
        this.url = SysProperties.getProperty(databaseName + "_URL");
        this.user = SysProperties.getProperty(databaseName + "_USERNAME");
        this.password = SysProperties.getProperty(databaseName + "_PASSWORD");

        if (Util.isSet(poolProperties)) {
            usePool = true;
            poolProperties.setUrl(url);
            poolProperties.setDriverClassName(driver);
            poolProperties.setUsername(user);
            poolProperties.setPassword(password);
            instantiatePool(poolProperties);
        } else {
            usePool = false;
        }
    }

    public Database(String databaseName) {
        this(databaseName, null);
    }

    protected abstract Logger getLogger();

    protected void instantiatePool(PoolProperties poolProperties) {
        DataSource dataSource = dataSourceMap.getOrDefault(databaseName, null);
        if (dataSource == null) {
            synchronized (Database.class) {
                dataSource = dataSourceMap.getOrDefault(databaseName, null);
                if (dataSource == null) {
                    log.info("INITIALIZING_DATASOURCE");
                    dataSource = new DataSource(poolProperties);
                    log.info("DATASOURCE_INITIALIZED");
                    dataSourceMap.put(databaseName, dataSource);
                }
            }
        }
    }

    protected Connection getConnection() throws Exception {
        long startTime = System.currentTimeMillis();
        Connection connection;
        if (usePool) {
            connection = dataSourceMap.get(databaseName).getConnection();
        } else {
            connection = getSingleDirectConnection();
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        log.info("DB_CONNECT_TIME|" + databaseName, timeTaken);
        return connection;
    }

    private Connection getSingleDirectConnection() throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        return DriverManager.getConnection(url, user, password);
    }

    protected String getProcedureCallString(String storedProcedureName, List<Parameter> parameters) {
        List<String> paramList = parameters.stream().map(parameter -> parameter.getName() + " := ?").collect(Collectors.toList());
        String parameterPlaceHolders = Util.implode(",", paramList);
        String procedureCallString = String.format("{call %s (%s) }", storedProcedureName, parameterPlaceHolders);
        return procedureCallString;
    }

    protected PreparedStatement getPreparedStatement(Connection connection, String storedProcedureName, List<Parameter> parameters) throws Exception {
        PreparedStatement preparedStatement = connection.prepareStatement(getProcedureCallString(storedProcedureName, parameters));
        preparedStatement.setEscapeProcessing(true);
        int index = 0;
        for (Parameter parameter : parameters) {
            preparedStatement.setObject(++index, parameter.getValue(), parameter.getType());
        }
        return preparedStatement;
    }

    protected <T> List<T> executeQuery(StoredProcedureCall<T> storedProcedureCall) throws Exception {
        long startTime = System.currentTimeMillis();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<T> rows = null;

        String storedProcedureName = storedProcedureCall.getStoredProcedureName();
        try {
            connection = getConnection();
            preparedStatement = getPreparedStatement(connection, storedProcedureName, storedProcedureCall.getParameters());
            resultSet = preparedStatement.executeQuery();

            Function<ResultSet, T> rowMapper = storedProcedureCall.getRowMapper();
            rows = new LinkedList<>();

            while (resultSet.next()) {
                T row = rowMapper.apply(resultSet);
                rows.add(row);
            }

        } catch (Exception e) {
            log.error(storedProcedureName + "_FAILURE", e);
            throw e;
        } finally {
            closeDatabaseConnection(resultSet, preparedStatement, connection);
            long timeTaken = System.currentTimeMillis() - startTime;
            log.info("SP_TIME|" + storedProcedureName + "|" + timeTaken);
        }
        return rows;
    }

    protected <T> int executeUpdate(StoredProcedureCall<T> storedProcedureCall) throws Exception {
        long startTime = System.currentTimeMillis();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        int affectedRows = -1;

        String storedProcedureName = storedProcedureCall.getStoredProcedureName();
        try {
            connection = getConnection();
            preparedStatement = getPreparedStatement(connection, storedProcedureName, storedProcedureCall.getParameters());
            affectedRows = preparedStatement.executeUpdate();

        } catch (Exception e) {
            log.error(storedProcedureName + "_FAILURE", e);
            throw e;
        } finally {
            closeDatabaseConnection(null, preparedStatement, connection);
            long timeTaken = System.currentTimeMillis() - startTime;
            log.info("SP_TIME|" + storedProcedureName + "|" + timeTaken);
        }
        return affectedRows;
    }

    protected void closeDatabaseConnection(ResultSet resultSet, PreparedStatement statement, Connection connection) {
        if (resultSet != null) try {
            resultSet.close();
        } catch (Exception e) {
            log.error("ERROR_IN_CLOSING_RESULT_SET", e);
        }
        if (statement != null) try {
            statement.close();
        } catch (Exception e) {
            log.error("ERROR_IN_CLOSING_STATEMENT", e);
        }
        if (connection != null) try {
            connection.close();
        } catch (Exception e) {
            log.error("ERROR_IN_CLOSING_CONNECTION", e);
        }
    }
}

