import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sumeet
 * on 20/1/17.
 */
public abstract class Database {
    private final DatabaseConfig databaseConfig;

    private final static Map<String, DataSource> DATA_SOURCE_MAP = new HashMap<>();
    private final static Logger LOGGER = LoggerFactory.getLogger(Database.class);

    public Database(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;

        if (databaseConfig.usePool) {
            databaseConfig.poolProperties.setUrl(databaseConfig.url);
            databaseConfig.poolProperties.setDriverClassName(databaseConfig.driver);
            databaseConfig.poolProperties.setUsername(databaseConfig.user);
            databaseConfig.poolProperties.setPassword(databaseConfig.password);
            instantiatePool();
        }
    }

    private void instantiatePool() {
        String poolMapKey = getDataSourcePoolMapKey();
        DataSource dataSource = DATA_SOURCE_MAP.getOrDefault(poolMapKey, null);
        if (dataSource == null) {
            synchronized (Database.class) {
                dataSource = DATA_SOURCE_MAP.getOrDefault(poolMapKey, null);
                if (dataSource == null) {
                    LOGGER.info(getLogPrefix() + "INITIALIZING_DATASOURCE|{}", poolMapKey);
                    dataSource = new DataSource(databaseConfig.poolProperties);
                    LOGGER.info(getLogPrefix() + "DATASOURCE_INITIALIZED|{}", poolMapKey);
                    DATA_SOURCE_MAP.put(poolMapKey, dataSource);
                }
            }
        }
    }

    private String getDataSourcePoolMapKey() {
        return databaseConfig.databaseType + "|" + databaseConfig.databaseName;
    }

    private Connection getSingleDirectConnection() throws ClassNotFoundException, SQLException {
        Class.forName(databaseConfig.driver);
        return DriverManager.getConnection(databaseConfig.url, databaseConfig.user, databaseConfig.password);
    }

    protected Connection getConnection() throws Exception {
        long startTime = System.currentTimeMillis();
        Connection connection;
        String poolMapKey = getDataSourcePoolMapKey();
        if (databaseConfig.usePool) {
            connection = DATA_SOURCE_MAP.get(poolMapKey).getConnection();
        } else {
            connection = getSingleDirectConnection();
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOGGER.info(getLogPrefix() + "DB_CONNECT_TIME|{}|{}", poolMapKey, timeTaken);
        return connection;
    }

    protected PreparedStatement getPreparedStatement(Connection connection, String storedProcedureName, List<Parameter> parameters) throws Exception {
        String procedureCallString = databaseConfig.databaseType.getProcedureCallString(storedProcedureName, parameters);
        LOGGER.debug(getLogPrefix() + "SP_CALL_STRING|{}", procedureCallString);

        PreparedStatement preparedStatement = connection.prepareStatement(procedureCallString);
        preparedStatement.setEscapeProcessing(true);
        int index = 0;
        for (Parameter parameter : parameters) {
            preparedStatement.setObject(++index, parameter.getValue(), parameter.getType());
        }
        return preparedStatement;
    }

    protected <T> List<T> executeQuery(StoredProcedureCall<T> storedProcedureCall) throws DatabaseException {
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

            RowMapper<T> rowMapper = storedProcedureCall.getRowMapper();
            rows = new LinkedList<>();

            while (resultSet.next()) {
                T row = rowMapper.map(resultSet);
                rows.add(row);
            }

        } catch (Exception e) {
            LOGGER.error(getLogPrefix() + "{}_FAILURE|{}", storedProcedureName, e.getMessage());
            throw new DatabaseException(databaseConfig, storedProcedureName, e);
        } finally {
            closeDatabaseConnection(resultSet, preparedStatement, connection);
            long timeTaken = System.currentTimeMillis() - startTime;
            LOGGER.info(getLogPrefix() + "SP_TIME|{}|{}", storedProcedureName, timeTaken);
        }
        return rows;
    }

    protected <T> int executeUpdate(StoredProcedureCall<T> storedProcedureCall) throws DatabaseException {
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
            LOGGER.error(getLogPrefix() + "{}_FAILURE", storedProcedureName);
            throw new DatabaseException(databaseConfig, storedProcedureName, e);
        } finally {
            closeDatabaseConnection(null, preparedStatement, connection);
            long timeTaken = System.currentTimeMillis() - startTime;
            LOGGER.info(getLogPrefix() + "SP_TIME|{}|{}", storedProcedureName, timeTaken);
        }
        return affectedRows;
    }

    protected <T> List<T> executeQuery(PSMaker psMaker, RowMapper<T> rowMapper) throws DatabaseException {
        long startTime = System.currentTimeMillis();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<T> rows = null;

        try {
            connection = getConnection();
            preparedStatement = psMaker.getPreparedStatement(connection);
            resultSet = preparedStatement.executeQuery();

            rows = new LinkedList<>();

            while (resultSet.next()) {
                T row = rowMapper.map(resultSet);
                rows.add(row);
            }

        } catch (Exception e) {
            LOGGER.error(getLogPrefix() + "RAW_QUERY_FAILURE");
            throw new DatabaseException(databaseConfig, "RAW_QUERY", e);
        } finally {
            closeDatabaseConnection(resultSet, preparedStatement, connection);
            long timeTaken = System.currentTimeMillis() - startTime;
            LOGGER.info(getLogPrefix() + "RAW_QUERY|{}", timeTaken);
        }
        return rows;
    }

    protected void closeDatabaseConnection(ResultSet resultSet, PreparedStatement statement, Connection connection) {
        if (resultSet != null) try {
            resultSet.close();
        } catch (Exception e) {
            LOGGER.warn(getLogPrefix() + "ERROR_IN_CLOSING_RESULT_SET", e);
        }
        if (statement != null) try {
            statement.close();
        } catch (Exception e) {
            LOGGER.warn(getLogPrefix() + "ERROR_IN_CLOSING_STATEMENT", e);
        }
        if (connection != null) try {
            connection.close();
        } catch (Exception e) {
            LOGGER.warn(getLogPrefix() + "ERROR_IN_CLOSING_CONNECTION", e);
        }
    }

    protected String getLogPrefix() {
        return "";
    }
}

