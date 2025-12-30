package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppLogger {
    private AppLogger() {}

    /**
     * Convenience wrapper for obtaining SLF4J loggers attached to a class.
     */
    public static Logger get(Class<?> cls) {
        return LoggerFactory.getLogger(cls);
    }
}
