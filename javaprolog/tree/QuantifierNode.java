package tree;

import java.util.LinkedList;

public class QuantifierNode extends Node{

	public QuantifierNode(Node parent, String data) {
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