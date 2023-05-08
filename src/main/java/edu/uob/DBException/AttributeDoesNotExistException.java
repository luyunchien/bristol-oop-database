package edu.uob.DBException;

import java.io.Serial;

public class AttributeDoesNotExistException extends DBException{
    @Serial
    private static final long serialVersionUID = 7790717974429740927L;

    public AttributeDoesNotExistException(String message) {
        super(message);
    }

    public String toString() {
        return "The " + ErrorMessage + " does not exist";
    }
}
