package tree;

import main.Interpreter;

import java.util.LinkedList;

public class AttributeNode extends Node{
	
	public String getPluralForm() {
		return pluralForm;
	}

	@Override
	public String toNaturalString() {
		// TODO Auto-generated method stub
		if (getData().equals("anyform"))
			return "object";
		else
		 return getData();
	}
	
	public String toNaturalString(boolean plural) {
		if (plural == false ) return toNaturalString();
		if (getData().equals("anyform"))
			return "objects";
		else
		 return pluralForm;
	}

	public void setPluralForm(String pluralForm) {
		this.pluralForm = pluralForm;
	}

	private String pluralForm;

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
