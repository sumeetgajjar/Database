import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Types;
import java.util.List;

/**
 * Created by sumeet
 * on 20/1/17.
 */
public class PuzzleHunt extends Database {

    private static final PoolProperties poolProperties = new PoolProperties() {{
        poolProperties.setTestOnBorrow(true);
        poolProperties.setTestOnReturn(true);

    }};
    private static final DatabaseConfig DATABASE_CONFIG = new DatabaseConfig("PUZZLE_HUNT", "com.mysql.jdbc.Driver", "puzzle_hunt", "Puzzle_Hunt", "jdbc:mysql://localhost:3306/puzzle_hunt");
    private static final DatabaseConfig POOLED_DATABASE_CONFIG = new DatabaseConfig("PUZZLE_HUNT", "com.mysql.jdbc.Driver", "puzzle_hunt", "Puzzle_Hunt", "jdbc:mysql://localhost:3306/puzzle_hunt", poolProperties);
    private static final Logger log = LogManager.getLogger(PuzzleHunt.class);

    public PuzzleHunt(DatabaseConfig databaseConfig) {
        super(databaseConfig);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    public static void testSingleDirectConnection() throws Exception {
        PuzzleHunt puzzleHunt = new PuzzleHunt(DATABASE_CONFIG);
        StoredProcedureCall<Integer> storedProcedureCall = new StoredProcedureCall<>("get_user_count", resultSet -> (resultSet.getInt("count(*)")));
        List<Integer> integers = puzzleHunt.executeQuery(storedProcedureCall);
        System.out.println(integers);

    }

    public static void testPooledConnection() throws Exception {
        PuzzleHunt puzzleHuntPooled = new PuzzleHunt(POOLED_DATABASE_CONFIG);
        StoredProcedureCall<Integer> storedProcedureCall = new StoredProcedureCall<>("get_user_count", resultSet -> (resultSet.getInt("count(*)")));
        List<Integer> integers = puzzleHuntPooled.executeQuery(storedProcedureCall);
        System.out.println(integers);
    }

    public static void testRawQuery() throws Exception {
        PuzzleHunt puzzleHunt = new PuzzleHunt(DATABASE_CONFIG);
        List<String> v = puzzleHunt.executeQuery(connection -> connection.prepareStatement("SELECT @@VERSION as v"), resultSet -> resultSet.getString("v"));
        System.out.println(v);
    }

    public static void testExecuteUpdate() throws Exception {
        PuzzleHunt puzzleHunt = new PuzzleHunt(DATABASE_CONFIG);
        StoredProcedureCall<Integer> storedProcedureCall = new StoredProcedureCall<>("insert_user");
        storedProcedureCall.addParameter("name", "test_user", Types.VARCHAR);
        int affectedRows = puzzleHunt.executeUpdate(storedProcedureCall);
        System.out.println(affectedRows);
    }

    public static void main(String[] args) {
        try {
            testSingleDirectConnection();
            testPooledConnection();
            testRawQuery();
            testExecuteUpdate();
        } catch (Exception e) {
            log.error("EXCEPTION_CAUGHT");
        }
    }
}
