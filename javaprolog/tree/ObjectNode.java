package tree;

import main.Interpreter;

import java.util.LinkedList;

public class ObjectNode extends Node {

	private Node formNode;
	private Node sizeNode;
	private Node colorNode;

	public ObjectNode(Node parent, String data) {
		super(parent, data);
	}

	public void setFormNode(Node formNode) {
		this.formNode = formNode;
	}

	public void setSizeNode(Node sizeNode) {
		this.sizeNode = sizeNode;
	}

	public void setColorNode(Node colorNode) {
		this.colorNode = colorNode;
	}

	public Node getColorNode() {
		return colorNode;
	}

	public Node getFormNode() {
		return formNode;
	}

	public Node getSizeNode() {
		return sizeNode;
	}

	@Override
	public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) throws Interpreter.InterpretationException {
		return v.visit(this, arg, arg2);
	}

	@Override
	public LinkedList<Node> getChildren() {
		LinkedList<Node> childs = new LinkedList<Node>();
		childs.add(formNode);
		childs.add(sizeNode);
		childs.add(colorNode);
		return childs;
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

}