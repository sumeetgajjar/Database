# Database
Simple Database wrapper written in Java to easily execute storedProcedure with flexible functions to meet requirements for all cases.

In case one wants to enable logging then corresponding slf4j implementation for respective logger will have to be added as a dependency.

Time taken for establishing DB connection as well as for executing the query is logged at **INFO** level. 

## How to use it !!

#### Create DatabaseConfig
* For Single Connection
```java
DatabaseConfig MSSQL_DATABASE_CONFIG = new DatabaseConfig("TEST_MASTER", 
                                                        DatabaseTypeImpl.MSSQL, 
                                                        "test_user", 
                                                        "test_password", 
                                                        "net.sourceforge.jtds.jdbc.Driver", 
                                                        "jdbc:jtds:sqlserver://localhost;databaseName=TEST_MASTER");
```

* For Pooled Connections 
    *Create PoolProperties and Pass it to DatabaseConfig
```java
PoolProperties POOL_PROPERTIES = new PoolProperties() {{
        setTestOnBorrow(true);
        setTestOnReturn(true);
    }};
DatabaseConfig MSSQL_POOLED_DATABASE_CONFIG = new DatabaseConfig("TEST_MASTER", 
                                                        DatabaseTypeImpl.MSSQL, 
                                                        "test_user", 
                                                        "test_password", 
                                                        "net.sourceforge.jtds.jdbc.Driver", 
                                                        "jdbc:jtds:sqlserver://localhost;databaseName=TEST_MASTER", 
                                                        POOL_PROPERTIES);
```

#### Extend abstract class Database and match the constructor

```java
public class DatabaseTest extends Database {
    public DatabaseTest(DatabaseConfig databaseConfig) {
            super(databaseConfig);
    }
}
```

#### Initialize the Derived Class by passing DatabaseConfig to it

```java
DatabaseTest mssqlDB = new DatabaseTest(MSSQL_DATABASE_CONFIG);
DatabaseTest mssqlPooledDB = new DatabaseTest(MSSQL_POOLED_DATABASE_CONFIG);
```

#### Use the Derived Class object to executeQuery

```java
StoredProcedureCall<String> storedProcedureCall = new StoredProcedureCall<>("sp_server_info", resultSet -> (resultSet.getString("attribute_name")));
List<String> values = database.executeQuery(storedProcedureCall);
System.out.println(values);
```
