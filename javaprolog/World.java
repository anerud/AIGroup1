import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Roland on 2014-03-26.
 */
public class World {

    private ArrayList<LinkedList<WorldObject>> stacks;

    private WorldObject holding;

    public World(ArrayList<LinkedList<WorldObject>> stacks, WorldObject holding){
        this.holding = holding;
        this.stacks = stacks;
    }

    public World(ArrayList<LinkedList<WorldObject>> stacks){
        this.holding = null;
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
     * @return a new LinkedList containing the objects in this world
     */
    public LinkedList<WorldObject> getWorldObjects(){
        LinkedList<WorldObject> objs = new LinkedList<WorldObject>();
        for(LinkedList<WorldObject> ll : stacks){
            objs.addAll(ll);
        }
        return objs;
    }

    public boolean isOntopOfStack(WorldObject wo){
        WorldObject top = topOfStack(columnOf(wo));
        return top != null && top.equals(wo);
    }

    public boolean isOnFloor(WorldObject wo){
        WorldObject bottom = bottomOfStack(columnOf(wo));
        return bottom != null && bottom.equals(wo);
    }

    /**
     *
     * @param wo
     * @return the column of the WorldObject, or -1 if the object is not contained in the world
     */
    public int columnOf(WorldObject wo){
        for(LinkedList<WorldObject> ll : stacks){
            if(ll.contains(wo)){
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
     * @return the WorldObject which is on top of the stack, or null if the stack has no WorldObjects
     */
    public WorldObject topOfStack(int column) {
        List<WorldObject> stack = stacks.get(column);
        if(!stack.isEmpty()){
            return stack.get(stack.size() - 1);
        } else {
            return null;
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
     * Determines if the objects have a certain geometric relation in the world
     * @param relation
     * @param wo
     * @param worel
     * @return
     */
    public boolean hasRelation(String relation, WorldObject wo, WorldObject worel) {
        if(relation.equals("ontop")){
            int col = columnOf(wo);
            if(isOnFloor(wo)){
                return worel.getForm().equals("floor");
            } else {
                int row = rowOf(wo);
                return stacks.get(col).get(row - 1).equals(worel);
            }
        }
        return false;
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
}
