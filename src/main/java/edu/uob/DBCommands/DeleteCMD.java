package edu.uob.DBCommands;

import edu.uob.DBDataStructure.DBPath;
import edu.uob.DBDataStructure.FileIO;
import edu.uob.DBDataStructure.Table;
import edu.uob.DBException.DBException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DeleteCMD extends DBCmd{


    public DeleteCMD(DBPath rootPath) {
        super(rootPath);
        tableNames = new ArrayList<>();
        conditions = new Stack<>();
        operations = new Stack<>();
        tbToBeExecuted = new Table();
    }

    @Override
    public void interpretCMD() throws DBException, IOException {
        checkTableExists();
        tbToBeExecuted = dbToBeExecuted.getTable(tableNames.get(0) + EXTENSION);

        Table deletedTable = tbToBeExecuted;
        deletedTable.setTableName(tableNames.get(0));
        deletedTable = setDeletedTableEntries();
        deletedTable.updateMaxID();

        FileIO fileIO = new FileIO(rootPath, deletedTable);
        fileIO.writeTable();
        dbToBeExecuted.getTable(deletedTable.getTableName()+ EXTENSION).setMaxID(deletedTable.getMaxID());
        tbToBeExecuted = dbToBeExecuted.getTable(deletedTable.getTableName()+ EXTENSION);
    }

    private Table setDeletedTableEntries() throws DBException {
        Table newTable = tbToBeExecuted;
        List<Integer> selectedEntryIndex = getSelectedEntryIndex();
        for(int i=selectedEntryIndex.size()-1; i>=0; i--){
            newTable.dropEntry(selectedEntryIndex.get(i));
        }

        return newTable;
    }

}
