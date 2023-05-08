package edu.uob.DBException;

import java.io.Serial;

public class SyntaxErrorException extends DBException{

    @Serial
    private static final long serialVersionUID = 6568667184834134619L;

    public SyntaxErrorException(String message) {
        super(message);
    }

    public String toString() {
        return "Syntax error, " + ErrorMessage;
    }
}
