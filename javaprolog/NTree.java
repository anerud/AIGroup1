import java.util.LinkedList;

public class NTree {

	private Node root;

	public NTree(Node n) {
		root = n;
	}

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public LinkedList<String> getAsList() {
		LinkedList<String> s = new LinkedList<String>();
		s.add(root.getData());
		for (Node n : root.getChildren()) {
			s.addAll(n.toTree().getAsList());
		}
		return s;
	}

	@Override
	public String toString() {
		return root.toIndentString(0);
	}

}
