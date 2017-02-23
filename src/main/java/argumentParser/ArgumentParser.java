package argumentParser;

import java.text.ParseException;
import java.util.*;

/**
 * Created by yuhuazheng on 2/19/17.
 */
public class ArgumentParser {

    // TODO: separate marshaller class to its own file
    private abstract class ArgumentMarshaller {

        public abstract void set(Iterator<String> currentArgument);
        public abstract Object get();
    }

    private class BooleanArgumentMarshaller extends ArgumentMarshaller {
        private boolean booleanValue = false;


        @Override
        public void set(Iterator<String> currentArgument){
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
        public void set(Iterator<String> currentArgument){
            stringValue = currentArgument.next();
        }

        @Override
        public Object get() {
            return stringValue;
        }
    }
    //private class IntegerArgumentMarshaller extends ArgumentMarshaller {}

    private String schema;
    private List<String> argsList;
    private Set<Character> unexpectedArgs = new TreeSet<Character>();
    private Map<Character, ArgumentMarshaller> marshalers = new HashMap<Character, ArgumentMarshaller>();
    private Set<Character> argsFound = new HashSet<Character>();

    private boolean valid;
    private Iterator<String> curArg;

    // TODO: separate errors to its own class and file
    private char errorArg = '\0';
    enum ErrorCode  {OK, MISSING_STRING};
    private ErrorCode errorCode = ErrorCode.OK;

    public ArgumentParser(String schema, String[] argsList){
        this.schema = schema;
        this.argsList = Arrays.asList(argsList);
        valid = parse();
    }

    public boolean isValid(){
        return valid;
    }

    public int cardinality(){
        return argsFound.size();
    }

    private boolean parse(){
        if(schema.length()==0 && argsList.size() == 0)
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
        for(curArg = argsList.iterator(); curArg.hasNext();){
            parseArg(curArg.next());
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
        if (m == null)
            return false;

        m.set(curArg);

        return true;
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
