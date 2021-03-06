package tree;

import main.Interpreter;

import java.util.LinkedList;

public class TakeNode extends Node {

	@Override
	public String toNaturalString() {
		
		return "take " +  entityNode.toNaturalString();
	}

	private Node entityNode;

	public TakeNode(Node parent, String data) {
		super(parent, data);
	}

	public void setEntityNode(Node entityNode) {
		this.entityNode = entityNode;
	}

	public Node getEntityNode() {
		return entityNode;
	}

	@Override
	public LinkedList<Node> getChildren() {
		LinkedList<Node> childs = new LinkedList<Node>();
		childs.add(entityNode);
		return childs;
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) throws Interpreter.InterpretationException, CloneNotSupportedException {
		return v.visit(this, arg);
	}

	@Override
	public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) {
		
		return null;
	}

}
