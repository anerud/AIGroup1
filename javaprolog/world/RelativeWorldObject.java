package world;

import logic.LogicalExpression;

/**
 * Created by Roland on 2014-03-28.
 */
public class RelativeWorldObject extends WorldObject {

    private WorldConstraint.Relation relation;
    private LogicalExpression<WorldObject> relativeTo;

    public RelativeWorldObject(WorldObject obj, LogicalExpression<WorldObject> relativeTo, WorldConstraint.Relation relation) {
        super(obj.getForm(), obj.getSize(), obj.getColor(), obj.getId());
        this.relativeTo = relativeTo;
        this.relation = relation;
    }

    public RelativeWorldObject(LogicalExpression<WorldObject> relativeTo, WorldConstraint.Relation relation) {
        this.relativeTo = relativeTo;
        this.relation = relation;
    }

    public RelativeWorldObject(String form, String size, String color, String id, LogicalExpression<WorldObject> relativeTo, WorldConstraint.Relation relation) {
        super(form, size, color, id);
        this.relativeTo = relativeTo;
        this.relation = relation;
    }

    /**
     * Sets the parameters of this WorldObject to those of the specified WorldObject
     * @param obj
     */
    public void setObj(WorldObject obj){
        setForm(obj.getForm());
        setSize(obj.getSize());
        setColor(obj.getColor());
        setId(obj.getId());
    }


    public WorldConstraint.Relation getRelation() {
        return relation;
    }

    public void setRelation(WorldConstraint.Relation relation) {
        this.relation = relation;
    }

    public LogicalExpression<WorldObject> getRelativeTo() {
        return relativeTo;
    }

    public void setRelativeTo(LogicalExpression<WorldObject> relativeTo) {
        this.relativeTo = relativeTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RelativeWorldObject that = (RelativeWorldObject) o;

        if (relation != that.relation) return false;
        if (relativeTo != null ? !relativeTo.equals(that.relativeTo) : that.relativeTo != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (relation != null ? relation.hashCode() : 0);
        result = 31 * result + (relativeTo != null ? relativeTo.hashCode() : 0);
        return result;
    }
}
