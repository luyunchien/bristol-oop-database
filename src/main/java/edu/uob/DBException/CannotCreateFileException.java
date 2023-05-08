package edu.uob.DBException;

import java.io.Serial;

public class CannotCreateFileException extends DBException{
    @Serial
    private static final long serialVersionUID = -6496792505472954521L;

    public CannotCreateFileException(String message) {
        super(message);
    }

    public String toString() {
        return "Unable to create a new " + ErrorMessage;
    }
}
