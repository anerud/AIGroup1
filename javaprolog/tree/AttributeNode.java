package tree;

import main.Interpreter;

import java.util.LinkedList;

public class AttributeNode extends Node{

	public AttributeNode(Node parent, String data) {
		super(parent, data);
	}

	@Override
	public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) throws Interpreter.InterpretationException {
		return v.visit(this, arg, arg2);
	}

	@Override
	public LinkedList<Node> getChildren() {
		return new LinkedList<Node>();
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
