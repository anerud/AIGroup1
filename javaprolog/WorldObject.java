public class WorldObject {

    private String id;
    private String form;
	private String size;
	private String color;

	public WorldObject(String form, String size, String color) {
		this.form = form;
		this.size = size;
		this.color = color;
	}

    public WorldObject(String form, String size, String color, String id) {
        this.form = form;
        this.size = size;
        this.color = color;
        this.id = id;
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
		return "form: "+form+" size: "+size+" color: "+color;
	}

    public boolean matchesPattern(WorldObject match){
        return (match.form.equals(this.form) || match.form.equals("anyform")) && (match.size.equals(this.size) || match.size.equals("-")) && (match.color.equals(this.color) || match.color.equals("-"));
    }

}
