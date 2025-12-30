package service;

/**
 * Thrown when a user exceeds allowed login attempts within a window.
 */
public class TooManyAttemptsException extends ServiceException {
    public TooManyAttemptsException(String message) {
        super(message);
    }
}
