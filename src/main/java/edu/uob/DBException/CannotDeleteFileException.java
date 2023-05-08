package edu.uob.DBException;

import java.io.Serial;

public class CannotDeleteFileException extends DBException{
    @Serial
    private static final long serialVersionUID = 7348119454517349206L;

    public CannotDeleteFileException(String message) {
        super(message);
    }

    public String toString() {
        return "Unable to delete the " + ErrorMessage;
    }
}
