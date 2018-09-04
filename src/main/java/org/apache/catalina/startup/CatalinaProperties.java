package org.apache.catalina.startup;

import org.apache.catalina.Globals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by tianye on 2018/9/4.
 */
public class CatalinaProperties {
    private static Properties properties = null;


    static {

        loadProperties();

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return specified property value.
     */
    public static String getProperty(String name) {

        return properties.getProperty(name);

    }


    /**
     * Return specified property value.
     *
     * @deprecated  Unused - will be removed in 8.0.x
     */
    @Deprecated
    public static String getProperty(String name, String defaultValue) {

        return properties.getProperty(name, defaultValue);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Load properties.
     */
    private static void loadProperties() {

        InputStream is = null;
        Throwable error = null;

        try {
            String configUrl = getConfigUrl();
            if (configUrl != null) {
                is = (new URL(configUrl)).openStream();
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }

        if (is == null) {
            try {
                File home = new File(getCatalinaBase());
                File conf = new File(home, "conf");
                File propsFile = new File(conf, "catalina.properties");
                is = new FileInputStream(propsFile);
            } catch (Throwable t) {
                handleThrowable(t);
            }
        }

        if (is == null) {
            try {
                is = CatalinaProperties.class.getResourceAsStream
                        ("/org/apache/catalina/startup/catalina.properties");
            } catch (Throwable t) {
                handleThrowable(t);
            }
        }

        if (is != null) {
            try {
                properties = new Properties();
                properties.load(is);
            } catch (Throwable t) {
                handleThrowable(t);
                error = t;
            }
            finally
            {
                try {
                    is.close();
                } catch (IOException ioe) {
                    System.out.println("Could not close catalina.properties");
                }
            }
        }

        if ((is == null) || (error != null)) {
            // That's fine - we have reasonable defaults.
            properties=new Properties();
        }

        // Register the properties as system properties
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = properties.getProperty(name);
            if (value != null) {
                System.setProperty(name, value);
            }
        }

    }


    /**
     * Get the value of the catalina.home environment variable.
     */
    private static String getCatalinaHome() {
        return System.getProperty(Globals.CATALINA_HOME_PROP,
                System.getProperty("user.dir"));
    }


    /**
     * Get the value of the catalina.base environment variable.
     */
    private static String getCatalinaBase() {
        return System.getProperty(Globals.CATALINA_BASE_PROP, getCatalinaHome());
    }


    /**
     * Get the value of the configuration URL.
     */
    private static String getConfigUrl() {
        return System.getProperty("catalina.config");
    }

    // Copied from ExceptionUtils since that class is not visible during start
    private static void handleThrowable(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }
}
