package world;

import logic.LogicalExpression;
import main.Goal;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Roland on 2014-03-26.
 */
public class World {

	private ArrayList<LinkedList<WorldObject>> stacks;
	private List<WorldConstraint> constraints;
	private WorldObject holding;
	public static int nStatesChecked = 0;

	public World(ArrayList<LinkedList<WorldObject>> stacks,
			List<WorldConstraint> constrains, WorldObject holding) {
		this.constraints = constrains;
		this.holding = holding;
		this.stacks = stacks;
	}

	/**
	 * This constructor assumes there are no constraints and that nothing is
	 * being held
	 * 
	 * @param stacks
	 */
	public World(ArrayList<LinkedList<WorldObject>> stacks) {
		this.constraints = new ArrayList<WorldConstraint>();
		this.holding = null;
		this.stacks = stacks;
	}

	public World(ArrayList<LinkedList<WorldObject>> stacks,
			List<WorldConstraint> constrains) {
		this.constraints = constrains;
		this.holding = null;
		this.stacks = stacks;
	}

	/**
	 * This constructor assumes there are no constraints
	 * 
	 * @param stacks
	 * @param holding
	 */
	public World(ArrayList<LinkedList<WorldObject>> stacks, WorldObject holding) {
		this.constraints = new ArrayList<WorldConstraint>();
		this.holding = holding;
		this.stacks = stacks;
	}

	public int nObjectsOnTopOf(WorldObject o) {
		return stacks.get(this.columnOf(o)).indexOf(o);
	}

	public WorldObject getHolding() {
		return holding;
	}

	public ArrayList<LinkedList<WorldObject>> getStacks() {
		return stacks;
	}

	/**
	 * 
	 * @return a new Set containing the objects in this world
	 */
	public Set<WorldObject> getWorldObjects() {
		HashSet<WorldObject> objs = new HashSet<WorldObject>();
		if (holding != null) {
			objs.add(holding);
		}
		for (LinkedList<WorldObject> ll : stacks) {
			objs.addAll(ll);
		}
		return objs;
	}

	public boolean isOntopOfStack(WorldObject wo) {
		WorldObject top = topOfStack(columnOf(wo));
		return top != null && top.getId().equals(wo.getId());
	}

	// public boolean isOnFloor(WorldObject wo){
	// WorldObject bottom = bottomOfStack(columnOf(wo));
	// return bottom != null && bottom.equals(wo);
	// }

