package main;

import com.cedarsoftware.util.io.JsonReader;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Roland on 2014-03-31.
 */
public class InterpreterTest {
    @org.junit.Before
    public void setUp() throws Exception {

    }

    @org.junit.After
    public void tearDown() throws Exception {

    }

    @org.junit.Test
    public void testInterpret() throws Exception {

    }

    @org.junit.Test
    public void testTakeSimpleObject() throws Exception {
        String[] args = new String[] {"./testfiles/testTakeSimpleObject1.json", "debug"};

        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut);
        System.setOut(new PrintStream(pipeOut));

        Shrdlite.main(args);
        pipeOut.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(pipeIn));
        String hupp = br.readLine();
        String jsout= (String)((JSONObject) JSONValue.parse(hupp)).get("goals");
        assertEquals(jsout, "[(holding m)]");
    }
}
