package world;

public class EmptyWorldObject extends WorldObject{

	public EmptyWorldObject() {
	
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
