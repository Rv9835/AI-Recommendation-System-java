package service;

import db.DBConnection;
import db.DBConnectionFactory;
import java.util.function.Supplier;
import util.AppLogger;
import org.slf4j.Logger;

/**
 * Authentication-related business logic.
 *
 * This service delegates to a {@link db.DBConnection} provider for all database operations.
 * It is safe to construct with the default constructor in production; tests may inject a
 * supplier to provide fake or in-memory DB connections.
 */
public class AuthService {
    private static final Logger log = AppLogger.get(AuthService.class);
    private final Supplier<DBConnection> provider;
    private final java.util.concurrent.ConcurrentHashMap<String, Attempt> attempts = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000L; // 15 minutes

    public AuthService() {
        this.provider = DBConnectionFactory::getConnection;
    }

    // Constructor for dependency injection (tests)
    public AuthService(Supplier<DBConnection> provider) {
        this.provider = provider != null ? provider : DBConnectionFactory::getConnection;
    }

    /**
        * Attempt to authenticate a user.
        *
        * This method enforces an in-memory throttling policy to limit brute-force
        * attempts. On repeated failures within the configured time window a
        * {@link TooManyAttemptsException} is thrown.
        *
        * @param userId user identifier
        * @param password raw password to verify
        * @return LoginResult containing success flag and display name
        * @throws TooManyAttemptsException when the client exceeded allowed attempts
        * @throws ServiceException on unexpected errors
     */
    public LoginResult login(String userId, String password) {
        // throttling
        Attempt a = attempts.computeIfAbsent(userId, k -> new Attempt());
        synchronized (a) {
            long now = System.currentTimeMillis();
            if (a.count >= MAX_ATTEMPTS && now - a.lastFailure < WINDOW_MS) {
                throw new TooManyAttemptsException("Too many login attempts");
            }
        }
        try (DBConnection conn = provider.get()) {
            boolean valid = conn.verifyLogin(userId, password);
            if (valid) {
                // reset on success
                attempts.remove(userId);
            } else {
                a = attempts.computeIfAbsent(userId, k -> new Attempt());
                synchronized (a) {
                    a.count++;
                    a.lastFailure = System.currentTimeMillis();
                }
            }
            String name = valid ? conn.getUserName(userId) : "";
            return new LoginResult(valid, name);
        } catch (TooManyAttemptsException t) {
            throw t;
        } catch (Exception e) {
            log.error("login failed for {}", userId, e);
            throw new ServiceException("Login failed", e);
        }
    }

    private static class Attempt {
        int count = 0;
        long lastFailure = 0L;
    }

    /**
        * Register a new user record.
        *
        * @param userId desired user id
        * @param password plaintext password
        * @param firstName optional first name
        * @param lastName optional last name
        * @return true on success
        * @throws ServiceException on unexpected errors
     */
    public boolean register(String userId, String password, String firstName, String lastName) {
        try (DBConnection conn = provider.get()) {
            return conn.registerUser(userId, password, firstName, lastName);
        } catch (Exception e) {
            log.error("register failed for {}", userId, e);
            throw new ServiceException("Registration failed", e);
        }
    }

    public record LoginResult(boolean success, String displayName) {}

    /**
     * Fetch the display name for a user.
     */
    public String getDisplayName(String userId) {
        try (DBConnection conn = provider.get()) {
            return conn.getUserName(userId);
        } catch (Exception e) {
            log.error("Failed to fetch display name for {}", userId, e);
            throw new ServiceException("Failed to fetch display name", e);
        }
    }
}
