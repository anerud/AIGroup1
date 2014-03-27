package tree;

import java.util.LinkedList;

public class RelationNode extends Node{

	public RelationNode(Node parent, String data) {
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

}