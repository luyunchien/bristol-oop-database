package edu.uob.DBEngine;

import java.io.Serializable;

public enum Regex implements Serializable {

    PLAINTEXT("[a-zA-Z0-9]+"),
    STRING_LITERAL("('([^\\t]*?)')"),
    STRING_UNQUOATED("([^\\t]*?)"),
    OPERATORS("(?i)(>|<|==|>=|<=|!=|LIKE)"),
    OPERATOR("(?i)(==|>=|<=|!=|LIKE)"),
    OPERATOR_2("(>|<)"),
    STRUCTURE("(?i)(DATABASE|TABLE)"),
    TABLE("(?i)(TABLE)"),
    ALTERATION_TYPE("(?i)(ADD|DROP)"),
    BOOLEAN_LITERAL("(?i)(TRUE|FALSE)"),
    FLOAT_LITERAL("[+-]?(\\d*[.]\\d+)"),
    INTEGER_LITERAL("[+-]?(\\d+)"),
    NULL("(?i)(NULL)"),
    L_PAREN("[(]"),
    R_PAREN("[)]"),
    PAREN("[(|)]"),
    COMMA("[,]"),
    SEMICOLON("[;]"),
    STAR("[*]"),
    AND("(?i)(AND)"),
    OR("(?i)(OR)"),
    ON("(?i)(ON)"),
    FROM("(?i)(FROM)"),
    WHERE("(?i)(WHERE)"),
    ID("(?i)(ID)"),
    ID_WITH_NUM("((?i)(ID)\\d*)");

    private final String regex;

    Regex(String regex){
        this.regex = regex;
    }

    public String getType(){
        return regex;
    }


}
