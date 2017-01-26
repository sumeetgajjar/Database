import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sumeet
 * on 20/1/17.
 */
public abstract class Database {
    private final DatabaseConfig databaseConfig;

    private final static Map<String, DataSource> dataSourceMap = new HashMap<>();
    private final static Logger LOGGER = LoggerFactory.getLogger(Database.class);

    public Database(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;

        if (databaseConfig.usePool) {
            databaseConfig.poolProperties.setUrl(databaseConfig.url);
            databaseConfig.poolProperties.setDriverClassName(databaseConfig.driver);
            databaseConfig.poolProperties.setUsername(databaseConfig.user);
            databaseConfig.poolProperties.setPassword(databaseConfig.password);
            instantiatePool(databaseConfig.poolProperties);
        }
    }

    private void instantiatePool(PoolProperties poolProperties) {
        DataSource dataSource = dataSourceMap.getOrDefault(databaseConfig.databaseName, null);
        if (dataSource == null) {
            synchronized (Database.class) {
                dataSource = dataSourceMap.getOrDefault(databaseConfig.databaseName, null);
                if (dataSource == null) {
                    LOGGER.info("INITIALIZING_DATASOURCE|{}", databaseConfig.databaseName);
                    dataSource = new DataSource(poolProperties);
                    LOGGER.info("DATASOURCE_INITIALIZED|{}", databaseConfig.databaseName);
                    dataSourceMap.put(databaseConfig.databaseName, dataSource);
                }
            }
        }
    }

    private Connection getSingleDirectConnection() throws ClassNotFoundException, SQLException {
        Class.forName(databaseConfig.driver);
        return DriverManager.getConnection(databaseConfig.url, databaseConfig.user, databaseConfig.password);
    }

    protected Connection getConnection() throws Exception {
        long startTime = System.currentTimeMillis();
        Connection connection;
        if (databaseConfig.usePool) {
            connection = dataSourceMap.get(databaseConfig.databaseName).getConnection();
        } else {
            connection = getSingleDirectConnection();
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOGGER.info("DB_CONNECT_TIME|{}|{}", databaseConfig.databaseName, timeTaken);
        return connection;
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
            LOGGER.error("{}_FAILURE", storedProcedureName);
            throw new DatabaseException(databaseConfig.databaseName, storedProcedureName, e);
        } finally {
            closeDatabaseConnection(resultSet, preparedStatement, connection);
            long timeTaken = System.currentTimeMillis() - startTime;
            LOGGER.info("SP_TIME|{}|{}", storedProcedureName, timeTaken);
        }
        return rows;
    }

    protected int executeUpdate(StoredProcedureCall<Integer> storedProcedureCall) throws DatabaseException {
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
            LOGGER.error("{}_FAILURE", storedProcedureName);
            throw new DatabaseException(databaseConfig.databaseName, storedProcedureName, e);
        } finally {
            closeDatabaseConnection(null, preparedStatement, connection);
            long timeTaken = System.currentTimeMillis() - startTime;
            LOGGER.info("SP_TIME|{}|{}", storedProcedureName, timeTaken);
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
            LOGGER.error("RAW_QUERY_FAILURE");
            throw new DatabaseException(databaseConfig.databaseName, "RAW_QUERY", e);
        } finally {
            closeDatabaseConnection(resultSet, preparedStatement, connection);
            long timeTaken = System.currentTimeMillis() - startTime;
            LOGGER.info("RAW_QUERY|{}", timeTaken);
        }
        return rows;
    }

    protected void closeDatabaseConnection(ResultSet resultSet, PreparedStatement statement, Connection connection) {
        if (resultSet != null) try {
            resultSet.close();
        } catch (Exception e) {
            LOGGER.warn("ERROR_IN_CLOSING_RESULT_SET", e);
        }
        if (statement != null) try {
            statement.close();
        } catch (Exception e) {
            LOGGER.warn("ERROR_IN_CLOSING_STATEMENT", e);
        }
        if (connection != null) try {
            connection.close();
        } catch (Exception e) {
            LOGGER.warn("ERROR_IN_CLOSING_CONNECTION", e);
        }
    }
}

