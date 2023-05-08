package edu.uob.DBCommands;

import edu.uob.DBDataStructure.*;
import edu.uob.DBException.DBException;
import edu.uob.DBException.SyntaxErrorException;

import java.io.IOException;
import java.util.ArrayList;

public class InsertCMD extends DBCmd{


    public InsertCMD(DBPath rootPath) {
        super(rootPath);
        tableNames = new ArrayList<>();
        valueList = new ArrayList<>();
        tbToBeExecuted = new Table();
    }

    @Override
    public void interpretCMD() throws DBException, IOException {
        checkTableExists();
        tbToBeExecuted = dbToBeExecuted.getTable(tableNames.get(0) + EXTENSION);
        getEntriesForInsertedTb();
        tbToBeExecuted.updateMaxID();
        FileIO fileIO = new FileIO(rootPath, tbToBeExecuted);
        fileIO.writeTable();
    }

    private void getEntriesForInsertedTb() throws DBException {
        if(tbToBeExecuted.getNumOfAttributes()!=valueList.size()+1){
            throw new SyntaxErrorException("the number of values should be the same as that of attributes");
        }
        String[] entry = new String[valueList.size() + 1];
        tbToBeExecuted.setMaxID(tbToBeExecuted.getMaxID()+1);
        entry[0] = Integer.toString(tbToBeExecuted.getMaxID());

        int i=1;
        for (String s : valueList) {
            entry[i] = s;
            i++;
        }
        if(i!=valueList.size()+1) throw new DBException("The number of values must be the number of attributes");
        tbToBeExecuted.addEntry(entry);
    }
}
