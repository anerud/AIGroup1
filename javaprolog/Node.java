import java.util.LinkedList;

public class Node {
	private LinkedList<Node> childs;
	private Node parent;
	private String data;

	public Node(Node parent, String data) {
		this.data = data;
		this.parent = parent;
		childs = new LinkedList<Node>();
	}

	public Node getParent() {
		return parent;
	}

	public LinkedList<Node> getChildren() {
		return childs;
	}

	public String getData() {
		return data;
	}

	public void setChildren(LinkedList<Node> childs) {
		this.childs = childs;
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
}
