package argumentParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by yuhuazheng on 2/19/17.
 */
public class ArgumentParser {

    private String schema;
    private String[] args;
    private Set<Character> unexpectedArgs = new TreeSet<Character>();
    private Map<Character, Boolean> booleanArgs = new HashMap<Character, Boolean>();

    private boolean valid;
    private int numOfArgs = 0;

    public ArgumentParser(String schema, String[] args){
        this.schema = schema;
        this.args = args;
        valid = parse();
    }

    public boolean isValid(){
        return valid;
    }

    public int cardinality(){
        return numOfArgs;
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
            parseSchemaElement(schemaElement);
        }
    }

    private void parseSchemaElement(String schemaElement){
        if(schemaElement.length() == 1){
            parseBooleanSchemaElement(schemaElement);
        }
    }

    private void parseBooleanSchemaElement(String schemaElement){
        char c = schemaElement.charAt(0);
        if(Character.isLetter(c))
            setBooleanArgument(c, false);
    }

    private void setBooleanArgument(Character c, boolean b){
        booleanArgs.put(c, b);
    }

    private void parseArgs(){
        for(String arg : args){
            parseArg(arg);
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

    private void parseArgElement(Character c){
        if(isBooleanSchema(c)){
            numOfArgs++;
            setBooleanArgument(c, true);
        }
        else{
            unexpectedArgs.add(c);
        }
    }

    private boolean isBooleanSchema(Character c){
        return booleanArgs.containsKey(c);
    }

    public boolean has(Character c){
        return booleanArgs.containsKey(c);
    }

    public boolean getBoolean(Character c){
        return booleanArgs.get(c);
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
