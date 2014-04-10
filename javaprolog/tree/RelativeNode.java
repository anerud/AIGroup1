package tree;

import main.Interpreter;

import java.util.LinkedList;

public class RelativeNode extends Node {

	private RelationNode relationNode;
	private Node entityNode;

	public RelativeNode(Node parent, String data) {
		super(parent, data);
	}

    public void setRelationNode(Node relationNode) {
        this.relationNode = (RelationNode)relationNode;
    }

	public void setRelationNode(RelationNode relationNode) {
		this.relationNode = relationNode;
	}

	public void setEntityNode(Node entityNode) {
		this.entityNode = entityNode;
	}

	public Node getEntityNode() {
		return entityNode;
	}

	public RelationNode getRelationNode() {
		return relationNode;
	}

	@Override
	public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) throws Interpreter.InterpretationException, CloneNotSupportedException {
		return v.visit(this, arg, arg2);
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
