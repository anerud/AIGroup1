package main;

import gnu.prolog.vm.PrologException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

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

    @org.junit.Test
    public void testMoveObject2() throws Exception {
        String[] alternatives = new String[] {"[(AND (ONTOP m floor) (ONTOP k floor) (ONTOP l floor))]", "[(AND (ONTOP m floor) (ONTOP l floor) (ONTOP k floor))]", "[(AND (ONTOP k floor) (ONTOP m floor) (ONTOP l floor))]",
                "[(AND (ONTOP k floor) (ONTOP l floor) (ONTOP m floor))]", "[(AND (ONTOP l floor) (ONTOP m floor) (ONTOP k floor))]", "[(AND (ONTOP l floor) (ONTOP k floor) (ONTOP m floor))]"};
        test("testMoveObject2", alternatives);
    }

    @org.junit.Test
    public void testMoveObject3() throws Exception {
        String[] alternatives = new String[] {"[(OR (INSIDE a m) (INSIDE a l) (INSIDE a k))]", "[(OR (INSIDE a m) (INSIDE a k) (INSIDE a l))]", "[(OR (INSIDE a l) (INSIDE a m) (INSIDE a k))]", "[(OR (INSIDE a l) (INSIDE a k) (INSIDE a m))]",
                "[(OR (INSIDE a k) (INSIDE a l) (INSIDE a m))]", "[(OR (INSIDE a k) (INSIDE a m) (INSIDE a l))]"};
        test("testMoveObject3", alternatives);
    }

    @org.junit.Test
    public void testMoveObject4() throws Exception {
        String[] alternatives = new String[] {"[(OR (INSIDE e (ONTOP m floor)) (INSIDE e (ONTOP l floor)) (INSIDE e (ONTOP k floor)))]", "[(OR (INSIDE e (ONTOP m floor)) (INSIDE e (ONTOP k floor)) (INSIDE e (ONTOP l floor)))]",
                "[(OR (INSIDE e (ONTOP l floor)) (INSIDE e (ONTOP m floor)) (INSIDE e (ONTOP k floor)))]", "[(OR (INSIDE e (ONTOP l floor)) (INSIDE e (ONTOP k floor)) (INSIDE e (ONTOP m floor)))]",
                "[(OR (INSIDE e (ONTOP k floor)) (INSIDE e (ONTOP l floor)) (INSIDE e (ONTOP m floor)))]", "[(OR (INSIDE e (ONTOP k floor)) (INSIDE e (ONTOP m floor)) (INSIDE e (ONTOP l floor)))]"};
        test("testMoveObject4", alternatives);
    }

    @org.junit.Test
    public void testMoveObject5() throws Exception {
        String[] alternatives = new String[] {"[]"};
        test("testMoveObject5", alternatives);
    }


    @org.junit.Test
    public void testMoveObject6() throws Exception {
        String[] alternatives = new String[factorial(4)];
        String[] ids = {"a g", "b h", "b g", "a h"};

        Set<LinkedList<String>> list = new HashSet<LinkedList<String>>(permutations(new LinkedList<String>(Arrays.asList(ids)), ""));
        int iter = 0;
        for(LinkedList<String> l : list){
            Iterator<String> it = l.iterator();
            alternatives[iter] = "[(AND (ONTOP " + it.next() + ") (ONTOP " + it.next() + ") (ONTOP " + it.next() + ") (ONTOP " + it.next() + "))]";
            iter++;
        }
        test("testMoveObject6", alternatives);
    }


    @org.junit.Test
    public void testMoveObject7() throws Exception {
        String[] alternatives = new String[] {"[(OR (AND (INSIDE e k) (INSIDE f l)) (AND (INSIDE e l) (INSIDE f k)))]", "[(OR (AND (INSIDE e k) (INSIDE f l)) (AND (INSIDE f k) (INSIDE e l)))]",
                "[(OR (AND (INSIDE f l) (INSIDE e k)) (AND (INSIDE e l) (INSIDE f k)))]", "[(OR (AND (INSIDE f l) (INSIDE e k)) (AND (INSIDE f k) (INSIDE e l)))]",
                "[(OR (AND (INSIDE e l) (INSIDE f k)) (AND (INSIDE f l) (INSIDE e k)))]", "[(OR (AND (INSIDE e l) (INSIDE f k)) (AND (INSIDE e k) (INSIDE f l)))]",
                "[(OR (AND (INSIDE f k) (INSIDE e l)) (AND (INSIDE f l) (INSIDE e k)))]", "[(OR (AND (INSIDE f k) (INSIDE e l)) (AND (INSIDE e k) (INSIDE f l)))]"};
        test("testMoveObject7", alternatives);
    }


    private Set<LinkedList<String>> permutations(LinkedList<String> strings, String first){
        Set<LinkedList<String>> perms = new HashSet<LinkedList<String>>();
        for(String s : strings){
            LinkedList<String> stringsWithout = new LinkedList<String>(strings);
            stringsWithout.remove(s);
            Set<LinkedList<String>> permsWithout = null;
            if(stringsWithout.size() == 1){
                permsWithout = new HashSet<LinkedList<String>>();
                stringsWithout.addFirst(s);
                permsWithout.add(stringsWithout);
            } else {
                permsWithout = permutations(stringsWithout, s);
            }
            if(!first.equals("")){
                for(LinkedList<String> l : permsWithout){
                    l.addFirst(first);
                }
            }
            perms.addAll(permsWithout);
        }
        return perms;
    }

    private int factorial(int number){
        int fac = 1;
        for(int i = 1; i <= number; i++){
            fac *= i;
        }
        return fac;
    }


    private void test(String file, String[] alternatives) throws IOException, PrologException, ParseException {
        String[] args = new String[] {"testfiles/" + file + ".json", "debug"};

        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut);
        System.setOut(new PrintStream(pipeOut));

        Shrdlite.main(args);
        pipeOut.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(pipeIn));
        String hupp = br.readLine();
        String jsout= (String)((JSONObject) JSONValue.parse(hupp)).get("goals");

        boolean isis = false;
        for(String s : alternatives){
            if(jsout.equals(s)){
                isis = true;
                break;
            }
        }
        assertTrue(isis);
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