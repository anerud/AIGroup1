package tree;

import logic.Quantifier;
import main.Interpreter;

import java.util.LinkedList;

public class RelativeEntityNode extends Node {

	@Override
	public String toNaturalString() {
		// TODO Auto-generated method stub
		boolean plural = (quantifierNode.getQuantifier() ==  Quantifier.ALL);
		return quantifierNode.toNaturalString()+ " " + objectNode.toNaturalString(plural) + " " + locationNode.toNaturalString();
	}
	public String toNaturalString(boolean plural) {
		
		if (plural)
		// TODO Auto-generated method stub
		return  objectNode.toNaturalString(true) + " " + locationNode.toNaturalString();
		else  return toNaturalString();
		
	}

	private QuantifierNode quantifierNode;
	private Node objectNode;
	private RelativeNode locationNode;

	public RelativeEntityNode(Node parent, String data) {
		super(parent, data);
	}

    public void setLocationNode(Node locationNode) {
        this.locationNode = (RelativeNode)locationNode;
    }

	public void setLocationNode(RelativeNode locationNode) {
		this.locationNode = locationNode;
	}

	public void setObjectNode(Node objectNode) {
		this.objectNode = objectNode;
	}

    public void setQuantifierNode(Node quantifierNode) {
        this.quantifierNode = (QuantifierNode)quantifierNode;
    }

    public void setQuantifierNode(QuantifierNode quantifierNode) {
		this.quantifierNode = quantifierNode;
	}

	public Node getObjectNode() {
		return objectNode;
	}

	public RelativeNode getLocationNode() {
		return locationNode;
	}

	public QuantifierNode getQuantifierNode() {
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
	public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) throws Interpreter.InterpretationException, CloneNotSupportedException {
		return v.visit(this, arg, arg2);
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
