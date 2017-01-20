import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by sumeet
 * on 20/1/17.
 */
public class StoredProcedureCall<T> {
    private final String storedProcedureName;
    private final Function<ResultSet, T> rowMapper;
    private List<Parameter> parameters;

    public StoredProcedureCall(String storedProcedureName, Function<ResultSet, T> rowMapper) {
        this.storedProcedureName = storedProcedureName;
        this.rowMapper = rowMapper;
        parameters = new ArrayList<>();
    }

    public void addParameter(String name, Object value, int type) {
        this.parameters.add(new Parameter(name, value, type));
    }

    public void addNullParameters(String... params) {
        for (String param : params) {
            this.parameters.add(new Parameter(param, null, Types.NULL));
        }
    }

    public String getStoredProcedureName() {
        return storedProcedureName;
    }

    public Function<ResultSet, T> getRowMapper() {
        return rowMapper;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }
}
