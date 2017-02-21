package argumentParser;

import java.text.ParseException;
import java.util.*;

/**
 * Created by yuhuazheng on 2/19/17.
 */
public class ArgumentParser {

    private class ArgumentMarshaller {
        private boolean booleanValue = false;
        public void setBoolean(boolean value) {
            booleanValue = value;
        }
        public boolean getBoolean() {return booleanValue;}
    }
    private class BooleanArgumentMarshaller extends ArgumentMarshaller {
    }
    private class StringArgumentMarshaler extends ArgumentMarshaller {
    }
    private class IntegerArgumentMarshaller extends ArgumentMarshaller {
    }

    private String schema;
    private String[] args;
    private Set<Character> unexpectedArgs = new TreeSet<Character>();
    private Map<Character, ArgumentMarshaller> booleanArgs = new HashMap<Character, ArgumentMarshaller>();
    private Map<Character, String> stringArgs = new HashMap<Character, String>();
    private Set<Character> argsFound = new HashSet<Character>();

    private boolean valid;
    private int curArg;
    private char errorArg = '\0';

    enum ErrorCode  {OK, MISSING_STRING};
    private ErrorCode errorCode = ErrorCode.OK;

    public ArgumentParser(String schema, String[] args){
        this.schema = schema;
        this.args = args;
        valid = parse();
    }

    public boolean isValid(){
        return valid;
    }

    public int cardinality(){
        return argsFound.size();
    }

    private boolean parse(){
        if(schema.length()==0 && args.length == 0)
            return true;
        parseSchema();
        parseArgs();
        return unexpectedArgs.size() == 0;
    }

    private void parseSchema(){
        for(String schemaElement : schema.split(",")){
            if(schemaElement.length() > 0) {
                schemaElement = schemaElement.trim();
                parseSchemaElement(schemaElement);
            }
        }
    }

    private void parseSchemaElement(String schemaElement) {
        char elementId = schemaElement.charAt(0);
        String elementTail = schemaElement.substring(1);
        validateElementId(elementId);
        if (isBooleanSchemaElement(elementTail))
            parseBooleanSchemaElement(elementId);
        else if (isStringSchemaElement(elementTail))
            parseStringSchemaElement(elementId);
    }

    private void validateElementId(Character c) {
        if(!Character.isLetter(c)) {
            try {
                throw new ParseException("bad argument character", 0);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isBooleanSchemaElement(String elementTail) {
        return elementTail.length() == 0;
    }

    private boolean isStringSchemaElement(String elementTail) {
        return elementTail.equals("*");
    }

    private void parseBooleanSchemaElement(char c){
        booleanArgs.put(c, new BooleanArgumentMarshaller());
    }

    private void parseStringSchemaElement(char c) {
        stringArgs.put(c, "");
    }

    private void setBooleanArgument(Character c, boolean b){
        booleanArgs.get(c).setBoolean(b);
    }

    private void parseArgs(){
        for(curArg = 0; curArg < args.length; curArg++){
            parseArg(args[curArg]);
        }
    }

    private void parseArg(String arg){
        if(arg.startsWith("-"))
            parseArgElements(arg.substring(1));
    }

    private void parseArgElements(String argElements){
        for(Character c : argElements.toCharArray()){
            parseArgElement(c);
        }
    }

    private void parseArgElement(Character c) {
        if (setArgument(c))
            argsFound.add(c);
        else {
            unexpectedArgs.add(c);
            valid = false;
        }
    }

    private boolean setArgument(char c){
        boolean set = true;
        if(isBooleanSchema(c))
            setBooleanArgument(c, true);
        else if (isStringSchema(c))
            setStringArgument(c, "");
        else
            set = false;

        return set;
    }

    private boolean isBooleanSchema(Character c){
        return booleanArgs.containsKey(c);
    }

    private boolean isStringSchema(Character c){
        return stringArgs.containsKey(c);
    }

    private void setStringArgument(char c, String s){
        curArg++;
        try {
            stringArgs.put(c, args[curArg]);
        } catch (ArrayIndexOutOfBoundsException e) {
            valid = false;
            errorArg = c;
            errorCode = ErrorCode.MISSING_STRING;
        }
    }

    public boolean has(Character c){
        return argsFound.contains(c);
    }

    public boolean getBoolean(Character c){
        ArgumentMarshaller am = booleanArgs.get(c);
        return am!=null && am.getBoolean();
    }

    public String getString(char s) {
        return stringArgs.get(s);
    }


    public String errorMessage() {
        if(unexpectedArgs.size() > 0)
            return unexpectedArgsMessage();
        else
            return "";
    }

    private String unexpectedArgsMessage(){
        StringBuffer sb = new StringBuffer("unexpected arguments: ");
        for(Character c : unexpectedArgs){
            sb.append(c);
        }
        return sb.toString();
    }
}
