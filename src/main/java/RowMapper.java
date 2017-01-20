import java.sql.ResultSet;

/**
 * Created by sumeet
 * on 20/1/17.
 */
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet resultSet) throws Exception;
}
