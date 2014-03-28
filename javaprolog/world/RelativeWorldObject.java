package world;

/**
 * Created by Roland on 2014-03-28.
 */
public class RelativeWorldObject extends WorldObject {

    private WorldConstraint.Relation relation;
    private WorldObject relativeTo;

    public RelativeWorldObject(WorldObject obj, WorldObject relativeTo, WorldConstraint.Relation relation) {
        super(obj.getForm(), obj.getSize(), obj.getColor(), obj.getId());
        this.relativeTo = relativeTo;
        this.relation = relation;
    }

    public RelativeWorldObject(String form, String size, String color, WorldObject relativeTo, WorldConstraint.Relation relation) {
        super(form, size, color);
        this.relativeTo = relativeTo;
        this.relation = relation;
    }

    public RelativeWorldObject(String form, String size, String color, String id, WorldObject relativeTo, WorldConstraint.Relation relation) {
        super(form, size, color, id);
        this.relativeTo = relativeTo;
        this.relation = relation;
    }


    public WorldConstraint.Relation getRelation() {
        return relation;
    }

    public void setRelation(WorldConstraint.Relation relation) {
        this.relation = relation;
    }

    public WorldObject getRelativeTo() {
        return relativeTo;
    }

    public void setRelativeTo(WorldObject relativeTo) {
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
