package edu.uob.DBDataStructure;

import java.io.File;
import java.util.stream.StreamSupport;

public class DBPath {
    private final String rootPath;
    private String currentDbPath;
    private String currentTbPath;
    private final String EXTENSION = ".tab";

    public DBPath(File databaseDirectory) {
        String dir = databaseDirectory.getPath();
        System.out.println(dir);
        this.rootPath = dir.substring(0, dir.length() - 1);
        System.out.println(rootPath);
    }

    public String getRootPath(){
        return rootPath;
    }

    public void setCurrentDatabasePath(String databaseName){
        currentDbPath = rootPath + databaseName;
    }

    public String getCurrentDatabasePath(){
        return currentDbPath;
    }

    public void setCurrentTablePath(String tableName){
        currentTbPath = currentDbPath + File.separator + tableName + EXTENSION;
    }

    public String getCurrentTablePath(){
        return currentTbPath;
    }
}
