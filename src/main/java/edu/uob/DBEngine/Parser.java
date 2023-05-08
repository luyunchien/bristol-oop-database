package edu.uob.DBEngine;

import edu.uob.DBCommands.*;
import edu.uob.DBDataStructure.DBPath;
import edu.uob.DBDataStructure.Database;
import edu.uob.DBException.DBException;
import edu.uob.DBException.SyntaxErrorException;

import java.io.IOException;

public class Parser {
    DBCmd cmd;
    Tokenizer tokenizer;
    Database currentDatabase;
    DBPath rootBase;

    public DBCmd parse(String command, DBPath root) throws DBException, IOException {
        rootBase = root;
        command = command.trim();
        checkSyntax(command);
        tokenizer = new Tokenizer(command);

        String firstToken = (tokenizer.getFirstToken()).toUpperCase();
        switch (firstToken) {
            case "USE" -> {
                cmd = new UseCMD(rootBase);
                parseUseCmd();
            }
            case "CREATE" -> {
                cmd = new CreateCMD(rootBase);
                parseCreateCmd();
            }
            case "DROP" -> {
                cmd = new DropCMD(rootBase);
                parseDropCmd();
            }
            case "ALTER" -> {
                cmd = new AlterCMD(rootBase);
                parseAlterCmd();
            }
            case "INSERT" -> {
                cmd = new InsertCMD(rootBase);
                parseInsertCmd();
            }
            case "SELECT" -> {
                cmd = new SelectCMD(rootBase);
                parseSelectCmd();
            }
            case "UPDATE" -> {
                cmd = new UpdateCMD(rootBase);
                parseUpdateCmd();
            }
            case "DELETE" -> {
                cmd = new DeleteCMD(rootBase);
                parseDeleteCmd();
            }
            case "JOIN" -> {
                cmd = new JoinCMD(rootBase);
                parseJoinCmd();
            }
            default -> throw new SyntaxErrorException("unspecified command");
        }
        return cmd;
    }

    private void checkCurrentDatabase() throws DBException {
        if(currentDatabase == null){
            throw new DBException("database is not selected");
        }
    }

    private void checkSyntax(String command)  throws DBException{
        if(command.isEmpty()){
            throw new DBException("The command is empty.");
        }

        if(!command.endsWith(";")){
            throw new SyntaxErrorException("there is no semicolon");
        }
    }

    private String getCurrentToken(){
        return tokenizer.getCurrentToken();
    }

    private String getNextToken() throws DBException {
        return tokenizer.nextToken();
    }

    private void parseToken(String regex, String token) throws DBException{
        if(!token.matches(regex)){
            throw new SyntaxErrorException("cannot parse the token");
        }
    }

    private void parseAttributeList() throws DBException {
        while (getNextToken().matches(Regex.COMMA.getType())){
            parseToken(Regex.PLAINTEXT.getType(), getNextToken());
            cmd.addAttributeName(getCurrentToken());
        }
    }

    private String parseValue(String token) throws DBException {
        if(token.matches(Regex.FLOAT_LITERAL.getType()) || token.matches(Regex.BOOLEAN_LITERAL.getType()) ||
                token.matches(Regex.INTEGER_LITERAL.getType()) || token.matches(Regex.NULL.getType())){
            return getCurrentToken();
        }else if(token.matches(Regex.STRING_LITERAL.getType())){
            if(!token.startsWith("'") && token.endsWith("'")){
                throw new SyntaxErrorException("the string should be quoted");
            }
            return getCurrentToken().substring(1,getCurrentToken().length()-1);
        }else throw new SyntaxErrorException("invalid value");
    }

    private void parseConditions() throws DBException{
        if(getCurrentToken().matches(Regex.PAREN.getType())){
            parseToken(Regex.PAREN.getType(), getCurrentToken());
            cmd.addOperation(getCurrentToken());
            getNextToken();
            parseConditions();
        }else if(getCurrentToken().matches("(?i)(AND|OR)")){
            cmd.addOperation(getCurrentToken().toUpperCase());
            getNextToken();
            parseConditions();
        } else if(getCurrentToken().matches(Regex.PLAINTEXT.getType())){
            parseOneCondition();
            parseConditions();
        }else if(getCurrentToken().matches(Regex.SEMICOLON.getType())){
            return;
        }
        else throw new SyntaxErrorException("invalid condition");


    }

