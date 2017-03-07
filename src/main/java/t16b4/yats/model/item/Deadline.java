package t16b4.yats.model.item;

import t16b4.yats.commons.exceptions.IllegalValueException;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidDeadline(String)}
 */
public class Deadline {

    public static final String MESSAGE_PHONE_CONSTRAINTS = "Person phone numbers should only contain numbers";
    public static final String PHONE_VALIDATION_REGEX = "\\d+";

    public final String value;

    /**
     * Validates given phone number.
     *
     * @throws IllegalValueException if given phone string is invalid.
     */
    public Deadline(String deadline) throws IllegalValueException {
        assert deadline != null;
        String trimmedDeadline = deadline.trim();
        if (!isValidDeadline(trimmedDeadline)) {
            throw new IllegalValueException(MESSAGE_PHONE_CONSTRAINTS);
        }
        this.value = trimmedDeadline;
    }

    /**
     * Returns true if a given string is a valid person phone number.
     */
    public static boolean isValidDeadline(String test) {
        return test.matches(PHONE_VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Deadline // instanceof handles nulls
                && this.value.equals(((Deadline) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
