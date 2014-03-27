package tree;

import java.util.LinkedList;
import java.util.List;

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
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}

	@Override
	public <R, A> R accept(INodeVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