    private void parseOneCondition() throws DBException{
        Condition condition = new Condition();
        parseToken(Regex.PLAINTEXT.getType(), getCurrentToken());
        condition.setAttributeName(getCurrentToken());

        String next = getNextToken();
        if(!next.matches(Regex.OPERATOR.getType())){
            if(next.matches(Regex.OPERATOR_2.getType())){
                parseToken(Regex.OPERATOR_2.getType(), next);
                condition.setOperator(getCurrentToken().toUpperCase());
            }else throw new SyntaxErrorException("unrecognized operator");
        }else {
            parseToken(Regex.OPERATOR.getType(), next);
            condition.setOperator(getCurrentToken().toUpperCase());
        }
        condition.setValue(parseValue(getNextToken()));
        cmd.addCondition(condition);

        cmd.addOperation(String.valueOf(cmd.getCount()));
        cmd.setCount(cmd.getCount()+1);

        getNextToken();
    }

    private void parseUseCmd() throws DBException, IOException {
        currentDatabase = new Database(rootBase);

        parseToken(Regex.PLAINTEXT.getType(),getCurrentToken());

        currentDatabase.setDatabaseName(getCurrentToken());
        currentDatabase.initializeDb(getCurrentToken());
        cmd.setDbToBeExecuted(currentDatabase);

        parseToken(Regex.SEMICOLON.getType(), getNextToken());
    }

    private void parseCreateCmd() throws DBException{
        switch (getCurrentToken().toUpperCase()) {
            case "DATABASE" -> {
                parseToken(Regex.STRUCTURE.getType(), getCurrentToken());
                cmd.setStructureType(getCurrentToken().toUpperCase());
                parseToken(Regex.PLAINTEXT.getType(), getNextToken());
                cmd.setDatabaseName(getCurrentToken());
                parseToken(Regex.SEMICOLON.getType(), getNextToken());
            }
            case "TABLE" -> {
                checkCurrentDatabase();
                cmd.setDbToBeExecuted(currentDatabase);
                parseToken(Regex.STRUCTURE.getType(), getCurrentToken());
                cmd.setStructureType(getCurrentToken().toUpperCase());
                parseToken(Regex.PLAINTEXT.getType(), getNextToken());
                cmd.addTableName(getCurrentToken());
                switch (getNextToken()) {
                    case "(" -> {
                        parseToken(Regex.L_PAREN.getType(), getCurrentToken());
                        parseToken(Regex.PLAINTEXT.getType(), getNextToken());
                        cmd.addAttributeName(getCurrentToken());
                        parseAttributeList();
                        parseToken(Regex.R_PAREN.getType(), getCurrentToken());
                        parseToken(Regex.SEMICOLON.getType(), getNextToken());
                    }
                    case ";" -> parseToken(Regex.SEMICOLON.getType(), getCurrentToken());
                    default -> throw new SyntaxErrorException("cannot create database/table");
                }
            }
            default -> throw new SyntaxErrorException("only Database or Table can be created.");
        }
    }

    private void parseDropCmd() throws DBException{
        checkCurrentDatabase();
        cmd.setDbToBeExecuted(currentDatabase);
        parseToken(Regex.STRUCTURE.getType(), getCurrentToken());
        cmd.setStructureType(getCurrentToken().toUpperCase());
        parseToken(Regex.PLAINTEXT.getType(), getNextToken());
        if(cmd.getStructureType().equals("DATABASE")){
            cmd.setDatabaseName(getCurrentToken());
        }else if(cmd.getStructureType().equals("TABLE")){
            cmd.addTableName(getCurrentToken());
        }else throw new SyntaxErrorException("invalid structure type");
        parseToken(Regex.SEMICOLON.getType(), getNextToken());
    }

    private void parseAlterCmd() throws DBException {
        checkCurrentDatabase();
        cmd.setDbToBeExecuted(currentDatabase);

        parseToken(Regex.TABLE.getType(), getCurrentToken());
        parseToken(Regex.PLAINTEXT.getType(), getNextToken());
        cmd.addTableName(getCurrentToken());


        parseToken(Regex.ALTERATION_TYPE.getType(), getNextToken());
        cmd.setAlterationType(getCurrentToken().toUpperCase());
        parseToken(Regex.PLAINTEXT.getType(), getNextToken());
        cmd.addAttributeName(getCurrentToken());
        parseToken(Regex.SEMICOLON.getType(), getNextToken());
    }

    private void parseInsertCmd() throws DBException{
        checkCurrentDatabase();
        cmd.setDbToBeExecuted(currentDatabase);
        parseToken("(?i)(INTO)", getCurrentToken());
        parseToken(Regex.PLAINTEXT.getType(), getNextToken());
        cmd.addTableName(getCurrentToken());
        parseToken("(?i)(VALUES)", getNextToken());
        parseToken(Regex.L_PAREN.getType(), getNextToken());
        parseValueList();
        parseToken(Regex.R_PAREN.getType(), getCurrentToken());
        parseToken(Regex.SEMICOLON.getType(), getNextToken());
    }

