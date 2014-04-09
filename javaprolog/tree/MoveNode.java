package tree;

import main.Interpreter;

import java.util.LinkedList;

public class MoveNode extends Node {

	private Node entityNode;
	private Node locationNode;

	public MoveNode(Node parent, String data) {
		super(parent, data);
	}

	public void setEntityNode(Node entityNode) {
		this.entityNode = entityNode;
	}

	public void setLocationNode(Node locationNode) {
		this.locationNode = locationNode;
	}

	public Node getEntityNode() {
		return entityNode;
	}

	public Node getLocationNode() {
		return locationNode;
	}

	@Override
	public LinkedList<Node> getChildren() {
		LinkedList<Node> childs = new LinkedList<Node>();
		childs.add(entityNode);
		childs.add(locationNode);
		return childs;
	}


	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) throws Interpreter.InterpretationException, CloneNotSupportedException {
		return v.visit(this, arg);
	}

	@Override
	public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
