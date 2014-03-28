package tree;

import main.Interpreter;

import java.util.*;

public class PutNode extends Node {

	private Node locationNode;

	public PutNode(Node parent, String data) {
		super(parent, data);
	}

	public void setLocationNode(Node locationNode) {
		this.locationNode = locationNode;
	}

	public Node getLocationNode() {
		return locationNode;
	}

	@Override
	public LinkedList<Node> getChildren() {
		LinkedList<Node> childs = new LinkedList<Node>();
		childs.add(locationNode);
		return childs;
	}

	@Override
	public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) {
		return null;
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) throws Interpreter.InterpretationException {
		return v.visit(this, arg);
	}

}
