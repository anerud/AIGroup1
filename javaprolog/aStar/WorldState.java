package aStar;

import java.util.*;

import logic.LogicalExpression;
import main.Goal;
import world.EmptyWorldObject;
import world.RelativeWorldObject;
import world.World;
import world.WorldConstraint;
import world.WorldObject;

public class WorldState implements IAStarState {

	private List<String> actionsToGetHere;
	private double heuristicWeight = 1.5;
	private int heuristicValue;
	private World world;
	private Goal goal;
	private Set<WorldObject> objectsToMove;
	private IHeuristic<WorldState> heuristic = new HeuristicONE();
	private boolean bothArmsMoved;
	private int arm1;
	private int arm2;
	private double correctionForMovingBothArms;
	private static boolean printProgress = false;

	public int getHeuristicValue() {
		return heuristicValue;
	}

	public static Set<String> getVisitedWorld() {
		return visitedWorld;
	}

	public static void setVisitedWorld(Set<String> visitedWorld) {
		WorldState.visitedWorld = visitedWorld;
	}

	private static Set<String> visitedWorld = new HashSet<String>();

	/**
	 * 
	 * @param world
	 * @param goal
	 * @param actionsToGetHere
	 */
	public WorldState(World world, Goal goal, List<String> actionsToGetHere, int arm1, int arm2,
			double correctionForMoving) throws CloneNotSupportedException {
		this.world = world;
		this.goal = goal;
		this.correctionForMovingBothArms = correctionForMoving;
		this.heuristicValue = (int) heuristic.h(this, goal);
		HashMap<Integer, Set<WorldObject>> heuristic = null;
		heuristic = goal.getExpression().isCnf() ? computeHeuristicOnCnf(goal.getExpression()) : computeHeuristicOnDnf(
				goal.getExpression(), null);
		Set<WorldObject> set1 = heuristic.get(1);
		Set<WorldObject> set2 = heuristic.get(2);
		set1.removeAll(set2);
		this.heuristicValue = set1.size() * 2 + set2.size() * 4;
		this.actionsToGetHere = actionsToGetHere;
		this.arm1 = arm1;
		this.arm2 = arm2;
		// //Debugging
		// //____________________________
		// if(world.getRepresentString().equals(".e,.a,j,.l,m,..i,h,..g,b,.k,f,.c,d,..")){
		// this.getClass();
		// }
		if (this.heuristicValue == 2) {
			this.getClass();
		}
		// //____________________________
	}

	private HashMap<Integer, Set<WorldObject>> computeHeuristicOnCnf(LogicalExpression<WorldObject> expression)
			throws CloneNotSupportedException {
		HashMap<Integer, Set<WorldObject>> minObjs = new HashMap<>();
		Set<WorldObject> moveAtleastOnce = new HashSet<>();
		Set<WorldObject> moveAtleastTwice = new HashSet<>();
		minObjs.put(1, moveAtleastOnce);
		minObjs.put(2, moveAtleastTwice);
		if (!expression.isCnf()) {
			return minObjs;
		}
		// Computes max-min, which is a lower bound
		if (expression.getOp().equals(LogicalExpression.Operator.OR)) {
			return computeHeuristicOnDnf(expression, null);
		} else {
			if (expression.getObjs() != null) {
				for (WorldObject wo : expression.getObjs()) {
					HashMap<Integer, Set<WorldObject>> herps = calculateMinObjsToMove(wo, null, true);
					moveAtleastOnce.addAll(herps.get(1));
					moveAtleastTwice.addAll(herps.get(2));
				}
			}
			// Do the le's once to see which objects which invariably need to
			// move in each clause, and then a second time to determine the
			// max-min clause
			Set<WorldObject> toAddOnce = new HashSet<>();
			Set<WorldObject> toAddTwice = new HashSet<>();
			for (LogicalExpression l : expression.getExpressions()) {
				HashMap<Integer, Set<WorldObject>> minObjsCopy = new HashMap<>(); // Copying
																					// just
																					// in
																					// case
																					// mutation
																					// occurs..
																					// Might
																					// be
																					// able
																					// to
																					// remove
																					// this..
				Set<WorldObject> moveAtleastOnceCopy = new HashSet<>();
				moveAtleastOnceCopy.addAll(moveAtleastOnce);
				Set<WorldObject> moveAtleastTwiceCopy = new HashSet<>();
				moveAtleastTwiceCopy.addAll(moveAtleastTwice);
				minObjsCopy.put(1, moveAtleastOnceCopy);
				minObjsCopy.put(2, moveAtleastTwiceCopy);
				HashMap<Integer, Set<WorldObject>> herps = computeHeuristicOnDnf(l, minObjsCopy);
				toAddOnce.addAll(herps.get(3));
				toAddTwice.addAll(herps.get(4));
			}
			moveAtleastOnce.addAll(toAddOnce);
			moveAtleastTwice.addAll(toAddTwice);

			HashMap<Integer, Set<WorldObject>> max = minObjs;
			for (LogicalExpression l : expression.getExpressions()) {
				HashMap<Integer, Set<WorldObject>> minObjsCopy = new HashMap<>(); // Copying
																					// just
																					// in
																					// case
																					// mutation
																					// occurs..
																					// Might
																					// be
																					// able
																					// to
																					// remove
																					// this..
				Set<WorldObject> moveAtleastOnceCopy = new HashSet<>();
				moveAtleastOnceCopy.addAll(moveAtleastOnce);
				Set<WorldObject> moveAtleastTwiceCopy = new HashSet<>();
				moveAtleastTwiceCopy.addAll(moveAtleastTwice);
				minObjsCopy.put(1, moveAtleastOnceCopy);
				minObjsCopy.put(2, moveAtleastTwiceCopy);
				HashMap<Integer, Set<WorldObject>> herps = computeHeuristicOnDnf(l, minObjsCopy);
				Set<WorldObject> set1 = herps.get(1);
				Set<WorldObject> set2 = herps.get(2);
				set1.removeAll(set2);
				Set<WorldObject> maxSet1 = max.get(1);
				Set<WorldObject> maxSet2 = max.get(2);
				maxSet1.removeAll(maxSet2);
				if (set1.size() + set2.size() * 2 > maxSet1.size() + maxSet2.size() * 2) {
					max = herps;
				}
			}
			return max;
		}
	}

