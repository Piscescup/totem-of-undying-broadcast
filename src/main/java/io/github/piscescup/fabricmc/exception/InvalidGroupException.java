package io.github.piscescup.fabricmc.exception;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class InvalidGroupException extends IllegalArgumentException {
    public InvalidGroupException(String value, String invalidKey) {
        super("Invalid group " + invalidKey + " for " + value);
    }

    public static InvalidGroupException name(String groupName) {
        return new InvalidGroupException(groupName, "name");
    }

    public static InvalidGroupException id(int id) {
        return new InvalidGroupException(String.valueOf(id), "id");
    }
}
