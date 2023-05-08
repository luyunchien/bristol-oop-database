package edu.uob.DBCommands;

import edu.uob.DBDataStructure.DBPath;
import edu.uob.DBDataStructure.Database;
import edu.uob.DBDataStructure.FileIO;
import edu.uob.DBDataStructure.Table;
import edu.uob.DBEngine.Regex;
import edu.uob.DBException.CannotCreateFileException;
import edu.uob.DBException.DBException;
import edu.uob.DBException.FileAlreadyExistsException;
import edu.uob.DBException.SyntaxErrorException;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class CreateCMD extends DBCmd{

    public CreateCMD(DBPath rootPath) {
        super(rootPath);
        attributeNames = new ArrayList<>();
        tableNames = new ArrayList<>();
    }

    public void interpretCMD() throws DBException, IOException {
        if(structureType.equals("DATABASE")){
            dbToBeExecuted = new Database(rootPath);
            dbToBeExecuted.setDatabaseName(databaseName);
            rootPath.setCurrentDatabasePath(databaseName);
            createDatabase();
        }else if(structureType.equals("TABLE")){
            if(tableNames.isEmpty()) throw new SyntaxErrorException("there is no table name");
            rootPath.setCurrentTablePath(tableNames.get(0));
            createTable();
        }else throw new SyntaxErrorException("invalid structure type.");
    }

    private void createDatabase() throws DBException {
        File newDatabase = new File(rootPath.getCurrentDatabasePath());
        if(newDatabase.isDirectory()) throw new FileAlreadyExistsException("directory");
        if (!newDatabase.mkdir()) throw new CannotCreateFileException("directory");
    }

    private void createTable() throws DBException, IOException {
        File newTableFile = new File(rootPath.getCurrentTablePath());
        if(newTableFile.exists()) throw new FileAlreadyExistsException("table");

        Table newTable = new Table();
        newTable.setTableName(tableNames.get(0));

        if(!attributeNames.isEmpty()){
            checkIfAttributeNameContainsID();
            attributeNames.add(0, "id");
            newTable.setAttributes(attributeNames);
            FileIO fileIO = new FileIO(rootPath, newTable);
            fileIO.setTable(newTable);
            fileIO.writeTable();
        }
        dbToBeExecuted.addTable(newTable);
    }

    private void checkIfAttributeNameContainsID() throws DBException{
        for(String name : attributeNames){
            if(name.matches(Regex.ID.getType())){
                throw new SyntaxErrorException("attribute name cannot be 'id'");
            }
        }
    }
}
