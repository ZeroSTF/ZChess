package tn.zeros.zchess.core.logic.validation;

public class ValidationResult {
    public static final ValidationResult VALID = new ValidationResult(true, "");
    private final boolean valid;
    private final String message;

    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static ValidationResult valid() {
        return VALID;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}
