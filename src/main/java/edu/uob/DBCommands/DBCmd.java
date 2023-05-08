package edu.uob.DBCommands;

import edu.uob.DBDataStructure.DBPath;
import edu.uob.DBDataStructure.Database;
import edu.uob.DBDataStructure.Table;
import edu.uob.DBEngine.*;
import edu.uob.DBException.*;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class DBCmd {

    protected String databaseName;
    protected String structureType;
    protected String alterationType;
    protected List<Condition> conditions;
    protected List<String> operations;
    protected List<Condition> nameValueList;
    protected List<String> attributeNames;
    protected List<String> tableNames;
    protected List<String> valueList;
    protected Database dbToBeExecuted;
    protected final String EXTENSION = ".tab";
    protected DBPath rootPath;
    protected Table tbToBeExecuted;
    protected Table currentTable;
    private int count = 0;
    private List<List<Boolean>> conditionsResults;

    public int getCount(){return count;}

    public void setCount(int cnt){ count = cnt; }


    public DBCmd(DBPath rootPath) {
        this.rootPath = rootPath;
    }

    public void interpretCMD() throws DBException, IOException {
    }

    public void setDatabaseName(String name) {
        this.databaseName = name;
    }

    public void addTableName(String name){
        this.tableNames.add(name);
    }

    public void addAttributeName(String name){
        this.attributeNames.add(name);
    }

    public void setStructureType(String type){
        this.structureType = type;
    }

    public String getStructureType(){
        return structureType;
    }

    public void setAlterationType(String type){
        alterationType = type;
    }

    public void addValue(String value){
        valueList.add(value);
    }

    public void addCondition(Condition condition){
        conditions.add(condition);
    }

    public void addOperation(String operation) { operations.add(operation); }

    public void addNameValueList(Condition condition){
        nameValueList.add(condition);
    }

    public Table getCurrentTable(){
        return currentTable;
    }

    public void setDbToBeExecuted(Database db){
        rootPath.setCurrentDatabasePath(db.getDatabaseName());
        dbToBeExecuted = db;
    }

    protected void checkTableExists() throws DBException {
        if(tableNames == null) throw new FileDoesNotExistException("table");
        for (String t : tableNames) {
            rootPath.setCurrentTablePath(t);
            File newTableFile = new File(rootPath.getCurrentTablePath());
            if (!newTableFile.exists()) throw new FileDoesNotExistException("table");
         }
    }

    public void checkDatabaseExists() throws DBException {
        File newDBFile = new File(rootPath.getCurrentDatabasePath());
        if (!newDBFile.exists()) throw new FileDoesNotExistException("database");
    }

    protected void checkAttributesExist() throws DBException {
        List<String> attributes = dbToBeExecuted.getTable(tableNames.get(0)+EXTENSION).getAttributeList();
        for (String s : attributeNames) {
            if (!attributes.contains(s)) throw new AttributeDoesNotExistException("attribute");
        }
    }

    public Database passCurrentDBtoParser(){
        return dbToBeExecuted;
    }

    protected List<Boolean> checkConditions() throws DBException{
        if(conditionsResults==null)
            conditionsResults = getConditionsResult();
        if (operations.size() == 1) {
            return conditionsResults.get(conditionsResults.size()-1);
        }else if(checkLeftParenExists()){
            int leftIndex = getLeftParenIndex();
            checkMultipleConditions(conditionsResults, leftIndex+1);
            operations.subList(leftIndex, leftIndex+7).clear();
            operations.add(leftIndex,String.valueOf(count));
            count++;
            checkConditions();
        }else throw new SyntaxErrorException("cannot parse condition");
        return conditionsResults.get(conditionsResults.size()-1);
    }

    private void checkMultipleConditions(List<List<Boolean>> conditionsResults, int startIndex) throws DBException {
        int leftConditionIndex = Integer.parseInt(operations.get(startIndex));
        String operator = operations.get(startIndex+2);
        int rightConditionIndex = Integer.parseInt(operations.get(startIndex+4));

        List<Boolean> leftResult = conditionsResults.get(leftConditionIndex);
        List<Boolean> rightResult = conditionsResults.get(rightConditionIndex);
        if(leftResult.size()!=rightResult.size()) throw new DBException("different numbers of entry results");
        if(operator.equals("AND")){
            List<Boolean> newResult = new ArrayList<>();
            for(int i=0 ;i<leftResult.size(); i++){
                newResult.add(leftResult.get(i) && rightResult.get(i));
            }
            conditionsResults.add(newResult);
        }else if(operator.equals("OR")){
            List<Boolean> newResult = new ArrayList<>();
            for(int i=0 ;i<leftResult.size(); i++){
                newResult.add(leftResult.get(i) || rightResult.get(i));
            }
            conditionsResults.add(newResult);
        }else throw new SyntaxErrorException("only ADD or OR is allowed");

    }

    private boolean checkLeftParenExists(){
        for(String s : operations){
            if(s.equals("(")){
                return true;
            }
        }return false;
    }

    private int getLeftParenIndex() throws DBException {
        for(int i=0; i<operations.size(); i++){
            System.out.println(operations);
            if(operations.get(i).matches("(AND|OR)")){
                if(i-2>=0 && i+2<operations.size()) {
                    if (operations.get(i - 2).matches(Regex.INTEGER_LITERAL.getType()) &&
                            operations.get(i + 2).matches(Regex.INTEGER_LITERAL.getType())) {
                        return i - 3;
                    }
                }
            }
        }
        throw new SyntaxErrorException("there is no left paren");
    }

    private List<List<Boolean>> getConditionsResult() throws DBException {
        List<List<Boolean>> result = new ArrayList<>();
        for(Condition c : conditions){
            List<Boolean> oneConditionResult = checkOneCondition(c);
            result.add(oneConditionResult);
        }
        return result;
    }


    private List<Boolean> checkOneCondition(Condition condition) throws DBException {
        List<Boolean> result = new ArrayList<>();
        Table tb = dbToBeExecuted.getTable(tableNames.get(0) + EXTENSION);
        List<Map<String, String>>  entries =  tb.getEntries();
        String value = condition.getValue();
        String operator = condition.getOperator();

        for (Map<String, String> entry : entries) {
            String valueInTable = entry.get(condition.getAttributeName());
            switch (operator) {
                case "LIKE" -> {
                    value.trim();
                    if (valueInTable.contains(value)) {
                        result.add(true);
                    } else result.add(false);
                }
                case "==" -> {
                    value.trim();

                    //OR operation matches lower case of true and false
                    result.add(value.equals(valueInTable) || value.toUpperCase().equals(valueInTable));
                }
                case "!=" -> result.add(!value.equals(valueInTable));
                default -> checkOtherCondition(value, valueInTable, operator, result);
            }
        }
        return result;

    }


    private void checkOtherCondition(String value, String valueInTable, String operator, List<Boolean> result) throws DBException {
        if(checkIfIsTheSameType(value,valueInTable)) {
            switch (operator) {
                case ">=":
                    if(checkIfAreNumbers(value,valueInTable)) {
                        if (Float.parseFloat(valueInTable) >= Float.parseFloat(value)) {
                            result.add(true);
                        } else result.add(false);
                    }else {
                        if(valueInTable.compareTo(value)>=0){
                            result.add(true);
                        }else result.add(false);
                    }
                    break;
                case "<=":
                    if(checkIfAreNumbers(value,valueInTable)) {
                        if (Float.parseFloat(valueInTable) <= Float.parseFloat(value)) {
                            result.add(true);
                        } else result.add(false);
                    }else {
                        if(valueInTable.compareTo(value)<=0){
                            result.add(true);
                        }else result.add(false);
                    }
                    break;
                case "<":
                    if(checkIfAreNumbers(value,valueInTable)) {
                        if (Float.parseFloat(valueInTable) < Float.parseFloat(value)) {
                            result.add(true);
                        } else result.add(false);
                    }else {
                        if(valueInTable.compareTo(value)<0){
                            result.add(true);
                        }else result.add(false);
                    }
                    break;
                case ">":
                    if(checkIfAreNumbers(value,valueInTable)) {
                        if (Float.parseFloat(valueInTable) > Float.parseFloat(value)) {
                            result.add(true);
                        } else {
                            result.add(false);
                        }
                    }else {
                        if(valueInTable.compareTo(value)>0){
                            result.add(true);
                        }else result.add(false);
                    }
                    break;
                default:
                    throw new SyntaxErrorException("invalid operator");
            }
        }
    }


    protected List<Integer> getSelectedEntryIndex() throws DBException {
        List<Integer> selectedEntryIndex = new ArrayList<>();
        List<Boolean> result = checkConditions();
        if(result.size()!=tbToBeExecuted.getNumOfEntries()) throw new DBException("select cmd error: invalid result");
        for(int i=0; i<result.size(); i++){
            if(result.get(i)){
                selectedEntryIndex.add(i);
            }
        }
        return selectedEntryIndex;
    }

    public String convertTableToString(Table currentTable){
        StringBuilder s = new StringBuilder();
        s.append("\n");
        List<String> col = currentTable.getAttributeList();
        for(int i=0; i<currentTable.getNumOfAttributes();i++){
            s.append(col.get(i)).append("\t");
        }
        s.append("\n");
        for(int j=0; j<currentTable.getNumOfEntries();j++){
            Map<String,String> row = currentTable.getEntry(j);
            for(int i=0; i<currentTable.getNumOfAttributes(); i++){
                s.append(row.get(currentTable.getAttributeByIndex(i))).append("\t");
            }
            s.append("\n");
        }
        s.delete(s.length()-1,s.length());
        return s.toString();
    }

    private boolean checkIfIsTheSameType(String value, String valueInTable) throws DBException {
        String string = Regex.STRING_UNQUOATED.getType();
        String type_null = Regex.NULL.getType();
        String type_bool = Regex.BOOLEAN_LITERAL.getType();
        if(value.matches(type_null)){
            throw new DBException("the type of values in conditions should be the same");
        }
        if(value.matches(string)  && valueInTable.matches(string) && !checkIfIsNumber(value) && !checkIfIsNumber(valueInTable)){
            return true;
        }else if(checkIfAreNumbers(value,valueInTable)){
            return true;
        }else if(value.matches(type_bool) && value.matches(type_bool)){
            return true;
        }
        else throw new DBException("the type of values in conditions should be the same");
    }

    private boolean checkIfAreNumbers(String value, String valueInTable){
        String type_integer = Regex.INTEGER_LITERAL.getType();
        String type_float = Regex.FLOAT_LITERAL.getType();
        return (value.matches(type_integer) || value.matches(type_float)) &&
                (valueInTable.matches(type_integer) || valueInTable.matches(type_float));
    }

    private boolean checkIfIsNumber(String value){
        String type_integer = Regex.INTEGER_LITERAL.getType();
        String type_float = Regex.FLOAT_LITERAL.getType();
        return (value.matches(type_integer) || value.matches(type_float));
    }

}