    private void parseValueList() throws DBException{
        cmd.addValue(parseValue(getNextToken()));
        while(getNextToken().matches(Regex.COMMA.getType())){
            parseToken(Regex.COMMA.getType(),getCurrentToken());
            cmd.addValue(parseValue(getNextToken()));
        }
    }

    private void parseSelectCmd() throws DBException {
        checkCurrentDatabase();
        cmd.setDbToBeExecuted(currentDatabase);
        parseWildAttribList();
        parseToken(Regex.FROM.getType(), getCurrentToken());
        parseToken(Regex.PLAINTEXT.getType(), getNextToken());
        cmd.addTableName(getCurrentToken());
        switch (getNextToken().toUpperCase()) {
            case ";" -> parseToken(Regex.SEMICOLON.getType(), getCurrentToken());
            case "WHERE" -> {
                parseToken(Regex.WHERE.getType(), getCurrentToken());
                getNextToken();
                parseConditions();
                parseToken(Regex.SEMICOLON.getType(), getCurrentToken());
            }
            default -> throw new SyntaxErrorException("cannot select table");
        }
    }

    private void parseWildAttribList() throws DBException{
        if(getCurrentToken().equals("*")){
            parseToken(Regex.STAR.getType(), getCurrentToken());
            getNextToken();
        }else if(getCurrentToken().matches(Regex.PLAINTEXT.getType())){
            cmd.addAttributeName(getCurrentToken());
            parseAttributeList();
        }else {
            throw new SyntaxErrorException("invalid WildAttribute.");
        }
    }

    private void parseUpdateCmd() throws DBException {
        checkCurrentDatabase();
        cmd.setDbToBeExecuted(currentDatabase);
        parseToken(Regex.PLAINTEXT.getType(), getCurrentToken());
        cmd.addTableName(getCurrentToken());
        parseToken("SET", getNextToken().toUpperCase());
        getNextToken();
        parseNameValueList();
        parseToken("WHERE", getCurrentToken().toUpperCase());
        getNextToken();
        parseConditions();
        parseToken(Regex.SEMICOLON.getType(), getCurrentToken());
    }

    private void parseNameValueList() throws DBException{
        parseNameValuePair();
        getNextToken();
        while(getCurrentToken().matches("[,]")){
            getNextToken();
            parseNameValueList();
        }
    }

    private void parseNameValuePair() throws DBException {
        Condition condition = new Condition();
        parseToken(Regex.PLAINTEXT.getType(), getCurrentToken());
        condition.setAttributeName(getCurrentToken());
        parseToken("=", getNextToken());
        condition.setOperator(getCurrentToken());
        condition.setValue(parseValue(getNextToken()));
        cmd.addNameValueList(condition);
    }

    private void parseDeleteCmd() throws DBException {
        checkCurrentDatabase();
        cmd.setDbToBeExecuted(currentDatabase);
        parseToken(Regex.FROM.getType(), getCurrentToken().toUpperCase());
        parseToken(Regex.PLAINTEXT.getType(), getNextToken());
        cmd.addTableName(getCurrentToken());
        parseToken(Regex.WHERE.getType(), getNextToken().toUpperCase());
        getNextToken();
        parseConditions();
        parseToken(Regex.SEMICOLON.getType(), getCurrentToken());
    }

    private void parseJoinCmd() throws DBException {
        checkCurrentDatabase();
        cmd.setDbToBeExecuted(currentDatabase);
        parseToken(Regex.PLAINTEXT.getType(), getCurrentToken());
        cmd.addTableName(getCurrentToken());
        parseToken(Regex.AND.getType(), getNextToken().toUpperCase());
        parseToken(Regex.PLAINTEXT.getType(), getNextToken());
        cmd.addTableName(getCurrentToken());
        parseToken(Regex.ON.getType(), getNextToken().toUpperCase());
        parseToken(Regex.PLAINTEXT.getType(), getNextToken());
        cmd.addAttributeName(getCurrentToken());
        parseToken(Regex.AND.getType(), getNextToken().toUpperCase());
        parseToken(Regex.PLAINTEXT.getType(), getNextToken());
        cmd.addAttributeName(getCurrentToken());
        parseToken(Regex.SEMICOLON.getType(), getNextToken());
    }

    public void updateDatabase() throws IOException {
        currentDatabase = cmd.passCurrentDBtoParser();
        currentDatabase.initializeDb(currentDatabase.getDatabaseName());
    }

}
