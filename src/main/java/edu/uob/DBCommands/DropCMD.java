package edu.uob.DBCommands;

import edu.uob.DBDataStructure.DBPath;
import edu.uob.DBException.CannotDeleteFileException;
import edu.uob.DBException.DBException;
import edu.uob.DBException.FileDoesNotExistException;
import edu.uob.DBException.SyntaxErrorException;

import java.io.File;

import java.util.ArrayList;

public class DropCMD extends DBCmd {

    public DropCMD(DBPath rootPath) {
        super(rootPath);
        tableNames = new ArrayList<>();
    }

    @Override
    public void interpretCMD() throws DBException {
        if(structureType.equals("DATABASE")){
            checkDatabaseExists();
            dropDatabase();
        }else if(structureType.equals("TABLE")){
            checkTableExists();
            rootPath.setCurrentTablePath(tableNames.get(0));
            File table = new File(rootPath.getCurrentTablePath());
            table.delete();
            if(table.exists()) throw new CannotDeleteFileException("table");
        }else throw new SyntaxErrorException("invalid structure type.");
    }

    private void dropDatabase() throws DBException {
        File dbTobeDropped = new File(rootPath.getCurrentDatabasePath());
        File[] tables = dbTobeDropped.listFiles();
        if (tables != null) {
            for(File f : tables){
                f.delete();
                if(f.exists()) throw new CannotDeleteFileException("table");
            }
        }
        dbTobeDropped.delete();
        if(dbTobeDropped.exists()) throw new CannotDeleteFileException("database");

    }


}
