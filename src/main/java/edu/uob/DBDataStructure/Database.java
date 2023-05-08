package edu.uob.DBDataStructure;

import edu.uob.DBException.DBException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Database {
    private String databaseName;
    private final HashMap<String, Table> listOfTables = new HashMap<>();
    private final DBPath rootPath;

    public Database(DBPath rootPath) {
        this.rootPath = rootPath;
    }

    public void setDatabaseName(String name){
        databaseName = name;
    }

    public String getDatabaseName(){
        return databaseName;
    }

    public void addTable(Table newTable) throws DBException, IOException {
        FileIO newFile = new FileIO(rootPath, newTable);
        listOfTables.put(newTable.getTableName(),newTable);
        newFile.writeTable();
    }

    public Table getTable(String tableName){
        return listOfTables.get(tableName);
    }

    public void initializeDb(String dbName) throws IOException {
        rootPath.setCurrentDatabasePath(dbName);
        File db = new File(rootPath.getCurrentDatabasePath());
        File[] allTables = db.listFiles((dir, name) -> !name.equals(".DS_Store"));
        if(allTables == null){
            return;
        }
        for(File file : allTables){
            Table table = new Table();
            table.setTableName(file.getName().substring(0,file.getName().length()-4));
            FileIO fileIO = new FileIO(rootPath, table);
            fileIO.readTable();
            listOfTables.put(file.getName(),fileIO.getTable());
        }

    }

}
