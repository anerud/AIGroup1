package tree;

import java.util.LinkedList;
import java.util.List;

public class BasicEntityNode extends Node {

	private Node quantityNode;
	private Node objectNode;

	public BasicEntityNode(Node parent, String data) {
		super(parent, data);
	}

	public void setQuantifierNode(Node quantityNode) {
		this.quantityNode = quantityNode;
	}

	public void setObjectNode(Node objectNode) {
		this.objectNode = objectNode;
	}

	public Node getObjectNode() {
		return objectNode;
	}

	public Node getQuantifierNode() {
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
	public <R, A> R accept(INodeVisitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