	/**
	 * Only creates non-zero heuristics for dnf expressions. If not dnf, an
	 * empty map is returned. //TODO: make it support cnf as well, as some
	 * expressions are not practical in dnf. Consider for example the sentence
	 * "put all objects beside an object"
	 * 
	 * @param le
	 * @return
	 */
	private HashMap<Integer, Set<WorldObject>> computeHeuristicOnDnf(LogicalExpression<WorldObject> le,
			HashMap<Integer, Set<WorldObject>> minObjsRef) throws CloneNotSupportedException {
		HashMap<Integer, Set<WorldObject>> minObjs = new HashMap<>();
		Set<WorldObject> moveAtleastOnce = new HashSet<>();
		Set<WorldObject> moveAtleastTwice = new HashSet<>();
		minObjs.put(1, moveAtleastOnce);
		minObjs.put(2, moveAtleastTwice);
		if (!le.isDnf()) {
			return minObjs;
		}
		if (le.getOp().equals(LogicalExpression.Operator.AND) || le.size() <= 1) {
			if (le.getObjs() != null) {
				for (WorldObject wo : le.getObjs()) {
					HashMap<Integer, Set<WorldObject>> herps = calculateMinObjsToMove(wo, minObjsRef, true);
					moveAtleastOnce.addAll(herps.get(1));
					moveAtleastTwice.addAll(herps.get(2));
				}
			} // Note that the expression is simplified and dnf, so we do not
				// need to check the logicalexpressions..
			return minObjs;
		} else {
			// return the smallest set //TODO: some goals are impossible to
			// reach. In this case, it is not good to use it for heuristics!
			// (could result in endless searching in a local minimum)
			HashMap<Integer, Set<WorldObject>> smallestSet = new HashMap<>();
			Set<WorldObject> onceAll = new HashSet<>();
			Set<WorldObject> twiceAll = new HashSet<>();
			boolean first = true;
			if (le.getObjs() != null) {
				for (WorldObject wo : le.getObjs()) {
					HashMap<Integer, Set<WorldObject>> thisSet = calculateMinObjsToMove(wo, minObjsRef, true);
					if (first) {
						onceAll.addAll(thisSet.get(1));
						twiceAll.addAll(thisSet.get(2));
						smallestSet = thisSet;
						first = false;
					} else {
						Set<WorldObject> set1 = thisSet.get(1);
						Set<WorldObject> set2 = thisSet.get(2);
						onceAll.retainAll(set1);
						twiceAll.retainAll(set2);
						set1.removeAll(set2);
						Set<WorldObject> smallestSet1 = smallestSet.get(1);
						Set<WorldObject> smallestSet2 = smallestSet.get(2);
						smallestSet1.removeAll(smallestSet2);
						if (set1.size() + set2.size() * 2 < smallestSet1.size() + smallestSet2.size() * 2) {
							smallestSet = thisSet;
						}
					}
				}
			}
			for (LogicalExpression<WorldObject> le1 : le.getExpressions()) {
				// le1 is an AND expression
				HashMap<Integer, Set<WorldObject>> thisSet = computeHeuristicOnDnf(le1, minObjsRef);
				if (first) {
					onceAll.addAll(thisSet.get(1));
					twiceAll.addAll(thisSet.get(2));
					smallestSet = thisSet;
					first = false;
				} else {
					Set<WorldObject> set1 = thisSet.get(1);
					Set<WorldObject> set2 = thisSet.get(2);
					onceAll.retainAll(set1);
					twiceAll.retainAll(set2);
					set1.removeAll(set2);
					Set<WorldObject> smallestSet1 = smallestSet.get(1);
					Set<WorldObject> smallestSet2 = smallestSet.get(2);
					set1.removeAll(set2);
					if (set1.size() + set2.size() * 2 < smallestSet1.size() + smallestSet2.size() * 2) {
						smallestSet = thisSet;
					}
				}
			}
			smallestSet.put(3, onceAll);
			smallestSet.put(4, twiceAll);
			return smallestSet;
		}
	}

