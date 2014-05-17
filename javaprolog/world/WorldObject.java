package world;

public class WorldObject implements Cloneable{

    private String id;
    private String form;
	private String size;
	private String color;

    /**
     * Sets all fields to null.
     */
    public WorldObject(){}

    public WorldObject(String form, String size, String color) {
        this.form = form;
        this.size = size;
        this.color = color;
        this.id = "";
    }

    
    public WorldObject(String form, String size, String color, String id) {
        this.form = form;
        this.size = size;
        this.color = color;
        this.id = id;
    }

    /**
     * Copies the properties of the argument
     * @param wo
     */
    public WorldObject(WorldObject wo){
        this.form = wo.getForm();
        this.size = wo.getSize();
        this.color = wo.getColor();
        this.id = wo.getId();
    }

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
	public String toString() {
		return getId() + ": "+ getSize()+ " " + getColor()+" " + getForm();
	}

    public boolean matchesPattern(WorldObject match){
        return (match.form.equals(this.form) || match.form.equals("anyform")) && (match.size.equals(this.size) || match.size.equals("-")) && (match.color.equals(this.color) || match.color.equals("-"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorldObject that = (WorldObject) o;

        if (color != null ? !color.equals(that.color) : that.color != null) return false;
        if (form != null ? !form.equals(that.form) : that.form != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (size != null ? !size.equals(that.size) : that.size != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (form != null ? form.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }

    @Override
    public WorldObject clone() {
        try{
            return (WorldObject)super.clone();
        } catch (CloneNotSupportedException e){
            //erpy..
        }
        return null;
    }
}
