package main;

import tree.NTree;
import tree.Node;
import world.WorldObject;

import java.util.*;

import main.Interpreter.AmbiguousReferenceException;

public class Disambiguator {
	private String message;

	final private static String[] units = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight",
			"nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen",
			"nineteen" };
	final private static String[] tens = { "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty",
			"ninety" };

	// author: joel

	// returns a unique world object from a set of worldObjects using
	// natural language clarification questions. The original user reference
	// that was ambiguous is used to form the correct question for the context.
	// The possible objects are described with no redundant information. Only
	// features that are needed to discriminate between the possible objects are
	// mentioned.

	// Example: the user may say: "take the large object" . if there are three
	// large objects, the disambiguator produces a question like, "I can see
	// three large objects. Did you mean the box, the white ball, or the red
	// ball?

	public static String disambiguate(Set<WorldObject> objs, Node n)
	{
		
		// put alternatives in sorted list for nicer enumeration
		List<WorldObject> sortedObjs = new LinkedList<WorldObject>(objs);

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
		String iCanSee = "I can see " + int2String(count) + " " + n.toNaturalString(true);
		sb.append(iCanSee);
		sb.append(". Did you mean ");

		Iterator<WorldObject> i = sortedObjs.iterator();

		// produce minimal unique descriptions, separated by commas and "or"
		boolean first = true;
		while (i.hasNext()) {
			String d = minimalUniqueDiscription(i.next(), new HashSet<WorldObject>(objs));
			if (d.equals("error")) {
				return iCanSee + ". Please be more specific.";
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

		// System.out.println(sb.toString());
		// not immplemented
		return sb.toString();
	}

	public String getMessage() {
		return message;
	}

	// returns a description of a WorldObject, that is minimal but sufficient to
	// distinguish it from
	// all other WorldObjects in the given set.
	public static String minimalUniqueDiscription(WorldObject obj, Set<WorldObject> theOthers) {
		return minimalUniqueDiscription(obj, theOthers, true);

	}

	public static String minimalUniqueDiscription(WorldObject obj, Set<WorldObject> theOthers, boolean definiteArticle) {

		String article = definiteArticle ? "the " : "a ";
		theOthers = new HashSet<WorldObject>(theOthers); // do not modify input!
		theOthers.remove(obj);
		Set<WorldObject> removeThese = new HashSet<WorldObject>();

		// check if type is unique
		for (WorldObject other : theOthers)
			if (!obj.getForm().equals(other.getForm()))
				removeThese.add(other);
		theOthers.removeAll(removeThese);
		removeThese.clear();

		if (theOthers.isEmpty())
			return article + obj.getForm();

		// form was not unique, check if size is unique
		for (WorldObject other : theOthers)
			if (!obj.getSize().equals(other.getSize()))
				removeThese.add(other);
		theOthers.removeAll(removeThese);
		
		if (theOthers.isEmpty())
			return article + obj.getSize() + " " + obj.getForm();
		
        
		// put back the ones with different size, check for unique color
		theOthers.addAll(removeThese);
		removeThese.clear();
		for (WorldObject other : theOthers)
			if (!obj.getColor().equals(other.getColor()))
				removeThese.add(other);
		theOthers.removeAll(removeThese);

		if (theOthers.isEmpty())
			return article + obj.getColor() + " " + obj.getForm();
		
		// check for uniqe dscription using all attributes
		theOthers.addAll(removeThese);
		removeThese.clear();
		for (WorldObject other : theOthers)
		{
			if (!obj.getColor().equals(other.getColor()))
				removeThese.add(other);
			if (!obj.getSize().equals(other.getSize()))
				removeThese.add(other);
		}
		theOthers.removeAll(removeThese);
         
		
		if (theOthers.isEmpty())
			return article +" "+ obj.getSize() + " " + obj.getColor() + " " + obj.getForm();

	
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

	public static String int2String(int i) {
		if( i < 20)  return units[i];
		if( i < 100) return tens[i/10] + ((i % 10 > 0)? " " + int2String(i % 10):"");
		if( i < 1000) return units[i/100] + " Hundred" + ((i % 100 > 0)?" and " + int2String(i % 100):"");
		if( i < 1000000) return int2String(i / 1000) + " Thousand " + ((i % 1000 > 0)? " " + int2String(i % 1000):"") ;
		return int2String(i / 1000000) + " Million " + ((i % 1000000 > 0)? " " + int2String(i % 1000000):"") ;
	}

}