package tree;

import main.Interpreter;

import java.util.LinkedList;

public class BasicEntityNode extends Node {

	private QuantifierNode quantityNode;
	private Node objectNode;

	public BasicEntityNode(Node parent, String data) {
		super(parent, data);
	}

    public void setQuantifierNode(Node quantityNode) {
        this.quantityNode = (QuantifierNode)quantityNode;
    }

	public void setQuantifierNode(QuantifierNode quantityNode) {
		this.quantityNode = quantityNode;
	}

	public void setObjectNode(Node objectNode) {
		this.objectNode = objectNode;
	}

	public Node getObjectNode() {
		return objectNode;
	}

	public QuantifierNode getQuantifierNode() {
		return quantityNode;
	}

	@Override
	public LinkedList<Node> getChildren() {
		LinkedList<Node> childs = new LinkedList<Node>();
		childs.add(quantityNode);
		childs.add(objectNode);
		return childs;
	}

    @Override
    public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) throws Interpreter.InterpretationException {
        return v.visit(this, arg, arg2);
    }

    @Override
    public <R, A> R accept(IActionVisitor<R, A> v, A arg) throws Interpreter.InterpretationException {
        // TODO Auto-generated method stub
        return null;
    }
}
