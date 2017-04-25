import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sumeet
 * on 20/1/17.
 */
public class Util {

    public static boolean isSet(Object object) {
        return object != null;
    }

    public static String implode(String separator, Collection<?> values) {
        StringBuilder builder = new StringBuilder();
        Iterator<?> iterator = values.iterator();
        if (iterator.hasNext()) {
            builder.append(iterator.next());
            while (iterator.hasNext()) {
                builder.append(separator).append(iterator.next());
            }
        }
        return builder.toString();
    }

    public static List<String> repeat(String value, int count) {
        List<String> returnList = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            returnList.add(value);
        }
        return returnList;
    }
}

