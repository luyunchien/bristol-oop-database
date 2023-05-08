package edu.uob.DBException;

import java.io.Serial;

public class FileAlreadyExistsException extends DBException{
    @Serial
    private static final long serialVersionUID = -8918121091594094691L;

    public FileAlreadyExistsException(String message) {
        super(message);
    }

    public String toString() {
        return "The " + ErrorMessage + " already exists";
    }
}
