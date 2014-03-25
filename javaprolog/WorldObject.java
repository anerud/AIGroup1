public class WorldObject {

	String form;
	String size;
	String color;

	public WorldObject(String form, String size, String color) {
		this.form = form;
		this.size = size;
		this.color = color;
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
	
	@Override
	public String toString() {
		return "form: "+form+" size: "+size+" color: "+color;
	}

}
