package tree;

import main.Interpreter;

import java.util.LinkedList;
import java.util.List;

public class RelativeEntityNode extends Node {

	private Node quantifierNode;
	private Node objectNode;
	private Node locationNode;

	public RelativeEntityNode(Node parent, String data) {
		super(parent, data);
	}

	public void setLocationNode(Node locationNode) {
		this.locationNode = locationNode;
	}

	public void setObjectNode(Node objectNode) {
		this.objectNode = objectNode;
	}

	public void setQuantifierNode(Node quantifierNode) {
		this.quantifierNode = quantifierNode;
	}

	public Node getObjectNode() {
		return objectNode;
	}

	public Node getLocationNode() {
		return locationNode;
	}

	public Node getQuantifierNode() {
		return quantifierNode;
	}

	@Override
	public LinkedList<Node> getChildren() {
		LinkedList<Node> childs = new LinkedList<Node>();
		childs.add(quantifierNode);
		childs.add(objectNode);
		childs.add(locationNode);
		return childs;
	}

	@Override
	public <R, A> R accept(INodeVisitor<R, A> v, A arg) throws Interpreter.InterpretationException {
		return v.visit(this, arg);
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
