package world;

public class EmptyWorldObject extends WorldObject {

	public EmptyWorldObject() {

	}

	@Override
	public String getColor() {
		return "empty";
	}

	@Override
	public String getForm() {
		return "empty";
	}

	public String getSize() {
		return "empty";
	};

	@Override
	public boolean matchesPattern(WorldObject match) {
		return false;
	}

	@Override
	public String getId() {
		return "-1";
	}

	@Override
	public boolean equals(Object o) {
		return false;
	}
}