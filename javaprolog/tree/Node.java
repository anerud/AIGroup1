package tree;

import main.Interpreter;

import java.util.LinkedList;

public abstract class Node {
	private Node parent;
	private String data;

	public Node(Node parent, String data) {
		this.data = data;
		this.parent = parent;
	}

	public Node getParent() {
		return parent;
	}

	public String getData() {
		return data;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public NTree toTree(){
		return new NTree(this);
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public abstract LinkedList<Node> getChildren();
	
	@Override
	public String toString() {
		return toNaturalString();
	}
	
	public String toIndentString(int tabs){
		String tab = "";
		for(int i = 0; i< tabs;i++){
			tab += "\t";
		}
		String s = tab+data+"\n";
		for(Node n : getChildren()){
			s += n.toIndentString(tabs+1);
		}
		return s;
	}
	
	
	// natural language represenation
	public String toNaturalString(){
        return data;
        
	}
	// natural language represenation, possibly in plural
	public String toNaturalString(boolean plural) {
		return toNaturalString();
	}
	
	
	
	
	public abstract <R,A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) throws Interpreter.InterpretationException, CloneNotSupportedException;
	
	public abstract <R,A> R accept(IActionVisitor<R,A> v, A arg) throws Interpreter.InterpretationException, CloneNotSupportedException;


}
