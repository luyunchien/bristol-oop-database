package edu.uob.DBException;

import java.io.Serial;

public class AttributeAlreadyExistsException extends DBException{
    @Serial
    private static final long serialVersionUID = -1346107633395828537L;

    public AttributeAlreadyExistsException(String message) {
        super(message);
    }

    public String toString() {
        return "The " + ErrorMessage + " already exists";
    }
}
