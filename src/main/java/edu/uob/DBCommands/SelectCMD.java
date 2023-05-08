package edu.uob.DBCommands;

import edu.uob.DBDataStructure.*;
import edu.uob.DBException.AttributeDoesNotExistException;
import edu.uob.DBException.DBException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SelectCMD extends DBCmd{

    private List<String> selectedAttributes;


    public SelectCMD(DBPath rootPath) {
        super(rootPath);
        tableNames = new ArrayList<>();
        attributeNames = new ArrayList<>();
        conditions = new Stack<>();
        operations = new Stack<>();
        tbToBeExecuted = new Table();
        currentTable = new Table();
    }

    @Override
    public void interpretCMD() throws DBException {
        checkTableExists();
        checkAttributesExist();
        tbToBeExecuted = dbToBeExecuted.getTable(tableNames.get(0) + EXTENSION);
        setSelectedAttributes();
        currentTable = setSelectTableEntries();
    }

    private void setSelectedAttributes() throws DBException {
        if(attributeNames.isEmpty()){
            selectedAttributes = tbToBeExecuted.getAttributeList();
        }else {
            for(String s : attributeNames){
                if(!tbToBeExecuted.getAttributeList().contains(s)) {
                    throw new AttributeDoesNotExistException("attribute");
                }
            }
            selectedAttributes = attributeNames;
        }
    }

    private Table setSelectTableEntries() throws DBException {
        Table newTable = new Table();
        newTable.setAttributes(selectedAttributes);

        if(conditions.isEmpty()){
            for(int i=0; i<tbToBeExecuted.getNumOfEntries(); i++){
                addEntries(i,newTable);
            }
        }else {
            List<Integer> selectedEntryIndex = getSelectedEntryIndex();
            for (Integer entryIndex : selectedEntryIndex) {
                addEntries(entryIndex, newTable);
            }
        }
        return newTable;
    }

    private void addEntries(int rowIndex, Table newTable){
        Map<String, String> row = tbToBeExecuted.getEntry(rowIndex);
        String[] newRow = new String[selectedAttributes.size()];
        for (int k = 0; k < selectedAttributes.size(); k++) {
            newRow[k] = row.get(selectedAttributes.get(k));
        }
        newTable.addEntry(newRow);
    }


}
