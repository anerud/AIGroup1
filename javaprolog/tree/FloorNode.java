package tree;

import java.util.LinkedList;

public class FloorNode extends Node{

	@Override
	public String toNaturalString() {
		
		return "the floor";
	}

	public FloorNode(Node parent, String data) {
		super(parent, data);
	}

	@Override
	public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) {
		return v.visit(this, arg, arg2);
	}

	@Override
	public LinkedList<Node> getChildren() {
		return new LinkedList<Node>();
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		
		return null;
	}

}
