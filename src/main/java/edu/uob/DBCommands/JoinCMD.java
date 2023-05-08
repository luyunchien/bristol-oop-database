package edu.uob.DBCommands;

import edu.uob.DBDataStructure.DBPath;
import edu.uob.DBDataStructure.Table;
import edu.uob.DBEngine.*;
import edu.uob.DBException.AttributeDoesNotExistException;
import edu.uob.DBException.DBException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinCMD extends DBCmd{

    private Table tb1;
    private Table tb2;

    public JoinCMD(DBPath rootPath) {
        super(rootPath);
        tableNames = new ArrayList<>();
        attributeNames = new ArrayList<>();
        currentTable = new Table();
    }

    @Override
    public void interpretCMD() throws DBException, IOException {
        checkTableExists();
        checkAttributesExistForJoin();

        tb1 = dbToBeExecuted.getTable(tableNames.get(0) + EXTENSION);
        tb2 = dbToBeExecuted.getTable(tableNames.get(1) + EXTENSION);
        Map<Integer,Integer> keyPair = getKeyPair();
        getAttributeDroppedTb(tb1, 0);
        getAttributeDroppedTb(tb2, 1);

        Table joinedTable = new Table();
        List<String> newAttributes = new ArrayList<>();
        newAttributes.add("id");
        addAttributes(newAttributes, tb1);
        addAttributes(newAttributes, tb2);

        joinedTable.setAttributes(newAttributes);
        checkRepeatedAttributes(newAttributes);
        setEntriesForJoinedTb(keyPair, joinedTable);
        currentTable = joinedTable;
    }

    private List<String> getKey(int index, Table tb){
        List<String> key = new ArrayList<>();
        String attribute = attributeNames.get(index);
        for(int j=0; j<tb.getNumOfEntries(); j++){
            key.add(tb.getCellValue(j,tb.getIndexOfAttribute(attribute)));
        }
        return key;
    }

    private Map<Integer,Integer> getKeyPair(){
        List<String> key1 = getKey(0,tb1);
        List<String> key2 = getKey(1,tb2);
        Map<Integer,Integer> keyPair = new HashMap<>();
        for(int i=0; i<key1.size(); i++){
            for(int j=0; j<key2.size(); j++){
                if(key1.get(i).equals(key2.get(j))){
                    keyPair.put(i,j);
                }
            }
        }
        return keyPair;
    }

    private void getAttributeDroppedTb(Table tb, int index){
        if(attributeNames.get(index).matches(Regex.ID.getType())){
            tb.dropAttribute(attributeNames.get(index));
        }else {
            tb.dropAttribute(tb.getAttributeByIndex(0));
            tb.dropAttribute(attributeNames.get(index));
        }
    }

    private void addAttributes(List<String> newAttributes, Table tb){
        for(int i=0; i<tb.getNumOfAttributes(); i++){
            if(!tb.getAttributeByIndex(i).matches(Regex.ID.getType())){
                newAttributes.add(tb.getAttributeByIndex(i));
            }
        }
    }

    private void setEntriesForJoinedTb(Map<Integer,Integer> keyPair, Table joinedTable){
        int maxID = 1;
        for(int i=0; i<keyPair.size(); i++){
            String[] row = new String[tb1.getNumOfAttributes()+tb2.getNumOfAttributes()+1];
            row[0] = Integer.toString(maxID);
            maxID++;
            int cnt = 1;
            for(int j=0; j<tb1.getNumOfAttributes(); j++){
                row[cnt] = tb1.getCellValue(i,j);
                cnt++;
            }
            for(int k=0; k<tb2.getNumOfAttributes(); k++){
                row[cnt] = tb2.getCellValue(keyPair.get(i),k);
                cnt++;
            }
            joinedTable.addEntry(row);
        }
    }

    private void checkAttributesExistForJoin() throws DBException {
        List<String> attributes = dbToBeExecuted.getTable(tableNames.get(0)+EXTENSION).getAttributeList();
        if (!attributes.contains(attributeNames.get(0))) throw new AttributeDoesNotExistException("attribute");
        attributes = dbToBeExecuted.getTable(tableNames.get(1)+EXTENSION).getAttributeList();
        if (!attributes.contains(attributeNames.get(1))) throw new AttributeDoesNotExistException("attribute");
    }

    private void checkRepeatedAttributes(List<String> newAttributes){
        //if two attributes have the same name, the second one is marked by 2
        for(int i=0; i<newAttributes.size(); i++){
            for(int j=i+1; j<newAttributes.size(); j++){
                if(newAttributes.get(i).equals(newAttributes.get(j)) && i!=j){
                    newAttributes.remove(j);
                    newAttributes.add(j, newAttributes.get(i)+"2");
                }
            }
        }
    }

}