	private HashMap<Integer, Set<WorldObject>> calculateMinObjsToMove(WorldObject wo,
			HashMap<Integer, Set<WorldObject>> minObjsRef, boolean recursive) {
		HashMap<Integer, Set<WorldObject>> minObjs = null;
		Set<WorldObject> moveAtleastOnce = null;
		Set<WorldObject> moveAtleastTwice = null;
		if (minObjsRef == null) {
			minObjs = new HashMap<>();
			moveAtleastOnce = new HashSet<>();
			moveAtleastTwice = new HashSet<>();
		} else {
			minObjs = new HashMap<Integer, Set<WorldObject>>();
			moveAtleastOnce = new HashSet<WorldObject>(minObjsRef.get(1));
			moveAtleastTwice = new HashSet<WorldObject>(minObjsRef.get(2));
		}
		minObjs.put(1, moveAtleastOnce);
		minObjs.put(2, moveAtleastTwice);

		if (!(wo instanceof RelativeWorldObject)) {
			moveAtleastOnce.addAll(world.objectsAbove(wo));
			return minObjs;
		}
		WorldObject woRel = ((RelativeWorldObject) wo).getRelativeTo();

		if (woRel instanceof RelativeWorldObject && recursive) {
			HashMap<Integer, Set<WorldObject>> mins = calculateMinObjsToMove(woRel, minObjs, true);
			moveAtleastOnce.addAll(mins.get(1));
			moveAtleastTwice.addAll(mins.get(2));
		}

		switch (((RelativeWorldObject) wo).getRelation()) {
		case ONTOP:
			if (!world.hasRelation(WorldConstraint.Relation.ONTOP, wo, woRel)) {
				if (!woRel.getForm().equals("floor")) {
					moveAtleastOnce.addAll(world.objectsAbove(new WorldObject(woRel)));
					if (world.hasRelation(WorldConstraint.Relation.ABOVE, wo, woRel)) {
						moveAtleastTwice.add(new WorldObject(wo));
					}
				}
				moveAtleastOnce.addAll(world.objectsAbove(new WorldObject(wo)));
				moveAtleastOnce.add(new WorldObject(wo));
			} else {
				if (moveAtleastOnce.contains(new WorldObject(wo))) {
					moveAtleastTwice.add(new WorldObject(wo));
				}
			}
			break;
		case INSIDE:
			if (!world.hasRelation(WorldConstraint.Relation.INSIDE, wo, woRel)) {
				if (!woRel.getForm().equals("floor")) {
					moveAtleastOnce.addAll(world.objectsAbove(new WorldObject(woRel)));
					if (world.hasRelation(WorldConstraint.Relation.ABOVE, wo, woRel)) {
						moveAtleastTwice.add(new WorldObject(wo));
					}
				}
				moveAtleastOnce.addAll(world.objectsAbove(new WorldObject(wo)));
				moveAtleastOnce.add(new WorldObject(wo));
			} else {
				if (moveAtleastOnce.contains(new WorldObject(wo))) {
					moveAtleastTwice.add(new WorldObject(wo));
				}
			}
			break;
		case ABOVE:
			if (!world.hasRelation(WorldConstraint.Relation.ABOVE, wo, woRel)) {
				moveAtleastOnce.addAll(world.objectsAbove(wo));
				moveAtleastOnce.add(new WorldObject(wo));
				if (wo.getSize().equals("large")) {
					for (WorldObject w : world.objectsAbove(new WorldObject(woRel))) {
						if (w.getSize().equals("small")) {
							moveAtleastOnce.add(new WorldObject(w));
						}
					}
				}
				// TODO: for wo small objects, add objects above woWel which it
				// cannot be placed above; such as balls
			} else {
				if (moveAtleastOnce.contains(new WorldObject(wo))) {
					moveAtleastTwice.add(new WorldObject(wo));
				}
			}
			break;
		case UNDER: // TODO: do the same things as for above..
			if (!world.hasRelation(WorldConstraint.Relation.UNDER, wo, woRel)) {
				moveAtleastOnce.addAll(world.objectsAbove(woRel));
				moveAtleastOnce.add(new WorldObject(woRel));
			}
			break;
		case BESIDE:
			break;
		case LEFTOF:
			break;
		case RIGHTOF:
			break;
		}
		if (recursive) {
			Set<WorldObject> indirectRelations = ((RelativeWorldObject) wo).inferIndirectRelations();
			for (WorldObject o : indirectRelations) {
				HashMap<Integer, Set<WorldObject>> inobjs2move = calculateMinObjsToMove(o, minObjs, false);
				moveAtleastOnce.addAll(inobjs2move.get(1));
				moveAtleastTwice.addAll(inobjs2move.get(2));
			}
		}

		return minObjs;
	}

