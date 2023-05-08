package edu.uob.DBCommands;

public class Condition {
    private String attributeName;
    private String operator;
    private String value;

    public void setAttributeName(String name) {
         this.attributeName = name;
    }

    public void setOperator(String op) {
        this.operator = op;
    }

    public void setValue(String value){
        this.value = value;
    }

    protected String getAttributeName(){
        return attributeName;
    }

    protected String getOperator(){
        return operator;
    }

    protected String getValue(){
        return value;
    }
}
