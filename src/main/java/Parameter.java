/**
 * Created by sumeet
 * on 20/1/17.
 */
public class Parameter {
    private final String name;
    private final int type;
    private final Object value;

    public Parameter(String name, Object value, int type) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return "@" + name;
    }

    public Object getValue() {
        return value;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        String tempValue;
        if (value != null) {
            if (value instanceof String) {
                tempValue = "'" + value + "'";
            } else {
                tempValue = value.toString();
            }
        } else {
            tempValue = "NULL";
        }
        return tempValue;
    }
}
