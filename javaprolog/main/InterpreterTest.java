package main;

import aStar.WorldState;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import gnu.prolog.vm.PrologException;
import world.WorldObject;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Roland on 2014-03-31.
 */
public class InterpreterTest {
    @org.junit.Before
    public void setUp() throws Exception {

    }

    @org.junit.After
    public void tearDown() throws Exception {
        //WorldState.setVisitedWorld(new HashSet<String>()); //Remove this to speed up the process..
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
        String[] alternatives = new String[] {"[(OR (holding l) (holding j) (holding h) (holding c))]", "[(OR (holding l) (holding j) (holding c) (holding h))]"};
        test("testTakeObject3", alternatives);
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
    public void testTakeObject6() throws Exception {
        test("testTakeObject6", "[]");
    }

    @org.junit.Test
    public void testTakeObject7() throws Exception {
        String[] alternatives = new String[factorial(3)];
        String[] ids = {"l", "e", "a"};

        Set<LinkedList<String>> list = new HashSet<LinkedList<String>>(permutations(new LinkedList<String>(Arrays.asList(ids)), ""));
        int iter = 0;
        for(LinkedList<String> l : list){
            Iterator<String> it = l.iterator();
            alternatives[iter] = "[(OR (holding " + it.next() + ") (holding " + it.next() + ") (holding " + it.next() + "))]";
            iter++;
        }
        test("testTakeObject7", alternatives);
    }

    @org.junit.Test
    public void testTakeObject8() throws Exception {
        test("testTakeObject8", "[(holding b)]");
    }

    @org.junit.Test
    public void testPutObject1() throws Exception {
        test("testPutObject1", "[(ONTOP e floor)]");
    }

    @org.junit.Test
    public void testPutObject2() throws Exception {
        test("testPutObject2", "[]");
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
        String[] alternatives = new String[] {"[(OR (INSIDE a l) (INSIDE a k))]", "[(OR (INSIDE a k) (INSIDE a l))]"};
        test("testMoveObject3", alternatives);
    }

    @org.junit.Test
    public void testMoveObject4() throws Exception {
        String[] alternatives = new String[] {"[(OR (INSIDE e (ONTOP l floor)) (INSIDE e (ONTOP k floor)))]", "[(OR (INSIDE e (ONTOP k floor)) (INSIDE e (ONTOP l floor)))]"};
        test("testMoveObject4", alternatives);
    }

    @org.junit.Test
    public void testMoveObject5() throws Exception {
        String[] alternatives = new String[] {"[]"};
        test("testMoveObject5", alternatives);
    }

//    @org.junit.Test
//    public void testMoveObject6() throws Exception {
//        String[] alternatives = new String[factorial(4)];
//        String[] ids = {"a g", "b h", "b g", "a h"};
//
//        Set<LinkedList<String>> list = new HashSet<LinkedList<String>>(permutations(new LinkedList<String>(Arrays.asList(ids)), ""));
//        int iter = 0;
//        for(LinkedList<String> l : list){
//            Iterator<String> it = l.iterator();
//            alternatives[iter] = "[(AND (ONTOP " + it.next() + ") (ONTOP " + it.next() + ") (ONTOP " + it.next() + ") (ONTOP " + it.next() + "))]";
//            iter++;
//        }
//        test("testMoveObject6", alternatives);
//    }

    @org.junit.Test
    public void testMoveObject6() throws Exception {
        String[] alternatives = new String[] {"[(AND (ONTOP a g) (OR (ONTOP b h) (ONTOP b g)))]", "[(AND (ONTOP a g) (OR (ONTOP b g) (ONTOP b h)))]",
                "[(AND (OR (ONTOP b h) (ONTOP b g)) (ONTOP a g))]", "[(AND (OR (ONTOP b g) (ONTOP b h)) (ONTOP a g))]"};
        test("testMoveObject6", alternatives);
    }


    @org.junit.Test
    public void testMoveObject7() throws Exception {
        String[] alternatives = new String[] {"[(AND (OR (INSIDE e k) (INSIDE e l)) (OR (INSIDE f l) (INSIDE f k)))]", "[(AND (OR (INSIDE e k) (INSIDE e l)) (OR (INSIDE f k) (INSIDE f l)))]",
                "[(AND (OR (INSIDE e l) (INSIDE e k)) (OR (INSIDE f l) (INSIDE f k)))]", "[(AND (OR (INSIDE e l) (INSIDE e k)) (OR (INSIDE f k) (INSIDE f l)))]",
                "[(AND (OR (INSIDE f l) (INSIDE f k)) (OR (INSIDE e l) (INSIDE e k)))]", "[(AND (OR (INSIDE f l) (INSIDE f k)) (OR (INSIDE e k) (INSIDE e l)))]",
                "[(AND (OR (INSIDE f k) (INSIDE f l)) (OR (INSIDE e l) (INSIDE e k)))]", "[(AND (OR (INSIDE f k) (INSIDE f l)) (OR (INSIDE e k) (INSIDE e l)))]"};
        test("testMoveObject7", alternatives);
    }

    @org.junit.Test
    public void testMoveObject8() throws Exception {
        test("testMoveObject8", "[]");
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

    private void test(String file, String[] alternatives) throws IOException, PrologException, JsonSyntaxException {
        String[] args = new String[] {"testfiles/" + file + ".json", "debug"};

        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut);
        System.setOut(new PrintStream(pipeOut));

        Shrdlite.main(args);
        pipeOut.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(pipeIn));
        String hupp = br.readLine();
        String jsout= new Gson().fromJson(hupp, Input.class).getGoals();

        boolean isis = false;
        for(String s : alternatives){
            if(jsout.equals(s)){
                isis = true;
                break;
            }
        }
        assertTrue(isis);
        pipeIn.close();
    }

    private void test(String file, String result) throws JsonSyntaxException, IOException, PrologException {
        String[] args = new String[] {"testfiles/" + file + ".json", "debug"};

        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut);
        System.setOut(new PrintStream(pipeOut));

        Shrdlite.main(args);
        pipeOut.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(pipeIn));
        String hupp = br.readLine();
        String jsout= new Gson().fromJson(hupp, Input.class).getGoals();
        assertEquals(jsout, result);
        pipeIn.close();
    }
}
