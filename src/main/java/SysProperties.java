import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * Created by sumeet
 * on 20/1/17.
 */
public class SysProperties {

    private static SysProperties sysProperties = null;
    private static Properties properties = new Properties();

    private static final String PROPERTY_FILE = "/application.properties";
    private static final Logger log = LogManager.getLogger(SysProperties.class);

    static {
        getInstance();
    }

    private SysProperties() {
        initProperties();
    }

    public static synchronized SysProperties getInstance() {
        if (!Util.isSet(sysProperties)) {
            sysProperties = new SysProperties();
        }
        return sysProperties;
    }

    private static void initProperties() {
        log.info("SYSPROPERITES_INIT_STARTED");
        try {
            properties.load(SysProperties.class.getResourceAsStream(PROPERTY_FILE));
        } catch (Exception e) {
            log.error("SYSPROPERITES_INIT_FAILURE", e);
        }
        log.info("SYSPROPERITES_SIZE|" + properties.size());
    }

    public static String getProperty(String key) {
        return (String) properties.get(key);
    }
}
