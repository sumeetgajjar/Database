import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Created by sumeet
 * on 20/1/17.
 */
@FunctionalInterface
public interface PSMaker {
    PreparedStatement getPreparedStatement(Connection connection )throws Exception;
}
