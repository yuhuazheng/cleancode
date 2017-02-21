package argumentParser;

import java.text.ParseException;
import java.util.*;

/**
 * Created by yuhuazheng on 2/19/17.
 */
public class ArgumentParser {

    private abstract class ArgumentMarshaller {

        public abstract void set(String s);
        public abstract Object get();
    }

    private class BooleanArgumentMarshaller extends ArgumentMarshaller {
        private boolean booleanValue = false;

        @Override
        public void set(String s) {
            booleanValue = true;
        }

        @Override
        public Object get() {
            return booleanValue;
        }
    }

    private class StringArgumentMarshaler extends ArgumentMarshaller {
        private String stringValue = "";

        @Override
        public void set(String s) {
            stringValue = s;
        }

        @Override
        public Object get() {
            return stringValue;
        }
    }
    //private class IntegerArgumentMarshaller extends ArgumentMarshaller {}

    private String schema;
    private String[] args;
    private Set<Character> unexpectedArgs = new TreeSet<Character>();
    private Map<Character, ArgumentMarshaller> marshalers = new HashMap<Character, ArgumentMarshaller>();
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
            marshalers.put(elementId, new BooleanArgumentMarshaller());
        else if (isStringSchemaElement(elementTail))
            marshalers.put(elementId, new StringArgumentMarshaler());
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
        ArgumentMarshaller m  = marshalers.get(c);
        if(m instanceof BooleanArgumentMarshaller)
            setBooleanArg(m);
        else if (m instanceof StringArgumentMarshaler)
            setStringArg(m);
        else
            return false;

        return true;
    }

    private void setBooleanArg(ArgumentMarshaller m){
        m.set("true");
    }

    private void setStringArg(ArgumentMarshaller m){
        curArg++;
        try {
            m.set(args[curArg]);
        } catch (ArrayIndexOutOfBoundsException e) {
            valid = false;
            errorCode = ErrorCode.MISSING_STRING;
        }
    }

    public boolean has(Character c){
        return argsFound.contains(c);
    }

    public boolean getBoolean(Character c){
        ArgumentMarshaller am = marshalers.get(c);
        boolean b = false;
        try {
            b = am != null && (Boolean) am.get();
        } catch (ClassCastException e) {
            b = false;
        }
        return b;
    }

    public String getString(char s) {
        ArgumentMarshaller am = marshalers.get(s);
        try {
            return am == null ? "" : (String) am.get();
        } catch (ClassCastException e) {
            return "";
        }
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
