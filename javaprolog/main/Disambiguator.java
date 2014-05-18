package main;

import tree.NTree;
import tree.Node;
import world.WorldObject;

import java.util.*;

import main.Interpreter.ClarificationQuestionException;


/**
 * @author Joel
 *
 */
public class Disambiguator {


    final private static String[] units = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight",
            "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen",
            "nineteen" };
    final private static String[] tens = { "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty",
            "ninety" };


    public static String disambiguate(List<NTree> trees)
    {


        StringBuilder sb = new StringBuilder();

        Iterator<NTree> i= trees.iterator();

        boolean first = true;
        while (i.hasNext()) {
            String d = i.next().toNaturalString();
            if (!i.hasNext()) {
                sb.append(" or ");
                sb.append(d);
            } else {
                if (!first)
                    sb.append(", ");
                sb.append(d);
                first = false;
            }
        }
        sb.append("?");
        return sb.toString();


    }

    /**
     * Generate a clarification question that disambiguates an ambiguous
     * reference, given the reference and the objects it matched
     Example: the user may say: "take the large object" . If there are three
     large objects, the method produces a question like, "I can see
     three large objects. Did you mean the box, the white ball, or the red
     ball?
     * @param objects The different objects that the reference may refer to.
     * @param reference The reference that was ambiguous.
     * @return A natural language clarification question.
     */
    public static String disambiguate(Set<WorldObject> objects, Node reference)
    {

// put alternatives in sorted list for nicer enumeration
        List<WorldObject> sortedObjs = new LinkedList<WorldObject>(objects);

// make comparator to sort list
        Comparator<WorldObject> worldObjectComparator = new Comparator<WorldObject>() {
            @Override
            public int compare(WorldObject o1, WorldObject o2) {
                return 100 * o1.getForm().compareTo(o2.getForm()) + 10 * o1.getColor().compareTo(o2.getColor())
                        + o1.getSize().compareTo(o2.getSize());
            }
        };

        Collections.sort(sortedObjs, worldObjectComparator);

        StringBuilder sb = new StringBuilder();

        int count = sortedObjs.size();
        //String iCanSee = "I can see " + int2String(count) + " " + reference.toNaturalString(true);
        //sb.append(iCanSee);
        //sb.append(". Did you mean ");

        Iterator<WorldObject> i = sortedObjs.iterator();

// produce minimal unique descriptions, separated by commas and "or"
        boolean first = true;
        while (i.hasNext()) {
            String d = minimalUniqueDescription(i.next(), new HashSet<WorldObject>(objects));
            if (d.equals("error")) {
               // return iCanSee + ". Please be more specific.";
                return  "Please be more specific.";
            }
            if (!i.hasNext()) {
                sb.append(" or ");
                sb.append(d);
            } else {
                if (!first)
                    sb.append(", ");
                sb.append(d);
                first = false;
            }
        }
        sb.append("?");
        return sb.toString();
    }


    /** Generate a description of an object. The description is minimal, but sufficient to distinguish
     * it from all other objects in the given set. <br><br>
     *
     * Example: Returns "the ball" if the object is a ball, and no there object in the set is a ball. <br>
     * Example: Returns "the large red ball" if there are other large balls and red balls in the set, but no large red one. <br>
     *
     * Limitations: Does not use spatial relationships to distinguish objects. Only intrinsic properties are considered
     * Returns the string "error" if the object could not be uniquely described.
     *
     * @param specialObject The object to be described
     * @param theOthers The set of objects from which to distinguish specialObject from. If specialObject itself is included, it is ignored.
     * @param definiteArticle Optional, defines whether to use the definite article ("the ball") or not ("a ball").
     * @return A string describing the specialObject
     */
    public static String minimalUniqueDiscription(WorldObject specialObject, Set<WorldObject> theOthers, boolean definiteArticle) {

        String article = definiteArticle ? "the " : "a ";
        theOthers = new HashSet<WorldObject>(theOthers); // do not modify input!
        theOthers.remove(specialObject);
        Set<WorldObject> removeThese = new HashSet<WorldObject>();

// check if type is unique
        for (WorldObject other : theOthers)
            if (!specialObject.getForm().equals(other.getForm()))
                removeThese.add(other);
        theOthers.removeAll(removeThese);
        removeThese.clear();

        if (theOthers.isEmpty())
            return article + specialObject.getForm();

// form was not unique, check if size is unique
        for (WorldObject other : theOthers)
            if (!specialObject.getSize().equals(other.getSize()))
                removeThese.add(other);
        theOthers.removeAll(removeThese);

        if (theOthers.isEmpty())
            return article + specialObject.getSize() + " " + specialObject.getForm();


// put back the ones with different size, check for unique color
        theOthers.addAll(removeThese);
        removeThese.clear();
        for (WorldObject other : theOthers)
            if (!specialObject.getColor().equals(other.getColor()))
                removeThese.add(other);
        theOthers.removeAll(removeThese);

        if (theOthers.isEmpty())
            return article + specialObject.getColor() + " " + specialObject.getForm();

// check for uniqe dscription using all attributes
        theOthers.addAll(removeThese);
        removeThese.clear();
        for (WorldObject other : theOthers)
        {
            if (!specialObject.getColor().equals(other.getColor()))
                removeThese.add(other);
            if (!specialObject.getSize().equals(other.getSize()))
                removeThese.add(other);
        }
        theOthers.removeAll(removeThese);


        if (theOthers.isEmpty())
            return article +" "+ specialObject.getSize() + " " + specialObject.getColor() + " " + specialObject.getForm();


// if there are exactly two identical objects, they can be disambiguated
// by relative position.
// if there are more than two identical objects, it is not possible to
// uniquely refer to any one of them

// if (theOthers.size() > 1)
// return "error";

// todo: disambiguate using relative position in the world
        return "error";

// todo: disambiguate using realtive position in the world
// return "a " + obj.getSize() + " " + obj.getColor() + " "

// natural language representation of integer quantities
    }

    public static String minimalUniqueDescription(WorldObject obj, Set<WorldObject> theOthers) {
        return minimalUniqueDiscription(obj, theOthers, true);

    }



    /** A natural language representation of an integer quantity, such as "two" or "one hundred and one"
     * @param i The quantity
     * @return The String representation
     */
    public static String int2String(int i) {
        if( i < 20) return units[i];
        if( i < 100) return tens[i/10] + ((i % 10 > 0)? " " + int2String(i % 10):"");
        if( i < 1000) return units[i/100] + " Hundred" + ((i % 100 > 0)?" and " + int2String(i % 100):"");
        if( i < 1000000) return int2String(i / 1000) + " Thousand " + ((i % 1000 > 0)? " " + int2String(i % 1000):"") ;
        return int2String(i / 1000000) + " Million " + ((i % 1000000 > 0)? " " + int2String(i % 1000000):"") ;
    }

}