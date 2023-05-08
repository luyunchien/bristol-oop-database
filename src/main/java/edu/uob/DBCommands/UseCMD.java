package edu.uob.DBCommands;

import edu.uob.DBDataStructure.*;
import edu.uob.DBException.DBException;

import java.io.IOException;

public class UseCMD extends DBCmd{

    public UseCMD(DBPath rootPath) {
        super(rootPath);
    }

    @Override
    public void interpretCMD() throws DBException, IOException {
        rootPath.setCurrentDatabasePath(dbToBeExecuted.getDatabaseName());
        checkDatabaseExists();
    }
}
