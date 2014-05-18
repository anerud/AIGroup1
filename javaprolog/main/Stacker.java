package main;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import world.World;
import world.WorldObject;
import world.WorldConstraint.Relation;

/**
 * @author Joel
 * Contains methods for generation valid stacks of objects
 */
public class Stacker {

	public static class StackException extends Exception{
	}

	/** Returns a list of the given objects in an order that is stackable 
	 * 
	 * @param input
	 * @return
	 * @throws StackException
	 */
	public static List<WorldObject> stack(Set<WorldObject> input, World world) throws StackException
	{

		// create comparator
		Comparator<WorldObject> stackComparator = new Comparator<WorldObject>(){

			@Override
			public int compare(WorldObject o1, WorldObject o2) {
				if(!o1.getSize().equals(o2.getSize())){	
					if(o1.getSize().equals("large"))
					{	return -1;}
					else
					{return 1;}
				}	
				if (o1.getForm().equals(o2.getForm()))
					return o1.getColor().compareTo(o2.getColor());
				if (o1.getForm().equals("table"))
					return 1;
				if (o2.getForm().equals("table"))
					return -1;
				if (o1.getForm().equals("brick"))
					return 1;
				if (o2.getForm().equals("brick"))
					return -1;
				if (o1.getForm().equals("pyramid"))
					return 1;
				if (o2.getForm().equals("pyramid"))
					return -1;
				if (o1.getForm().equals("plank"))
					return 1;
				if (o2.getForm().equals("plank"))
					return -1;				
				return 0;
			}
		};




		Set<WorldObject> balls = new HashSet<WorldObject>();
		Set<WorldObject> boxes = new HashSet<WorldObject>(); 

		List<WorldObject> stack = new LinkedList<WorldObject>();

		// first, put all objects in the stack, except for balls and boxes.

		for (WorldObject wo: input )
			if (!wo.getForm().equals("ball") && !wo.getForm().equals("box"))
			{
				stack.add(wo);

			}
			else if  (wo.getForm().equals("ball"))
			{
				balls.add(wo);
			}
			else if(wo.getForm().equals("box"))
			{
				boxes.add(wo);

			}
		//sort current stack objects with the special order   
		Collections.sort(stack,stackComparator);

		//put in all the boxes, one by one, as high up in the stack as possible
		// by iterating through the stack backwards
		Collections.reverse(stack);

		for(WorldObject box:boxes)
		{
			Iterator<WorldObject> i = stack.iterator();
			WorldObject above = null;
			WorldObject below = (i.hasNext())?i.next():null;
			Boolean foundPlace = false;
			int index = 0;
			boolean hitBottom = false;
			do 
			{
				boolean okAbove = (above == null || world.isValidRelation(Relation.INSIDE, above, box) );
				boolean okBelow = (below == null || world.isValidRelation(Relation.ONTOP, box, below) );
				if (okAbove && okBelow)
				{
					foundPlace =true;
					break;
				}
				else
				{
					above = below;
					if(i.hasNext())
					{
						below = i.next();
					}
					else
					{
						below = null;
						hitBottom = true;
					}
				}
				index++;
				
			}while (i.hasNext() || !hitBottom);

			if (foundPlace)
			{
				stack.add(index, box);
			}
			else
			{
				throw new StackException();
			}	

		}

	     //all boxes has been placed
		//if there are more than one ball, there is no way to stack
		if (balls.size()>1)
			throw new StackException();
		
		//   try to place ball. 
		// it must be on the top.
		if (balls.size()==1)

		{    
			WorldObject ball = balls.iterator().next();
			if( stack.isEmpty() ||world.isValidRelation(Relation.INSIDE, ball, stack.iterator().next()) )
			{
				stack.add(0,ball);

			}
			else 
			{
				throw new StackException();
			}
		}

		
		//the stacking was ok
		Collections.reverse(stack);
		return stack;
		

	}



}
