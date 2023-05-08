package edu.uob.DBEngine;

import edu.uob.DBException.DBException;
import java.util.ArrayList;

public class Tokenizer {
    private String command;
    private int currentTokenIndex = 1;
    ArrayList<String> tokens = new ArrayList<>();

    public Tokenizer(String command) {
        this.command = command;
    }

    public String getCurrentToken(){
        return tokens.get(currentTokenIndex);
    }

    public String nextToken() throws DBException {
        if(!hasMoreTokens()) throw new DBException("reach the end of token list.");
        currentTokenIndex++;
        return tokens.get(currentTokenIndex);
    }

    private boolean hasMoreTokens(){
        return (currentTokenIndex < tokens.size()-1);
    }

    public String getFirstToken() {
        splitIntoTokens();
        return tokens.get(0);
    }

    public void splitIntoTokens() {
        command = command.replaceAll(Regex.OPERATORS.getType(), " $1 ");
        command = command.replaceAll("([;,()])", " $1 ");
        command.trim().replaceAll("\\s+", " ");
        String[] Commands = command.split(" ");
        int i = 0;
        for(int j=0; j<Commands.length; j++){
            if(Commands[j].equals("") || j==i && j!=0){
            }else if(Commands[j].charAt(0) == '\''){
                for(i=j; i<Commands.length; i++){
                    if(Commands[i].endsWith("\'")){
                        break;
                    }
                }
                if(i != j) {
                    StringBuilder newString = new StringBuilder();
                    for(int m=j; m<=i && m<Commands.length; m++){
                        if(!newString.toString().equals("")){
                            newString.append(" ").append(Commands[m]);
                        }else newString.append(Commands[m]);
                    }
                    tokens.add(newString.toString());
                }else tokens.add(Commands[j]);
                j = i;
            }else if(Commands[j].equals("<") || Commands[j].equals(">")){
                if(j+1<Commands.length){
                    if(Commands[j+1].contains("=")){
                        String newString;
                        if(Commands[j+1].length() == 1) {
                            newString = Commands[j] + Commands[j + 1];
                        }else {
                            newString = Commands[j] + "=";
                            tokens.add(newString);
                            newString = Commands[j+1].substring(1);
                        }
                        tokens.add(newString);
                        j++;
                    }else tokens.add(Commands[j]);
                }
            }else if(Commands[j].contains("=")){
                int start = getStartIndex(Commands[j]);
                if(Commands[j].equals("!=")){
                    tokens.add(Commands[j]);
                }else
                if(Commands[j].contains("==")) {
                    addTokens(start, Commands[j], "==");
                }else{
                    addTokens(start, Commands[j], "=");
                }
            } else{
                tokens.add(Commands[j]);
            }
        }

    }

    private void addTokens(int start, String s, String sign){
        if(start == 0){
            tokens.add(sign);
            if(s.length()>2){
                tokens.add(s.substring(2));
            }
        }else {
            tokens.add(s.substring(0,start));
            tokens.add(sign);
            if(s.length()>start){
                tokens.add(s.substring(start+sign.length()));
            }
        }
    }

    private int getStartIndex(String s){
        for(int m=0; m<s.length(); m++){
            if(s.charAt(m) == '='){
                return m;
            }
        }
        return 0;
    }


}
