package service;

/**
 * Generic unchecked exception for service-layer failures. Wraps lower-level exceptions
 * to provide a consistent exception type to the web layer.
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(String message) {
        super(message);
    }
}
