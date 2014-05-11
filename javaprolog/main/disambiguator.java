package main;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tree.Node;

import world.WorldObject;

public class Disambiguator {
	private String message;

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
	

	public WorldObject disambiguate(Set<WorldObject> objs, Node n)

	{   
		
		//put alternatives in sorted list for nicer enumeration
		List<WorldObject> sortedObjs = new LinkedList<WorldObject>(objs);
		
		// make comparator to sort list 
		Comparator <WorldObject> worldObjectComparator = new Comparator<WorldObject>()
		{
			@Override
			public int compare(WorldObject o1, WorldObject o2) {
				return 100*o1.getForm().compareTo(o2.getForm())+10*o1.getColor().compareTo(o2.getColor())+o1.getSize().compareTo(o2.getSize());
			}
		};
		
		Collections.sort(sortedObjs, worldObjectComparator);
		

		StringBuilder sb = new StringBuilder();
		if (sortedObjs.size() == 1)
			return sortedObjs.iterator().next();
		int count = sortedObjs.size();
		String iCanSee = "I can see " + int2String(count) + " "
				+ n.toNaturalString(true);
		sb.append(iCanSee);
		sb.append(". Did you mean ");

		Iterator<WorldObject> i = sortedObjs.iterator();

		// produce minimal unique descriptions, separated by commas and "or" 
		boolean first = true;
		while (i.hasNext()) {
			String d = minimalUniqueDiscription(i.next(),
					new HashSet<WorldObject>(objs));
			if (d.equals("error")) {
				message = iCanSee
						+ " Im sorry, but I dont know which one you mean.";
				return null;
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
		message = sb.toString();
		// System.out.println(sb.toString());
		// not immplemented
		return null;
	}

	public String getMessage() {
		return message;
	}

	// returns a description of a WorldObject, that is minimal but sufficient to
	// distinguish it from
	// all other WorldObjects in the given set.
	private String minimalUniqueDiscription(WorldObject obj,
			Set<WorldObject> theOthers) {
		theOthers.remove(obj);
		Set<WorldObject> removeThese = new HashSet<WorldObject>();

		// check if type is unique
		for (WorldObject other : theOthers)
			if (!obj.getForm().equals(other.getForm()))
				removeThese.add(other);
		theOthers.removeAll(removeThese);

		if (theOthers.isEmpty())
			return "the " + obj.getForm();

		// form was not uniqe, check if color is unique
		for (WorldObject other : theOthers)
			if (!obj.getColor().equals(other.getColor()))
				removeThese.add(other);
		theOthers.removeAll(removeThese);

		if (theOthers.isEmpty())
			return "the " + obj.getColor() + " " + obj.getForm();

		// form and color was not unique, check if size is unique
		for (WorldObject other : theOthers)
			if (!obj.getSize().equals(other.getSize()))
				removeThese.add(other);
		theOthers.removeAll(removeThese);

		if (theOthers.isEmpty())
			return "the " + obj.getSize() + " " + obj.getColor() + " "
					+ obj.getForm();

		// if there are exactly two identical objects, they can be disambiguated
		// by relative position.
		// if there are more than two identical objects, it is not possible to
		// uniquely refer to any one of them

		if (theOthers.size() > 1)
			return "error";

		// todo: disambiguate using realtive position in the world
		return "error";

		// natural language representation of integer quantities
	}

	private String int2String(int n) {

		switch (n) {
		case 0:
			return "zero";
		case 1:
			return "one";
		case 2:
			return "two";
		case 3:
			return "three";
		case 4:
			return "four";
		case 5:
			return "five";
		case 6:
			return "six";
		case 7:
			return "seven";
		case 8:
			return "eight";
		case 9:
			return "nine";

		default:
			return Integer.toString(n);

		}
	}
	


}
