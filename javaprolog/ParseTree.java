import java.util.ArrayList;
import java.util.List;

/**
 * A parse tree. Iteration over the tree is done by navigating from the current state, which is a node in the tree.
 */
public class ParseTree {
	
	Node root;
	Node currentNode;
	private boolean initialized = false;
	
	public ParseTree() {
		this.root = new Node("");
		currentNode = root;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		returnToRoot();
		nextChild();
		currentNode.startIteration();
		sb.append(currentNode);
		while(currentNode != root) {
			if(currentNode.hasMoreChildren()) {
				nextChild();
				currentNode.startIteration();
				sb.append("(" + currentNode);
			} else {
				parent();
				if(currentNode.hasMoreChildren()) {
					nextChild();
					currentNode.startIteration();
					sb.append("," + currentNode);
				} else {
					if(currentNode != root) {
						sb.append(")");
					}
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Sets the current node to the parent and returns it
	 * @return
	 */
	public Node parent() {
		Node parent = currentNode.parent;
		currentNode = parent;
		return parent;
	}
	
	/**
	 * Returns the parent of the current node
	 * @return
	 */
	public Node getParent() {
		return currentNode.parent;
	}
	
	/**
	 * Returns and moves to the next child
	 * @return
	 */
	public Node nextChild(){
		currentNode = currentNode.nextChild();
		currentNode.startIteration();
		return currentNode;
	}
	
	/**
	 * Only returns the next child
	 * @return
	 */
	public Node getNextChild(){
		return currentNode.children.get(currentNode.currentChild);
	}
	
	/**
	 * Adds a leaf without going down in it
	 * @param o
	 */
	public void addLeaf(String o) {
		currentNode.addChild(new Node(o, currentNode));
	}
	
	/**
	 * Adds a child and go down in it
	 * @param o
	 */
	public void addChild(String o){
		Node child = new Node(o, currentNode);
		currentNode.addChild(child);
		currentNode = child;
	}
	
	/**
	 * Sets the current node to the root
	 */
	public void returnToRoot(){
		currentNode = root;
	}
	
	/**
	 * A node in the tree:
	 *  - String describing the node
	 *  - Its parent
	 *  - Its Children
	 *  - Remembers the current child one is investigating
	 * @author Sebbe
	 *
	 */
	public class Node{
		String object;
		Node parent;
		List<Node> children = new ArrayList<Node>();
		int currentChild = 0;
		
		public Node(String o) {
			object = o;
		}
		
		public Node(String o, Node p) {
			object = o;
			parent = p;
		}
		
		public void addChild(Node n) {
			children.add(n);
		}
		
		public Node nextChild() {
			return children.get(currentChild++);
		}
		
		@Override
		public String toString() {
			return object;
		}
		
		public boolean hasMoreChildren(){
			return currentChild < children.size();
		}
		
		public void startIteration(){
			currentChild = 0;
		}
		
	}
	
}

