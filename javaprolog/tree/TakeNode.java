package tree;

import java.util.LinkedList;
import java.util.List;

public class TakeNode extends Node {

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
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R, A> R accept(INodeVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
