package tn.zeros.zchess.core.logic.validation;

public class ValidationResult{
    private final boolean valid;
    private final String message;

    public static final ValidationResult VALID = new ValidationResult(true, "");

    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public static ValidationResult valid() {
        return VALID;
    }
}
