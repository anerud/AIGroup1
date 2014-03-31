package main;

import gnu.prolog.vm.PrologException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

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
    public void testTakeObject1() throws Exception {
        test("testTakeObject1", "[(holding m)]");
    }

    @org.junit.Test
    public void testTakeObject2() throws Exception {
        test("testTakeObject2", "[]");
    }

    @org.junit.Test
    public void testTakeObject3() throws Exception {
        test("testTakeObject3", "[(OR (holding l) (holding j) (holding h) (holding c))]");
    }

    @org.junit.Test
    public void testTakeObject4() throws Exception {
        test("testTakeObject4", "[(holding e)]");
    }

    @org.junit.Test
    public void testTakeObject5() throws Exception {
        test("testTakeObject5", "[(OR (holding h) (holding g))]");
    }

    @org.junit.Test
    public void testPutObject1() throws Exception {
        test("testPutObject1", "[(ONTOP e floor)]");
    }


    @org.junit.Test
    public void testPutObject2() throws Exception {
        test("testPutObject2", "[(UNDER e f)]");
    }

    @org.junit.Test
    public void testMoveObject1() throws Exception {
        test("testMoveObject1", "[(INSIDE e (ONTOP k floor))]");
    }

    private void test(String file, String result) throws ParseException, IOException, PrologException {
        String[] args = new String[] {"testfiles/" + file + ".json", "debug"};

        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut);
        System.setOut(new PrintStream(pipeOut));

        Shrdlite.main(args);
        pipeOut.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(pipeIn));
        String hupp = br.readLine();
        String jsout= (String)((JSONObject) JSONValue.parse(hupp)).get("goals");
        assertEquals(jsout, result);
    }




}
