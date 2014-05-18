package world;

import java.util.HashSet;
import java.util.Set;

/**
 * Note that RelativeWorldObjects can be chained arbitrarily.
 *
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

    public RelativeWorldObject(WorldObject relativeTo, WorldConstraint.Relation relation) {
        this.relativeTo = relativeTo;
        this.relation = relation;
    }

    public RelativeWorldObject(String form, String size, String color, String id, WorldObject relativeTo, WorldConstraint.Relation relation) {
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

    public WorldObject getRelativeTo() {
        return relativeTo;
    }

    public void setRelativeTo(WorldObject relativeTo) {
        this.relativeTo = relativeTo;
    }


    /**
     * Calculates indirect relations for the top object in this RelativeWorldObject chain. For example, if (ONTOP obj1 (ONTOP obj2 (ONTOP obj3 obj4))), the method returns a set with the relations (ABOVE obj1 (ONTOP obj3 obj4)) and (ABOVE obj1 obj4).
     * The floor is ignored for e.g. above floor.
     * @return
     */
    public Set<WorldObject> inferIndirectRelations() {
        Set<WorldObject> relobjs = new HashSet<>();
        WorldObject woRel = this.getRelativeTo();

        //Infer above-relations..
        if(!((woRel instanceof RelativeWorldObject) && (this.relation.equals(WorldConstraint.Relation.ONTOP) || this.relation.equals(WorldConstraint.Relation.INSIDE) || this.relation.equals(WorldConstraint.Relation.ABOVE)) && (((RelativeWorldObject) woRel).getRelation().equals(WorldConstraint.Relation.ONTOP) || ((RelativeWorldObject) woRel).getRelation().equals(WorldConstraint.Relation.INSIDE) || ((RelativeWorldObject) woRel).getRelation().equals(WorldConstraint.Relation.ABOVE)))){
            return relobjs;
        }
        WorldConstraint.Relation relation = ((RelativeWorldObject)woRel).getRelation();
        do {
            WorldObject relativeTo = ((RelativeWorldObject) woRel).getRelativeTo();
            if(!relativeTo.getForm().equals("floor")){
                RelativeWorldObject rwo = new RelativeWorldObject(new WorldObject(this), relativeTo, WorldConstraint.Relation.ABOVE);
                relobjs.add(rwo);
            }
            relation = ((RelativeWorldObject) woRel).getRelation();
            woRel = relativeTo;
        } while (woRel instanceof RelativeWorldObject && ((relation.equals(WorldConstraint.Relation.ONTOP) || relation.equals(WorldConstraint.Relation.INSIDE) || relation.equals(WorldConstraint.Relation.ABOVE))));
        //TODO: do other relations as well...
        return relobjs;
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