	/**
	 * 
	 * @param id
	 *            the id of the desired WorldObject
	 * @return the WorldObject which has the specified id or null if the world
	 *         contains no such WorldObject
	 */
	public WorldObject getWorldObject(String id) {
		if (holding != null && holding.getId().equals(id))
			return holding;
		if (id.equals("floor")) {
			return new WorldObject("floor", "floor", "floor", "floor");
		}
		for (LinkedList<WorldObject> ll : stacks) {
			for (WorldObject wo : ll) {
				if (wo.getId() != null && wo.getId().equals(id)) {
					return wo;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param column
	 * @return the WorldObject which is on top of the stack, or a floor object
	 *         if the stack has no WorldObjects
	 */
	public WorldObject topOfStack(int column) {
		List<WorldObject> stack = stacks.get(column);
		if (!stack.isEmpty()) {
			return stack.get(stack.size() - 1);
		} else {
			return new WorldObject("floor", "floor", "floor", "floor");
		}
	}

	public void setHolding(WorldObject holding) {
		this.holding = holding;
	}

	/**
	 * 
	 * @param column
	 * @return the WorldObject which is on the bottom of the stack, or null if
	 *         the stack has no WorldObjects
	 */
	public WorldObject bottomOfStack(int column) {
		List<WorldObject> stack = stacks.get(column);
		if (!stack.isEmpty()) {
			return stack.get(0);
		} else {
			return null;
		}
	}

	public int numberOfColumns() {
		return stacks.size();
	}

	public boolean isStackEmpty(int fromColumn) {
		return stacks.get(fromColumn).isEmpty();
	}

	/**
	 * 
	 * @param fromColumn
	 * @return true if the specified operation was successful
	 */
	public boolean moveTopToNextColumn(int fromColumn) {
		// TODO: check that the objects are compatible, that is, that object a
		// can be placed ontop of object b
		if (!isStackEmpty(fromColumn)) {
			WorldObject wo = stacks.get(fromColumn).getLast();
			stacks.get(fromColumn).removeLast();
			stacks.get((fromColumn + 1) % numberOfColumns()).addLast(wo);
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param woColumn
	 * @return true if the operation was successful
	 */
	public boolean pick(int woColumn) {
		WorldObject top = topOfStack(woColumn);
		if (holding == null && !top.getForm().equals("floor")) {
			holding = top;
			stacks.get(woColumn).removeLast();
			return true;
		}
		return false;
	}

	/**
	 * Drops the object being held at the specified column
	 * 
	 * @param woColumn
	 * @return true if the operation was successful. If false is returned, it
	 *         means the world state is unchanged.
	 */
	public boolean drop(int woColumn) {
		if (holding == null) {
			return false;
		}
		WorldObject top = topOfStack(woColumn);
		if (top != null
				&& !(top.getForm().equals("box") ? isValidRelation(
						WorldConstraint.Relation.INSIDE, holding, top)
						: isValidRelation(WorldConstraint.Relation.ONTOP,
								holding, top))) {
			return false;
		} // This assumes it's always ok to put stuff directly on the floor
		stacks.get(woColumn).addLast(holding);
		holding = null;
		return true;
	}

	/**
	 * 
	 * @param wo
	 * @return the column of the WorldObject, or -1 if the object is not
	 *         contained in the world. If the object is the floor, the first
	 *         column which is empty is returned. Relations are ignored for
	 *         RelativeWorldObjects.
	 */
	public int columnOf(WorldObject wo) {
		if (wo instanceof RelativeWorldObject) {
			wo = new WorldObject(wo);
		}
		for (LinkedList<WorldObject> ll : stacks) {
			if (ll.contains(wo)) {
				return stacks.indexOf(ll);
			}
			if (wo.getForm().equals("floor") && ll.isEmpty()) {
				return stacks.indexOf(ll);
			}
		}
		return -1;
	}

	/**
	 * Indexed from 0 where 0 means the object is on the floor and 1 means one
	 * step above the floor, etc. Relations are ignored for
	 * RelativeWorldObjects.
	 * 
	 * @param wo
	 * @return -1 if the object is not contained in this world or if the object
	 *         is the floor
	 */
	private int rowOf(WorldObject wo) {
		if (wo instanceof RelativeWorldObject) {
			wo = new WorldObject(wo);
		}
		for (LinkedList<WorldObject> ll : stacks) {
			if (ll.contains(wo)) {
				return ll.indexOf(wo);
			}
		}
		return -1;
	}

	public boolean isValidRelation(WorldConstraint.Relation relation,
			WorldObject obj1, LogicalExpression<WorldObject> woRel) {
		if (woRel.getObjs() != null) {
			if (woRel.getOp().equals(LogicalExpression.Operator.AND)) {
				for (WorldObject wo : woRel.getObjs()) {
					if (!isValidRelation(relation, obj1, wo)) {
						return false;
					}
				}
				for (LogicalExpression<WorldObject> exp : woRel
						.getExpressions()) {
					if (!isValidRelation(relation, obj1, exp)) {
						return false;
					}
				}
			} else {
				for (WorldObject wo : woRel.getObjs()) {
					if (isValidRelation(relation, obj1, wo)) {
						return true;
					}
				}
				for (LogicalExpression<WorldObject> exp : woRel
						.getExpressions()) {
					if (isValidRelation(relation, obj1, exp)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isValidRelation(WorldConstraint.Relation relation,
			WorldObject obj1, WorldObject obj2) {
		if (obj1.getId().equals(obj2.getId()))
			return false; // Cannot have a relation to itself
		if (obj2 instanceof RelativeWorldObject && obj2.getForm() == null) {
			LogicalExpression<WorldObject> woRel = ((RelativeWorldObject) obj2)
					.getRelativeTo();
			return isValidRelation(relation, obj1, woRel);
		}
		String form1 = obj1.getForm();
		String form2 = obj2.getForm();
		String size1 = obj1.getSize();
		String size2 = obj2.getSize();
		if (form1 == null || form2 == null)
			return false;
		if (relation.equals(WorldConstraint.Relation.ONTOP)) {
			if (form2.equals("box") || form2.equals("ball"))
				return false; // It's called "INSIDE" a box. Balls cannot
								// support anything.
			if (form1.equals("ball") && !(form2.equals("floor")))
				return false;
			if (form1.equals(""))
				return false;
			if (size1.equals("large") && size2.equals("small"))
				return false;
			if (form1.equals("box")) { // "Boxes can only be supported by tables or planks of the same size, but large boxes can also be supported by large bricks.".
										// TODO: Not sure if this is the
										// intended intepretation. Can a small
										// box be placed on a large plank? Why
										// not?
				if (size1.equals("small") && size2.equals("small")) {
					if (!(form2.equals("table") || form2.equals("plank")))
						return false;
				} else if (size1.equals("large") && size2.equals("large")) {
					if (!(form2.equals("table") || form2.equals("plank") || form2
							.equals("brick")))
						return false;
				}
			}
		} else if (relation.equals(WorldConstraint.Relation.INSIDE)) {
			if (!form2.equals("box"))
				return false; // Stuff can only be "INSIDE" boxes.
			if (size1.equals("large") && size2.equals("small"))
				return false;
			if (form1.equals("pyramid") || form1.equals("plank")) {
				if (!(size2.equals("large") && size1.equals("small")))
					return false; // Boxes cannot contain pyramids or planks of
									// the same size.
			}
			if (form1.equals("box")) { // "Boxes can only be supported by tables or planks of the same size, but large boxes can also be supported by large bricks.".
				if (!(size1.equals("small") && size2.equals("large")))
					return false;
			}
		} else if (relation.equals(WorldConstraint.Relation.UNDER)){
            if(form1.equals("ball")){
                return false; //Balls cannot support anything
            }
        } else if (relation.equals(WorldConstraint.Relation.ABOVE)){
            if(form2.equals("ball")){
                return false; //Balls cannot support anything
            }
        }
		return true;
	}

	/**
	 * Ignores the objects in the top level of theRelativeObjects and makes an
	 * attachment of all combinations of toBeAttached and attachTo. If an
	 * attachment does not obey the physical laws of this world, the attachment
	 * is ignored.
	 * 
	 * @param toBeAttached
	 * @param attachTo
	 * @return
	 */
	public LogicalExpression<WorldObject> attachWorldObjectsToRelation(
			Set<WorldObject> toBeAttached,
			LogicalExpression<WorldObject> attachTo,
			LogicalExpression.Operator op) {
		LogicalExpression<WorldObject> relobjs = new LogicalExpression<WorldObject>(
				null, op);
		for (WorldObject wo : toBeAttached) {
			// clone..
			Set<WorldObject> objsClone = new HashSet<WorldObject>();
			Set<LogicalExpression> expClone = new HashSet<LogicalExpression>();
			for (LogicalExpression<WorldObject> le : attachTo.getExpressions()) {
				Set<WorldObject> clonedObjs = new HashSet<WorldObject>();
				for (WorldObject wo1 : le.getObjs()) {
					clonedObjs.add(wo1.clone());
				}
				LogicalExpression<WorldObject> leCopy = new LogicalExpression<>(
						clonedObjs, le.getExpressions(), le.getOp());
				expClone.add(leCopy);
			}
			for (WorldObject obj : attachTo.getObjs()) {
				objsClone.add(obj.clone());
			}
			LogicalExpression<WorldObject> attachToClone = new LogicalExpression<WorldObject>(
					objsClone, expClone, attachTo.getOp());

			// set the non-relative object...
			Set<WorldObject> tops = attachToClone.topObjs();
			Set<WorldObject> toBeRemoved = new HashSet<WorldObject>();
			for (WorldObject wo1 : tops) {
				if (wo1 instanceof RelativeWorldObject && (wo1).getId() == null) {
					if (isValidRelation(
							((RelativeWorldObject) wo1).getRelation(), wo, wo1)) {
						((RelativeWorldObject) wo1).setObj(wo);
					} else {
						toBeRemoved.add(wo1);
					}
				}
			}
			// Remove the relations which are invalid in this world
			tops.removeAll(toBeRemoved);
			attachToClone.removeAll(toBeRemoved);

			// Add..
			int size = tops.size();
			if (size == 1) {
				if (relobjs.getObjs() == null) {
					relobjs.setObjs(new HashSet<WorldObject>());
				}
				relobjs.getObjs().addAll(tops);
			} else if (size > 1) {
				relobjs.getExpressions().add(attachToClone);
			}
		}
		return relobjs;
	}

	public LogicalExpression<WorldObject> attachWorldObjectsToRelation(
			LogicalExpression<WorldObject> toBeAttached,
			LogicalExpression<WorldObject> attachTo) {
		LogicalExpression<WorldObject> logExp = new LogicalExpression<WorldObject>(
				null, toBeAttached.getOp());

		Set<WorldObject> wos = toBeAttached.getObjs();
		if (wos != null) {
			logExp = attachWorldObjectsToRelation(wos, attachTo,
					toBeAttached.getOp());
		}

		for (LogicalExpression<WorldObject> le : toBeAttached.getExpressions()) {
			wos = toBeAttached.getObjs();
			logExp.getExpressions().add(
					attachWorldObjectsToRelation(wos, le, le.getOp()));
		}

		return logExp;
	}

	// /**
	// * For an object to be retained, all relations of a RelativeWorldObject or
	// a WorldObject must be fulfilled in this world.
	// * @param toBeFiltered
	// * @return
	// */
	// public Set<WorldObject> filterByExistsInWorld(Set<WorldObject>
	// toBeFiltered) {
	// Set<WorldObject> toBeRetained = new HashSet<WorldObject>();
	// for(WorldObject obj : toBeFiltered){
	// //Check that the object exists
	// Set<WorldObject> fil = getWorldObjects();
	// filterByMatch(fil, obj);
	// if(!fil.isEmpty()){
	// toBeRetained.add(obj);
	// }
	//
	// //Now remove the ones that do not match the relation (if any)
	// if(obj instanceof RelativeWorldObject){
	// if(!hasRelation(((RelativeWorldObject)obj).getRelation(), obj,
	// ((RelativeWorldObject) obj).getRelativeTo())){
	// toBeRetained.remove(obj);
	// }
	// }
	// }
	// toBeFiltered.retainAll(toBeRetained);
	// return toBeFiltered;
	// }

	/**
	 * Uses the operators in le to recursively determine if the objects exist in
	 * the world
	 * 
	 * @param le
	 * @return
	 */
	public Set<WorldObject> filterByExistsInWorld(LogicalExpression<WorldObject> le) {
		Set<WorldObject> filtered = new HashSet<WorldObject>();
		for (WorldObject wo : le.topObjs()) {
			filtered.add(new WorldObject(wo));
		}
		if (le.getOp().equals(LogicalExpression.Operator.AND)) {
			if (le.getObjs() != null) {
				for (WorldObject wo : le.getObjs()) {
					if (wo instanceof RelativeWorldObject) {
						RelativeWorldObject rwo = ((RelativeWorldObject) wo);
						if (!hasRelation(rwo)) {
							filtered.remove(new WorldObject(rwo));
						}
					}
				}
			}
			for (LogicalExpression<WorldObject> exp : le.getExpressions()) {
                Set<WorldObject> to = new HashSet<WorldObject>();
                for (WorldObject wo : exp.topObjs()) {
                    to.add(new WorldObject(wo));
                }
				to.removeAll(filterByExistsInWorld(exp));
                for(WorldObject wo : to){
                    filtered.remove(new WorldObject(wo));
                }
			}
		} else {
			Set<WorldObject> toBeRetained = new HashSet<WorldObject>();
			if (le.getObjs() != null) {
				for (WorldObject wo : le.getObjs()) {
					if (wo instanceof RelativeWorldObject) {
						RelativeWorldObject rwo = ((RelativeWorldObject) wo);
						if (hasRelation(rwo)) {
							toBeRetained.add(new WorldObject(rwo));
						}
					} else {
						toBeRetained.add(wo);
					}
				}
			}
			for (LogicalExpression<WorldObject> exp : le.getExpressions()) {
				toBeRetained.addAll(filterByExistsInWorld(exp));
			}
			filtered.retainAll(toBeRetained);
		}
		return filtered;
	}

	public void filterByMatch(Set<WorldObject> toBeFiltered, WorldObject match) {
		Set<WorldObject> toBeRetained = new HashSet<WorldObject>();
		for (WorldObject wo : toBeFiltered) {
			if (wo.matchesPattern(match)) {
				toBeRetained.add(wo);
			}
		}
		toBeFiltered.retainAll(toBeRetained);
	}

	/**
	 * Retains the objects in toBeFiltered which are "relation" to
	 * theRelativeObjects
	 * 
	 * @param toBeFiltered
	 * @param theRelativeObjects
	 * @param relation
	 */
	public Set<WorldObject> filterByRelation(Set<WorldObject> toBeFiltered,
			LogicalExpression<WorldObject> theRelativeObjects,
			WorldConstraint.Relation relation) {
		if (theRelativeObjects.topObjs().iterator().next().getForm() == null) {
			throw new NullPointerException();
		}
		Set<WorldObject> toBeRetained = new HashSet<>();
		for (WorldObject wo : toBeFiltered) {
			for (WorldObject obj : theRelativeObjects.topObjs()) {
				if (obj.getForm() == null
						&& theRelativeObjects.getOp().equals(relation)) {
					obj.setId(wo.getId());
					obj.setColor(wo.getColor());
					obj.setForm(wo.getForm());
					obj.setSize(wo.getSize());
				}
			}
			if (hasRelation(relation, wo, theRelativeObjects)) {
				toBeRetained.add(wo);
			}
		}
		toBeFiltered.retainAll(toBeRetained);
		return toBeFiltered;
	}

	/**
	 * Ignores the objects in the top level of theRelativeObjects and uses only
	 * their relation to determine which objects in toBeFiltered to retain. If
	 * used with the argument AND, all conditions in theRelativeObjects must be
	 * met for each WorldObject in toBeFiltered. If used with the argument OR,
	 * one of the conditions must be met for each object.
	 * 
	 * @param toBeFiltered
	 * @param theRelativeObjects
	 * @return
	 */
	public Set<WorldObject> filterByRelation(Set<WorldObject> toBeFiltered,
			LogicalExpression<WorldObject> theRelativeObjects,
			LogicalExpression.Operator op) {
		Set<WorldObject> toBeFilteredCopy = new HashSet<WorldObject>(
				toBeFiltered);
		for (WorldObject wo : toBeFilteredCopy) {
			if (wo instanceof RelativeWorldObject) {
				throw new IllegalArgumentException("Debug: Nocando");
			}
		}
		LogicalExpression<WorldObject> attached = attachWorldObjectsToRelation(
				toBeFiltered, theRelativeObjects, LogicalExpression.Operator.OR);

		if (op.equals(LogicalExpression.Operator.AND)) {
			for (WorldObject wo : attached.topObjs()) {
				RelativeWorldObject rwo = ((RelativeWorldObject) wo);
				if (!hasRelation(rwo)) {
					toBeFilteredCopy.remove(new WorldObject(rwo));
				}
			}
		} else {
			Set<WorldObject> toBeRetained = new HashSet<WorldObject>();
			for (WorldObject wo : attached.topObjs()) {
				RelativeWorldObject rwo = ((RelativeWorldObject) wo);
				if (hasRelation(rwo)) {
					toBeRetained.add(new WorldObject(rwo));
				}
			}
			toBeFilteredCopy.retainAll(toBeRetained);
		}
		return toBeFilteredCopy;
	}

	/**
	 * Determines if the objects have a certain geometric relation in the world
	 * If any of the arguments is a RelativeWorldObject, the relation of this
	 * particular object is ignored.
	 * 
	 * @param relation
	 * @param wo
	 * @param woRel
	 * @throws java.lang.NullPointerException
	 *             if wo.getForm() == null || woRel.getForm() == null
	 * @return
	 */
	public boolean hasRelation(WorldConstraint.Relation relation,
			WorldObject wo, WorldObject woRel) {
		// Make sure both objects have the same dynamic type
		wo = new WorldObject(wo);
		woRel = new WorldObject(woRel);
		int col1 = columnOf(wo);
		if (col1 == -1) {
			return false; // The object does not exist in this world and cannot
							// have a relation to anything
		}
		if (wo.getForm() == null || woRel.getForm() == null) {
			throw new NullPointerException();
		}
		if (relation.equals(WorldConstraint.Relation.ONTOP)
				|| relation.equals(WorldConstraint.Relation.INSIDE)) {
			int row = rowOf(wo);
			if (row == 0) {
				return woRel.getForm().equals("floor");
			} else {
				return stacks.get(col1).get(row - 1).equals(woRel);
			}
		} else if (relation.equals(WorldConstraint.Relation.UNDER)) {
			return hasRelation(WorldConstraint.Relation.ABOVE, woRel, wo);
		} else if (relation.equals(WorldConstraint.Relation.LEFTOF)) {
			return col1 < columnOf(woRel);
		} else if (relation.equals(WorldConstraint.Relation.RIGHTOF)) {
			return hasRelation(WorldConstraint.Relation.LEFTOF, woRel, wo);
		} else if (relation.equals(WorldConstraint.Relation.BESIDE)) {
			int col2 = columnOf(woRel);
			return Math.abs(col1 - col2) == 1;
		} else if (relation.equals(WorldConstraint.Relation.ABOVE)) {
			int col2 = columnOf(woRel);
			if (col1 != col2) {
				return false;
			}
			int row1 = rowOf(wo);
			int row2 = rowOf(woRel);
			return row1 > row2;
		}
		return false;
	}

	public boolean hasRelation(RelativeWorldObject obj) {
		if (obj.getForm() == null) {
			throw new NullPointerException(); // This method does NOT support
												// nullpointers...
		}
		return hasRelation(obj.getRelation(), obj, obj.getRelativeTo());
	}

	/**
	 * Recursively determines if the WorldObject fulfils the relation to the
	 * logical expression If the parameter wo is an instance of
	 * RelativeWorldObject, the relation of wo is ignored.
	 * 
	 * @param relation
	 * @param wo
	 * @param theRelativeObjects
	 * @throws java.lang.NullPointerException
	 *             if wo.getForm() == null ||
	 *             theRelativeObjects.topObjs().iterator().next().getForm() ==
	 *             null
	 * @return
	 */
	public boolean hasRelation(WorldConstraint.Relation relation,
			WorldObject wo, LogicalExpression<WorldObject> theRelativeObjects) {
		if (wo.getForm() == null
				|| theRelativeObjects.topObjs().iterator().next().getForm() == null) {
			throw new NullPointerException();
		}
		LogicalExpression.Operator op = theRelativeObjects.getOp();
		if (op.equals(LogicalExpression.Operator.AND)) {
			for (WorldObject wo1 : theRelativeObjects.getObjs()) {
				// First, check that the relations of the relative object are
				// fulfilled
				if (wo1 instanceof RelativeWorldObject) {
					if (!hasRelation(((RelativeWorldObject) wo1).getRelation(),
							wo1, ((RelativeWorldObject) wo1).getRelativeTo())) {
						return false;
					}
				}
				if (!hasRelation(relation, wo, wo1)) {
					return false;
				}
			}
			for (LogicalExpression exp : theRelativeObjects.getExpressions()) {
				if (!hasRelation(relation, wo, exp)) {
					return false;
				}
			}
			return true;
		} else {
			for (WorldObject wo1 : theRelativeObjects.getObjs()) {
				if (hasRelation(relation, wo, wo1)) {
					// So far so good. Now check that the relations of the
					// relative object are fulfilled
					if (wo1 instanceof RelativeWorldObject) {
						if (hasRelation(
								((RelativeWorldObject) wo1).getRelation(), wo1,
								((RelativeWorldObject) wo1).getRelativeTo())) {
							return true;
						}
					} else {
						return true;
					}
				}
			}
			for (LogicalExpression exp : theRelativeObjects.getExpressions()) {
				if (hasRelation(relation, wo, exp)) {
					return true;
				}
			}
			return false;
		}
	}

	public World clone() {
		ArrayList<LinkedList<WorldObject>> cStack = new ArrayList<LinkedList<WorldObject>>();
		for (LinkedList<WorldObject> s : stacks) {
			cStack.add(new LinkedList<WorldObject>(s));
		}

		List<WorldConstraint> cCon = new LinkedList<WorldConstraint>(
				this.constraints);
		return new World(cStack, cCon, this.holding);
	}

	public String getRepresentString() {
		StringBuilder sb = new StringBuilder();
		if (holding != null) {
			sb.append(holding.getId());
		}
		sb.append(".");
		for (LinkedList<WorldObject> wl : stacks) {
			for (WorldObject wo : wl) {
				sb.append(wo.getId());
				sb.append(",");
			}
			sb.append(".");
		}

		return sb.toString();
	}

	/**
	 * @param columnIndex
	 * @param holding
	 * @return
	 */
	public boolean isPlaceable(int columnIndex, WorldObject holding) {
		WorldObject wo = topOfStack(columnIndex);
        return isValidRelation(WorldConstraint.Relation.ONTOP, holding, wo);
	}

    public boolean isGoalFulFilled(Goal goal) {
        nStatesChecked++;

        try (PrintWriter asdf = new PrintWriter(new BufferedWriter(
                new FileWriter("GoalLog.txt", true)))) {
            asdf.append(getRepresentString() + " \n");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LogicalExpression<WorldObject> le = goal.getExpression();
        LogicalExpression.Operator op = le.getOp();
        Set<WorldObject> s = filterByExistsInWorld(le);
        if(goal.getAction().equals(Goal.Action.TAKE)){
            if(holding == null) return false;
            if(op.equals(LogicalExpression.Operator.OR)){
                for(WorldObject wo : s){
                    if(wo.getId().equals(holding.getId())) return true;
                }
            }
            return (s.size() != 1) && holding.getId().equals(s.iterator().next().getId());
        }
        if(op.equals(LogicalExpression.Operator.AND)){
            if(le.getObjs() != null){
                for(WorldObject wo : le.getObjs()){
                    if(!s.contains(new WorldObject(wo))){
                        return false;
                    }
                }
            }
            for(LogicalExpression<WorldObject> le2 : le.getExpressions()){
                Goal g = new Goal(le2, goal.getAction());
                if(!isGoalFulFilled(g)){
                    return false;
                }
            }
            return true;
        }
        return s.size() >= 1;
    }
}
