package db;

/**
 * Unchecked exception representing failures in the data access layer.
 * DAOs should wrap SQLExceptions in this type to avoid leaking SQL internals
 * into higher layers and to enable centralized error handling.
 */
 
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
