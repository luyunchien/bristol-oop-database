package edu.uob.DBDataStructure;

import edu.uob.DBEngine.Regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {
    private String name;
    private Integer maxID;
    private List<String> attributes;
    private List<Map<String, String>> data;

    public Table() {
        maxID = 0;
        attributes = new ArrayList<>();
        data = new ArrayList<>();
    }

    public void setTableName(String name){
        this.name = name;
    }

    public String getTableName(){
        return name;
    }

    public int getNumOfEntries(){
        return data.size();
    }

    public int getNumOfAttributes(){
        return attributes.size();
    }

    public String getAttributeByIndex(int index){
        return attributes.get(index);
    }

    public List<String> getAttributeList(){
        return attributes;
    }

    public int getIndexOfAttribute(String attributeName){
        return attributes.indexOf(attributeName);
    }

    public boolean checkIfAttributeExists(String attributeName){
        return attributes.contains(attributeName);
    }

    public int getMaxID(){
        return maxID;
    }

    public void setMaxID(int max){
        this.maxID = max;
    }

    public Map<String, String> getEntry(int index){
        return data.get(index);
    }

    public void addAttribute(String attributeName){
        attributes.add(attributeName);
    }

    public void setAttributes(List<String> list){
        attributes = list;
    }

    public void dropAttribute(String attributeName){
        if(attributes.contains(attributeName)) {
            int index = attributes.indexOf(attributeName);
            for(int i=0; i<data.size(); i++){
                data.get(i).remove(attributes.get(index));
            }
            attributes.remove(attributeName);
        }
    }

    public void addEntry(String[] entry){
        if(attributes.size() == entry.length) {
            Map<String, String> row = new HashMap<>();
            for (int i = 0; i < attributes.size(); i++) {
                row.put(attributes.get(i),entry[i]);
            }
            data.add(row);
        }
    }

    public void dropEntry(int rowIndex){
        data.remove(rowIndex);
    }

    public String getCellValue(int rowIndex, int colIndex){
        String key = attributes.get(colIndex);
        return data.get(rowIndex).get(key);
    }

    public void setCellValue(int rowIndex, int colIndex, String value){
        String key = attributes.get(colIndex);
        data.get(rowIndex).put(key,value);
    }

    public void printTable(){
        for(String attribute : attributes){
            if(attribute.matches(Regex.ID_WITH_NUM.getType())){
                System.out.print("id" + "\t");
            }
            else System.out.print(attribute + "\t");
        }
        System.out.println();

        for(int j=0; j<data.size(); j++) {
            for (int i = 0; i < attributes.size(); i++) {
                System.out.print(data.get(j).get(attributes.get(i)) + "\t");
            }
            System.out.println();
        }
    }

    public List<Map<String, String>> getEntries(){
        return data;
    }

    public void updateMaxID(){
        int max = getMaxID();
        List< Map<String,String>> entries = getEntries();
        String[] id_col = new String[entries.size()];
        int cnt = 0;
        for(Map<String,String> row : entries){
            int current = Integer.parseInt(row.get(attributes.get(0)));
            if(current>max) max = current;
            id_col[cnt] = row.get(attributes.get(0));
            cnt++;
        }
        setMaxID(max);
        List<String> attribute = getAttributeList();
        String newID = null;

        int index=0;
        for(int i=0; i<attribute.size(); i++){
            if(attribute.get(i).matches(Regex.ID.getType())){
                newID = "id" + max;
                index = i;
            }
        }
        attribute.remove(index);
        attribute.add(index,newID);
        attributes = attribute;
        for(int i=0; i<data.size();i++){
            entries.get(i).put(newID,id_col[i]);
        }

    }


}
