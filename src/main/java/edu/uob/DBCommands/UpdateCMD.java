package edu.uob.DBCommands;

import edu.uob.DBDataStructure.DBPath;
import edu.uob.DBDataStructure.FileIO;
import edu.uob.DBDataStructure.Table;
import edu.uob.DBException.AttributeDoesNotExistException;
import edu.uob.DBException.DBException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UpdateCMD extends DBCmd{


    public UpdateCMD(DBPath rootPath) {
        super(rootPath);
        tableNames = new ArrayList<>();
        nameValueList = new ArrayList<>();
        conditions = new Stack<>();
        operations = new Stack<>();
        tbToBeExecuted = new Table();
    }

    @Override
    public void interpretCMD() throws DBException, IOException {
        checkTableExists();
        tbToBeExecuted = dbToBeExecuted.getTable(tableNames.get(0) + EXTENSION);
        Table updatedTable = setUpdatedTableEntries(tbToBeExecuted.getAttributeList());
        updatedTable.setTableName(tableNames.get(0));

        FileIO fileIO = new FileIO(rootPath, updatedTable);
        fileIO.writeTable();
    }

    private Table setUpdatedTableEntries(List<String> attributes) throws DBException {
        List<Integer> selectedEntryIndex = getSelectedEntryIndex();
        Table newTable = tbToBeExecuted;
        for (Integer entryIndex : selectedEntryIndex) {
            for (int j = 0; j < tbToBeExecuted.getNumOfAttributes(); j++) {
                if (checkAttributeInTheList(attributes.get(j))) {
                    newTable.setCellValue(entryIndex, j, getValue(attributes.get(j)));
                }
            }
        }
        return newTable;
    }

    private String getValue(String attribute) throws DBException {
        for (Condition condition : nameValueList) {
            if (condition.getAttributeName().equals(attribute)) {
                return condition.getValue();
            }
        }
        throw new AttributeDoesNotExistException("attribute");
    }

    private Boolean checkAttributeInTheList(String attribute){
        for (Condition condition : nameValueList) {
            if (condition.getAttributeName().equals(attribute)) {
                return true;
            }
        }
        return false;
    }

}