	public Goal getGoal() {
		return this.goal;
	}

	@Override
	public double getStateValue() {
		return actionsToGetHere.size() + this.heuristicValue * heuristicWeight;
	}

	@Override
	public boolean hasReachedGoal() {
		return world.isGoalFulFilled(goal);
	}

	public void setHeuristicWeight(double heuristicWeight) {
		this.heuristicWeight = heuristicWeight;
	}

	public List<String> getActionsToGetHere() {
		return actionsToGetHere;
	}

	public void setActionsToGetHere(List<String> bestActionsToGetHere) {
		this.actionsToGetHere = bestActionsToGetHere;
	}

	@Override
	public int compareTo(IAStarState o) {
		// Here one can decide whether one wants FIFO or LIFO behavior on queue.
		if (this.getStateValue() + correctionForMovingBothArms - (o.getStateValue() + o.correctionForMovingBothArms()) >= 0) {
			return 1;
		}
		return -1;
	}

	/**
	 * @return
	 */
	@Override
	public Collection<? extends IAStarState> expand() throws CloneNotSupportedException {
		if (world.getHoldings().size() == 2) {
			return expandTwoArms();
		} else {
			return expandOneArm();
		}
	}

	public Collection<? extends IAStarState> expandOneArm() throws CloneNotSupportedException {
		Collection<IAStarState> l = new LinkedList<IAStarState>();
		if (world.getHoldings().get(0).getClass() != EmptyWorldObject.class) {
			for (int i = 0; i < world.getStacks().size(); i++) {
				World w = world.clone();
				if (w.drop(i,0) && !visitedWorld.contains(w.getRepresentString())) {
					visitedWorld.add(w.getRepresentString());
					List<String> newList = new LinkedList<String>(actionsToGetHere);
					newList.add("drop0 " + i);
					WorldState state = new WorldState(w, goal, newList,0,0,0);
					l.add(state);
				}
			}
		} else {
			for (int i = 0; i < world.getStacks().size(); i++) {
				World w = world.clone();
				if (w.pick(i,0) && !visitedWorld.contains(w.getRepresentString())) {
					visitedWorld.add(w.getRepresentString());
					List<String> newList = new LinkedList<String>(actionsToGetHere);
					newList.add("pick0 " + i);
					WorldState state = new WorldState(w, goal, newList,0,0,0);
					l.add(state);
				}
			}
		}
		return l;
	}

