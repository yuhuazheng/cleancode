package argumentParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by yuhuazheng on 2/19/17.
 */
public class generalTest {
    @Test
    public void testBooleanSchema() throws Exception {
        String schema = "b";
        String[] args = {"-b"};
        ArgumentParser argumentParser =  new ArgumentParser(schema, args);
        assertEquals(true, argumentParser.has('b'));
        assertEquals(true, argumentParser.getBoolean('b'));
    }

    @Test
    public void testUnexpectedBoolean() throws Exception {
        String schema = "b";
        String[] args = {"-c"};
        ArgumentParser argumentParser =  new ArgumentParser(schema, args);
        assertEquals(true, argumentParser.has('b'));
        assertEquals(false, argumentParser.getBoolean('b'));
        assertEquals(true, argumentParser.errorMessage().contains("c"));
    }

    @Test
    public void testString() throws Exception {
        assertEquals(true, true);
    }
}
