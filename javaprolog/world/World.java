package world;
import logic.LogicalExpression;

import java.util.*;

/**
 * Created by Roland on 2014-03-26.
 */
public class World {

    private ArrayList<LinkedList<WorldObject>> stacks;
    private List<WorldConstraint> constraints;
    private WorldObject holding;

    public World(ArrayList<LinkedList<WorldObject>> stacks, List<WorldConstraint> constrains, WorldObject holding){
        this.constraints = constrains;
        this.holding = holding;
        this.stacks = stacks;
    }

    /**
     * This constructor assumes there are no constraints and that nothing is being held
     * @param stacks
     */
    public World(ArrayList<LinkedList<WorldObject>> stacks){
        this.constraints = new ArrayList<WorldConstraint>();
        this.holding = null;
        this.stacks = stacks;
    }

    public World(ArrayList<LinkedList<WorldObject>> stacks, List<WorldConstraint> constrains){
        this.constraints = constrains;
        this.holding = null;
        this.stacks = stacks;
    }

    /**
     *  This constructor assumes there are no constraints
     * @param stacks
     * @param holding
     */
    public World(ArrayList<LinkedList<WorldObject>> stacks, WorldObject holding) {
        this.constraints = new ArrayList<WorldConstraint>();
        this.holding = holding;
        this.stacks = stacks;
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
    public Set<WorldObject> getWorldObjects(){
        HashSet<WorldObject> objs = new HashSet<WorldObject>();
        if(holding != null) {objs.add(holding);}
        for(LinkedList<WorldObject> ll : stacks){
            objs.addAll(ll);
        }
        return objs;
    }

    public boolean isOntopOfStack(WorldObject wo){
        WorldObject top = topOfStack(columnOf(wo));
        return top != null && top.getId().equals(wo.getId());
    }

    public boolean isOnFloor(WorldObject wo){
        WorldObject bottom = bottomOfStack(columnOf(wo));
        return bottom != null && bottom.equals(wo);
    }

    /**
     *
     * @param wo
     * @return the column of the WorldObject, or -1 if the object is not contained in the world. If the object is the floor, the first column which is empty is returned.
     */
    public int columnOf(WorldObject wo){
        for(LinkedList<WorldObject> ll : stacks){
            if(ll.contains(wo)){
                return stacks.indexOf(ll);
            }
            if(wo.getForm().equals("floor") && ll.isEmpty()){
                return stacks.indexOf(ll);
            }
        }
        return -1;
    }

    /**
     *
     * @param id the id of the desired WorldObject
     * @return the WorldObject which has the specified id or null if the world contains no such WorldObject
     */
    public WorldObject getWorldObject(String id) {
        if(holding != null && holding.getId().equals(id)) return holding;
        if(id.equals("floor")){
            return new WorldObject("floor", "floor", "floor", "floor");
        }
        for(LinkedList<WorldObject> ll : stacks){
            for(WorldObject wo : ll){
                if(wo.getId() != null && wo.getId().equals(id)){
                    return wo;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param column
     * @return the WorldObject which is on top of the stack, or a floor object if the stack has no WorldObjects
     */
    public WorldObject topOfStack(int column) {
        List<WorldObject> stack = stacks.get(column);
        if(!stack.isEmpty()){
            return stack.get(stack.size() - 1);
        } else {
            return new WorldObject("floor", "floor", "floor", "floor");
        }
    }

    /**
     *
     * @param column
     * @return the WorldObject which is on the bottom of the stack, or null if the stack has no WorldObjects
     */
    public WorldObject bottomOfStack(int column) {
        List<WorldObject> stack = stacks.get(column);
        if(!stack.isEmpty()){
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
        //TODO: check that the objects are compatible, that is, that object a can be placed ontop of object b
        if(!isStackEmpty(fromColumn)){
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
        if(holding == null && top!= null){
            holding = top;
            stacks.get(woColumn).removeLast();
            return true;
        }
        return false;
    }

    /**
     * Drops the object being held at the specified column
     * @param woColumn
     * @return true if the operation was successful
     */
    public boolean drop(int woColumn) {
        if(holding == null){
            return false;
        }
        WorldObject top = topOfStack(woColumn);
        if(top != null && !isValidRelation("ontop", holding, top)){
            return false;
        }  //This assumes it's always ok to put stuff directly on the floor
        stacks.get(woColumn).addLast(holding);
        holding = null;
        return true;
    }

    /**
     *
     * @param ontop
     * @param holding
     * @param top
     * @return
     */
    public boolean isValidRelation(String ontop, WorldObject holding, WorldObject top) {
        return true; //TODO
    }

    /**
     * Indexed from 0 where 0 means the object is on the floor and 1 means one step above the floor, etc.
     * @param wo
     * @return
     */
    private int rowOf(WorldObject wo) {
        for(LinkedList<WorldObject> ll : stacks){
            if(ll.contains(wo)){
                return ll.indexOf(wo);
            }
        }
        return -1;
    }

    public Set<WorldObject> filterByExistsInWorld(Set<WorldObject> toBeFiltered) {
        Set<WorldObject> toBeRetained = new HashSet<WorldObject>();
        for(WorldObject obj : toBeFiltered){
            //Check that the object exists
            Set<WorldObject> fil = getWorldObjects();
            filterByMatch(fil, obj);
            if(!fil.isEmpty()){
                toBeRetained.add(obj);
            }

            //Now remove the ones that do not match the relation (if any)
            if(obj instanceof RelativeWorldObject){
                if(!hasRelation(((RelativeWorldObject)obj).getRelation(), obj, ((RelativeWorldObject) obj).getRelativeTo())){
                    toBeRetained.remove(obj);
                }
            }
        }
        toBeFiltered.retainAll(toBeRetained);
        return toBeFiltered;
    }

    public void filterByMatch(Set<WorldObject> toBeFiltered, WorldObject match) {
        Set<WorldObject> toBeRetained = new HashSet<WorldObject>();
        for(WorldObject wo : toBeFiltered){
            if(wo.matchesPattern(match)){
                toBeRetained.add(wo);
            }
        }
        toBeFiltered.retainAll(toBeRetained);
    }

    /**
     * Retains the objects in toBeFiltered which are "relation" to theRelativeObjects
     * @param toBeFiltered
     * @param theRelativeObjects
     * @param relation
     */
    public void filterByRelation(Set<WorldObject> toBeFiltered, LogicalExpression<WorldObject> theRelativeObjects, WorldConstraint.Relation relation) {
        Set<WorldObject> toBeRetained = new HashSet<>();
        LogicalExpression.Operator op2 = theRelativeObjects.getOp();
        for(WorldObject wo : toBeFiltered){
            if(hasRelation(relation, wo, theRelativeObjects)){
                toBeRetained.add(wo);
            }
        }
        toBeFiltered.retainAll(toBeRetained);
    }

    /**
     * Determines if the objects have a certain geometric relation in the world
     *
     * @param relation
     * @param wo
     * @param worel
     * @return
     */
    public boolean hasRelation(WorldConstraint.Relation relation, WorldObject wo, WorldObject worel) {
        if(worel instanceof RelativeWorldObject && ((RelativeWorldObject) worel).getRelativeTo() != null){
            //TODO not sure if this method is necessary
            //1. filter out the possible objects
            //2. call this function again with the filtered objects
        }
        if(relation.equals(WorldConstraint.Relation.ONTOP) || relation.equals(WorldConstraint.Relation.INSIDE)){
            int col = columnOf(wo);
            if(isOnFloor(wo)){
                return worel.getForm().equals("floor");
            } else {
                int row = rowOf(wo);
                return stacks.get(col).get(row - 1).equals(worel);
            }
        } else if(relation.equals(WorldConstraint.Relation.UNDER)) {
            return hasRelation(WorldConstraint.Relation.ONTOP, worel, wo); //TODO: by "under", do we mean "directly under"?
        } else if(relation.equals(WorldConstraint.Relation.LEFTOF)) {
            return columnOf(wo) < columnOf(worel);
        } else if(relation.equals(WorldConstraint.Relation.RIGHTOF)){
            return hasRelation(WorldConstraint.Relation.LEFTOF, worel, wo);
        }
        return false;
    }

    public boolean hasRelation(RelativeWorldObject obj) {
        return hasRelation(obj.getRelation(), obj, obj.getRelativeTo());
    }

    /**
     * Recursively determines if the WorldObject fulfils the relation to the logical expression
     * @param relation
     * @param wo
     * @param theRelativeObjects
     * @return
     */
    public boolean hasRelation(WorldConstraint.Relation relation, WorldObject wo, LogicalExpression<WorldObject> theRelativeObjects) {
        LogicalExpression.Operator op = theRelativeObjects.getOp();
        if(op.equals(LogicalExpression.Operator.AND)){
            for(WorldObject wo1 : theRelativeObjects.getObjs()){
                if(!hasRelation(relation, wo, wo1)){
                    return false;
                }
            }
            for(LogicalExpression exp : theRelativeObjects.getExpressions()){
                if(!hasRelation(relation, wo, exp)){
                    return false;
                }
            }
            return true;
        } else {
            for(WorldObject wo1 : theRelativeObjects.getObjs()){
                if(hasRelation(relation, wo, wo1)){
                    return true;
                }
            }
            for(LogicalExpression exp : theRelativeObjects.getExpressions()){
                if(hasRelation(relation, wo, exp)){
                    return true;
                }
            }
            return false;
        }
    }
}
