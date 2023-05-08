package edu.uob.DBException;

import java.io.Serial;

public class FileDoesNotExistException extends DBException{
    @Serial
    private static final long serialVersionUID = 1655990225946139011L;

    public FileDoesNotExistException(String message) {
        super(message);
    }

    public String toString() {
        return "The " + ErrorMessage + " does not exist";
    }
}
