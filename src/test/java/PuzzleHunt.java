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

    private static final Logger log = LogManager.getLogger(PuzzleHunt.class);

    public PuzzleHunt() {
        super("PUZZLE_HUNT");
    }

    public PuzzleHunt(PoolProperties poolProperties) {
        super("PUZZLE_HUNT", poolProperties);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    public static void testSingleDirectConnection() throws Exception {
        PuzzleHunt puzzleHunt = new PuzzleHunt();
        StoredProcedureCall<Integer> storedProcedureCall = new StoredProcedureCall<>("get_user_count", resultSet -> (resultSet.getInt("count(*)")));
        List<Integer> integers = puzzleHunt.executeQuery(storedProcedureCall);
        System.out.println(integers);

    }

    public static void testPooledConnection() throws Exception {
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setTestOnBorrow(true);
        poolProperties.setTestOnReturn(true);
        PuzzleHunt puzzleHuntPooled = new PuzzleHunt(poolProperties);
        StoredProcedureCall<Integer> storedProcedureCall = new StoredProcedureCall<>("get_user_count", resultSet -> (resultSet.getInt("count(*)")));
        List<Integer> integers = puzzleHuntPooled.executeQuery(storedProcedureCall);
        System.out.println(integers);
    }

    public static void testRawQuery() throws Exception {
        PuzzleHunt puzzleHunt = new PuzzleHunt();
        List<String> v = puzzleHunt.executeQuery(connection -> connection.prepareStatement("SELECT @@VERSION as v"), resultSet -> resultSet.getString("v"));
        System.out.println(v);
    }

    public static void testExecuteUpdate() throws Exception {
        PuzzleHunt puzzleHunt = new PuzzleHunt();
        StoredProcedureCall<Integer> storedProcedureCall = new StoredProcedureCall<>("insert_user");
        storedProcedureCall.addParameter("name", "test_user", Types.VARCHAR);
        int affectedRows = puzzleHunt.executeUpdate(storedProcedureCall);
        System.out.println(affectedRows);
    }

    public static void main(String[] args) throws Exception {
        testSingleDirectConnection();
        testPooledConnection();
        testRawQuery();
        testExecuteUpdate();
    }
}
