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
		return data;
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
	
	public abstract <R,A> R accept(INodeVisitor<R,A> v, A arg) throws Interpreter.InterpretationException;
	
	public abstract <R,A> R accept(IActionVisitor<R,A> v, A arg) throws Interpreter.InterpretationException;
}
