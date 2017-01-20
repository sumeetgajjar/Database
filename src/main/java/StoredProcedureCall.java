import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sumeet
 * on 20/1/17.
 */
public class StoredProcedureCall<T> {
    private final String storedProcedureName;
    private final RowMapper<T> rowMapper;
    private List<Parameter> parameters;

    public StoredProcedureCall(String storedProcedureName) {
        this(storedProcedureName, null);
    }

    public StoredProcedureCall(String storedProcedureName, RowMapper<T> rowMapper) {
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

    public RowMapper<T> getRowMapper() {
        return rowMapper;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }
}
