package edu.uob.DBException;

import java.io.Serial;

public class DBException extends Throwable {
    @Serial
    private static final long serialVersionUID = -7127487914916171620L;
    String ErrorMessage;

    public DBException(String message) {
        this.ErrorMessage = message;
    }

    public String toString(){
        return ErrorMessage;
    }
}
