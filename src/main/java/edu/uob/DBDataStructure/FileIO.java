package edu.uob.DBDataStructure;

import edu.uob.DBException.CannotCreateFileException;
import edu.uob.DBException.DBException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileIO {
    private final String EXTENSION = ".tab";
    private String filePath;
    private Table table;
    private boolean idHasNum = false;

    public FileIO(DBPath path, Table table) {
        this.table = table;
        this.filePath = path.getCurrentDatabasePath() + File.separator + table.getTableName() + EXTENSION;
    }

    public Table getTable(){
        return table;
    }

    public void setTable(Table tb){
        table = tb;
    }

    public void readTable() throws IOException{
        File fileToRead = new File(filePath);
        ArrayList<String> listOfLines = new ArrayList<>();
        FileReader reader = new FileReader(fileToRead);
        BufferedReader br = new BufferedReader(reader);
        try {
            String line = br.readLine();
            while(line!=null){
                listOfLines.add(line);
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        readAttributes(listOfLines);
        readEntries(listOfLines);
        initMaxID();
    }

    private void readAttributes(ArrayList<String> listOfLines) {
        if(!listOfLines.isEmpty()) {
            String[] attributes = listOfLines.get(0).split("\\t");
            for (String i : attributes) {
                if (i.startsWith("id")) {
                    if (i.length() > 2) {
                        idHasNum = true;
                        table.addAttribute("id");
                        table.setMaxID(Integer.parseInt(i.substring(2)));
                    } else table.addAttribute(i);
                } else table.addAttribute(i);
            }
        }
    }

    private void readEntries(ArrayList<String> listOfLines){
        for(int i=1; i<listOfLines.size(); i++){
            table.addEntry(listOfLines.get(i).split("\\t"));
        }
    }

    private void initMaxID(){
        int max = 0;
        if(!idHasNum){
            List< Map<String,String>> entries = table.getEntries();
            for(Map<String,String> row : entries){
                int current = Integer.parseInt(row.get(table.getAttributeList().get(0)));
                if(current>max) max = current;
            }
        }else {
            max = table.getMaxID();
        }
        table.setMaxID(max);
    }

    public void writeTable() throws IOException, DBException {
        File fileToOpen = new File(filePath);
        fileToOpen.createNewFile();
        if (!fileToOpen.isFile()) throw new CannotCreateFileException("file");
        FileWriter writer = new FileWriter(fileToOpen);
        BufferedWriter bw = new BufferedWriter(writer);

        writeAttribute(bw);
        writeEntries(bw);
        bw.close();
    }

    private void writeAttribute(BufferedWriter bw) throws IOException{
        for(int i=0; i<table.getNumOfAttributes(); i++){
            bw.write(table.getAttributeByIndex(i) + "\t");
        }
        bw.flush();
    }

    private void writeEntries(BufferedWriter bw) throws IOException{
        for(int i=0; i<table.getNumOfEntries(); i++){
            bw.newLine();
            Map<String,String> entry = table.getEntry(i);
            for(int j=0; j<table.getNumOfAttributes(); j++){
                String key = table.getAttributeByIndex(j);
                bw.write(entry.get(key) + "\t");
            }
        }
        bw.flush();
    }

}
