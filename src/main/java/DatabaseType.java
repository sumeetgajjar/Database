import java.util.List;

/**
 * Created by sumeet
 * on 25/4/17.
 */
@FunctionalInterface
public interface DatabaseType {
    String getProcedureCallString(String storedProcedureName, List<Parameter> parameters);
}