	public Collection<? extends IAStarState> expandTwoArms() throws CloneNotSupportedException {
		Collection<IAStarState> l = new LinkedList<IAStarState>();

		if (world.getHoldings().get(0).getClass() != EmptyWorldObject.class
				&& world.getHoldings().get(1).getClass() != EmptyWorldObject.class) {
			for (int i = 0; i < world.getStacks().size() - 1; i++) {
				for (int j = i + 1; j < world.getStacks().size(); j++) {
					// Drop both arms
					World dropBoth = world.clone();
					if (dropBoth.drop(i, 0)) {
						if (dropBoth.drop(j, 1) && !visitedWorld.contains(dropBoth.getRepresentString())) {
							visitedWorld.add(dropBoth.getRepresentString());
							List<String> newList = new LinkedList<String>(actionsToGetHere);
							newList.add("drop0 " + i);
							newList.add("drop1 " + j);
							if (printProgress) {
								newList.add("dropping from arm 1 to column " + i);
								newList.add("dropping from arm 2 to column " + j);
							}
							l.add(new WorldState(dropBoth, goal, newList, i, j, 0.0001));
						}
					}
				}
			}
		}

		if (world.getHoldings().get(0).getClass() != EmptyWorldObject.class
				&& world.getHoldings().get(1).getClass() == EmptyWorldObject.class) {
			for (int i = 0; i < world.getStacks().size() - 1; i++) {
				for (int j = i + 1; j < world.getStacks().size(); j++) {
					// Drop both arms
					World dropBoth = world.clone();
					if (dropBoth.drop(i, 0)) {
						if (dropBoth.pick(j, 1) && !visitedWorld.contains(dropBoth.getRepresentString())) {
							visitedWorld.add(dropBoth.getRepresentString());
							List<String> newList = new LinkedList<String>(actionsToGetHere);
							newList.add("drop0 " + i);
							newList.add("pick1 " + j);
							if (printProgress) {
								newList.add("dropping from arm 1 to column " + i);
								newList.add("picking from arm 2 to column " + j);
							}
							l.add(new WorldState(dropBoth, goal, newList, i, j, 0));
						}
					}
				}
			}
		}

		if (world.getHoldings().get(0).getClass() == EmptyWorldObject.class
				&& world.getHoldings().get(1).getClass() != EmptyWorldObject.class) {
			for (int i = 0; i < world.getStacks().size() - 1; i++) {
				for (int j = i + 1; j < world.getStacks().size(); j++) {
					// Drop both arms
					World dropBoth = world.clone();
					if (dropBoth.pick(i, 0)) {
						if (dropBoth.drop(j, 1) && !visitedWorld.contains(dropBoth.getRepresentString())) {
							visitedWorld.add(dropBoth.getRepresentString());
							List<String> newList = new LinkedList<String>(actionsToGetHere);
							newList.add("pick0 " + i);
							newList.add("drop1 " + j);
							if (printProgress) {
								newList.add("picking from arm 1 to column " + i);
								newList.add("dropping from arm 2 to column " + j);
							}
							l.add(new WorldState(dropBoth, goal, newList, i, j, 0));
						}
					}
				}
			}
		}

		// Drop arm1 and move arm2
		if (world.getHoldings().get(0).getClass() != EmptyWorldObject.class) {
			for (int i = 0; i < world.getStacks().size() - 1; i++) {
				World drop0move1 = world.clone();
				if (drop0move1.drop(i, 0) && !visitedWorld.contains(drop0move1.getRepresentString())) {
					visitedWorld.add(drop0move1.getRepresentString());
					List<String> newList = new LinkedList<String>(actionsToGetHere);
					int arm2Pos = arm2;
					if (i >= arm2) {
						newList.add("drop0 " + i);
						newList.add("move1 " + (i + 1));
						if (printProgress) {
							newList.add("dropping arm 1 to column" + i);
							newList.add("moving arm 2 to column" + (i + 1));
						}
						arm2Pos = i + 1;
					} else {
						newList.add("drop0 " + i);
						newList.add("move1 " + arm2);
						if (printProgress) {
							newList.add("dropping arm 1 to column" + i);
							newList.add("hold arm 2 in column" + arm2);
						}
					}
					l.add(new WorldState(drop0move1, goal, newList, i, arm2Pos, 0));
				}
			}
		}

		// Drop arm2 and move arm1
		if (world.getHoldings().get(1).getClass() != EmptyWorldObject.class) {
			for (int j = 1; j < world.getStacks().size(); j++) {
				World drop1move0 = world.clone();
				if (drop1move0.drop(j, 1) && !visitedWorld.contains(drop1move0.getRepresentString())) {
					visitedWorld.add(drop1move0.getRepresentString());
					List<String> newList = new LinkedList<String>(actionsToGetHere);
					int arm1Pos = arm1;
					if (j <= arm1) {
						newList.add("move0 " + (j - 1));
						newList.add("drop1 " + j);
						if (printProgress) {
							newList.add("moving arm 1 to column " + (j - 1));
							newList.add("dropping arm 2 to column " + j);
						}
						arm1Pos = j - 1;
					} else {
						newList.add("move0 " + arm1);
						newList.add("drop1 " + j);
						if (printProgress) {
							newList.add("hold arm 1 in column" + arm1);
							newList.add("dropping arm 2 to column " + j);
						}
					}

					l.add(new WorldState(drop1move0, goal, newList, arm1Pos, j, 0));
				}
			}
		}

		// Pick both arms
		if (world.getHoldings().get(0).getClass() == EmptyWorldObject.class
				&& world.getHoldings().get(1).getClass() == EmptyWorldObject.class) {
			for (int i = 0; i < world.getStacks().size() - 1; i++) {
				for (int j = i + 1; j < world.getStacks().size(); j++) {
					World pickBoth = world.clone();
					if (pickBoth.pick(i, 0)) {
						if (pickBoth.pick(j, 1) && !visitedWorld.contains(pickBoth.getRepresentString())) {
							visitedWorld.add(pickBoth.getRepresentString());
							List<String> newList = new LinkedList<String>(actionsToGetHere);
							newList.add("pick0 " + i);
							newList.add("pick1 " + j);
							if (printProgress) {
								newList.add("picking with arm 1 from column " + i);
								newList.add("picking with arm 2 from column " + j);
							}
							l.add(new WorldState(pickBoth, goal, newList, i, j, 0.0001));
						}
					}
				}
			}
		}

		// Pick arm1 and move arm2
		if (world.getHoldings().get(0).getClass() == EmptyWorldObject.class) {
			for (int i = 0; i < world.getStacks().size() - 1; i++) {
				World pick0move1 = world.clone();
				if (pick0move1.pick(i, 0) && !visitedWorld.contains(pick0move1.getRepresentString())) {
					visitedWorld.add(pick0move1.getRepresentString());
					List<String> newList = new LinkedList<String>(actionsToGetHere);
					int arm2Pos = arm2;
					if (i >= arm2) {
						newList.add("pick0 " + i);
						newList.add("move1 " + (i + 1));
						if (printProgress) {
							newList.add("picking with arm 1 from column " + i);
							newList.add("moving arm 2 to comulm " + (i + 1));
						}
						arm2Pos = i + 1;
					} else {
						newList.add("pick0 " + i);
						newList.add("move1 " + arm2);
						if (printProgress) {
							newList.add("picking with arm 1 from column " + i);
							newList.add("hold arm 2 in column" + arm2);
						}
					}
					l.add(new WorldState(pick0move1, goal, newList, i, arm2Pos, 0));
				}
			}
		}

		// Pick arm2 and move arm1
		if (world.getHoldings().get(1).getClass() == EmptyWorldObject.class) {
			for (int j = 1; j < world.getStacks().size(); j++) {
				World pick1move0 = world.clone();
				if (pick1move0.pick(j, 1) && !visitedWorld.contains(pick1move0.getRepresentString())) {
					visitedWorld.add(pick1move0.getRepresentString());
					List<String> newList = new LinkedList<String>(actionsToGetHere);
					int arm1Pos = arm1;
					if (j <= arm1) {
						newList.add("move0 " + (j - 1));
						newList.add("pick1 " + j);
						if (printProgress) {
							newList.add("moving arm 1 to column" + (j - 1));
							newList.add("picking with arm 2 from column " + j);
						}
						arm1Pos = j - 1;
					} else {
						newList.add("move0 " + arm1);
						newList.add("pick1 " + j);
						if (printProgress) {
							newList.add("hold arm 1 in column " + arm1);
							newList.add("picking with arm 2 from column " + j);
						}
					}
					l.add(new WorldState(pick1move0, goal, newList, arm1Pos, j, 0));
				}
			}
		}
		return l;
	}

	public World getWorld() {
		return world;
	}

	@Override
	public double correctionForMovingBothArms() {
		return this.correctionForMovingBothArms;
	}

}
