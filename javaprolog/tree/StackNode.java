package tree;

import main.Interpreter;

import java.util.LinkedList;

public class StackNode extends Node {

	private Node thingsToStackNode;

	
	public StackNode(Node parent, String data) {
		super(parent, data);
	}




	@Override
	public String toNaturalString() {
		// TODO Auto-generated method stub
		return "Stack " + thingsToStackNode.toNaturalString();
	}
		
	

	public Node getThingsToStackNode() {
		return thingsToStackNode;
	}

	public void setThingsToStackNode(Node thingsToStackNode) {
		this.thingsToStackNode = thingsToStackNode;
	}






	@Override
	public LinkedList<Node> getChildren() {
		LinkedList<Node> childs = new LinkedList<Node>();
		childs.add(thingsToStackNode);
		return childs;
	}

	@Override
	public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) {
		return null;
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) throws Interpreter.InterpretationException, CloneNotSupportedException {
		return v.visit(this, arg);
	}

}
