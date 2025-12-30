package util;

public final class Validation {
    private Validation() {}

    /** Validate `user_id` format and length. */
    public static boolean isValidUserId(String id) {
        return id != null && id.length() >= 3 && id.length() <= 100 && id.matches("[A-Za-z0-9_\\-@.]+");
    }

    /** Validate password policy: length and presence of alpha + digit. */
    public static boolean isValidPassword(String pw) {
        if (pw == null) return false;
        if (pw.length() < 8 || pw.length() > 128) return false;
        // require at least one digit and one letter
        return pw.matches("(?=.*[0-9])(?=.*[A-Za-z]).*");
    }

    /** Validate a list of item ids (e.g., favorites) with a maximum size. */
    public static boolean isValidIdList(java.util.List<String> ids, int maxSize) {
        if (ids == null) return false;
        if (ids.size() == 0 || ids.size() > maxSize) return false;
        for (String id : ids) {
            if (id == null || id.length() == 0 || id.length() > 200) return false;
            if (!id.matches("[A-Za-z0-9_\\-:.@]+")) return false;
        }
        return true;
    }
}
