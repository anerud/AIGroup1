package tree;

import java.util.LinkedList;

public class FloorNode extends Node{

	public FloorNode(Node parent, String data) {
		super(parent, data);
	}

	@Override
	public <R, A> R accept(INodeVisitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}

	@Override
	public LinkedList<Node> getChildren() {
		return new LinkedList<Node>();
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
