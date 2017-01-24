import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Types;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by sumeet
 * on 20/1/17.
 */
public class PuzzleHunt extends Database {

    private static final PoolProperties poolProperties = new PoolProperties() {{
        setTestOnBorrow(true);
        setTestOnReturn(true);

    }};
    private static final DatabaseConfig DATABASE_CONFIG = new DatabaseConfig("PUZZLE_HUNT", "puzzle_hunt", "Puzzle_Hunt", "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/puzzle_hunt");
    private static final DatabaseConfig POOLED_DATABASE_CONFIG = new DatabaseConfig("PUZZLE_HUNT", "puzzle_hunt", "Puzzle_Hunt", "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/puzzle_hunt", poolProperties);

    public PuzzleHunt(DatabaseConfig databaseConfig) {
        super(databaseConfig);
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
            Database.setLoggingLevel(Level.ALL);
            testSingleDirectConnection();
            Database.setLoggingLevel(Level.OFF);
            testPooledConnection();
            testRawQuery();
//            testExecuteUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
