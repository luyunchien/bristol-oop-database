package edu.uob.DBCommands;

import edu.uob.DBDataStructure.DBPath;
import edu.uob.DBDataStructure.FileIO;
import edu.uob.DBDataStructure.Table;
import edu.uob.DBEngine.Regex;
import edu.uob.DBException.AttributeAlreadyExistsException;
import edu.uob.DBException.AttributeDoesNotExistException;
import edu.uob.DBException.DBException;
import edu.uob.DBException.SyntaxErrorException;

import java.util.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class AlterCMD extends DBCmd{


    public AlterCMD(DBPath rootPath) {
        super(rootPath);
        tableNames = new ArrayList<>();
        attributeNames = new ArrayList<>();
        currentTable = new Table();
    }

    @Override
    public void interpretCMD() throws DBException, IOException {
        checkTableExists();
        tbToBeExecuted = dbToBeExecuted.getTable(tableNames.get(0) + EXTENSION);
        currentTable.setTableName(tbToBeExecuted.getTableName());
        if(alterationType.equals("ADD")){
            addAttributeToTable();

        }else if(alterationType.equals("DROP")){
            if(attributeNames.get(0).matches(Regex.ID.getType())){
                throw new DBException("id cannot be dropped");
            }
            tbToBeExecuted.dropAttribute(attributeNames.get(0));
        }else throw new SyntaxErrorException("only ADD and DROP is valid");
        FileIO fileIO = new FileIO(rootPath, tbToBeExecuted);
        fileIO.writeTable();
    }

    private void addAttributeToTable() throws DBException {
        List<String> attributeList = tbToBeExecuted.getAttributeList();
        checkIfAttributeExists(attributeNames.get(0));
        attributeList.add(attributeNames.get(0));
        currentTable.setAttributes(attributeList);
        for(int j=0; j<tbToBeExecuted.getNumOfEntries(); j++){
            Map<String,String> row = tbToBeExecuted.getEntry(j);
            String[] newRow = new String[attributeList.size()];
            for(int i=0; i<tbToBeExecuted.getNumOfAttributes();i++){
                newRow[i] = row.get(tbToBeExecuted.getAttributeByIndex(i));
            }
            newRow[attributeList.size()-1] = "NULL";
            currentTable.addEntry(newRow);
        }
        tbToBeExecuted = currentTable;
        checkAttributesExist();
    }

    private void checkIfAttributeExists(String attribute) throws DBException {
        List<String> attributeList = tbToBeExecuted.getAttributeList();
        for(String s : attributeList){
            if(s.equals(attribute)) {
                throw new AttributeAlreadyExistsException("attribute");
            }
        }
    }

}
