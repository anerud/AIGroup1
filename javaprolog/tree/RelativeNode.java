package tree;

import main.Interpreter;

import java.util.LinkedList;
import java.util.List;

public class RelativeNode extends Node {

	private Node relationNode;
	private Node entityNode;

	public RelativeNode(Node parent, String data) {
		super(parent, data);
	}

	public void setRelationNode(Node relationNode) {
		this.relationNode = relationNode;
	}

	public void setEntityNode(Node entityNode) {
		this.entityNode = entityNode;
	}

	public Node getEntityNode() {
		return entityNode;
	}

	public Node getRelationNode() {
		return relationNode;
	}

	@Override
	public <R, A> R accept(INodeVisitor<R, A> v, A arg) throws Interpreter.InterpretationException {
		return v.visit(this, arg);
	}

	@Override
	public LinkedList<Node> getChildren() {
		LinkedList<Node> childs = new LinkedList<Node>();
		childs.add(relationNode);
		childs.add(entityNode);
		return childs;
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
