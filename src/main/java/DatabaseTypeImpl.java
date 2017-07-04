import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sumeet
 * on 25/4/17.
 */
public enum DatabaseTypeImpl implements DatabaseType {

    MSSQL {
        @Override
        public String getProcedureCallString(String storedProcedureName, List<Parameter> parameters) {
            List<String> paramList = parameters.stream().map(parameter -> parameter.getName() + " = ?").collect(Collectors.toList());
            String parameterPlaceHolders = Util.implode(",", paramList);
            String procedureCallString = String.format("{call %s (%s) }", storedProcedureName, parameterPlaceHolders);
            return procedureCallString;
        }
    },

    MYSQL {
        @Override
        public String getProcedureCallString(String storedProcedureName, List<Parameter> parameters) {
            List<String> paramList = parameters.stream().map(parameter -> parameter.getName() + " := ?").collect(Collectors.toList());
            String parameterPlaceHolders = Util.implode(",", paramList);
            String procedureCallString = String.format("{call %s (%s) }", storedProcedureName, parameterPlaceHolders);
            return procedureCallString;
        }
    }
}