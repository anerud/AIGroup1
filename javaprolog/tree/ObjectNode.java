package tree;

import main.Interpreter;

import java.util.LinkedList;

public class ObjectNode extends Node {

	@Override
	public String toNaturalString(boolean plural) {
		StringBuilder out = new StringBuilder();
		

        
        String size = sizeNode.toNaturalString();
        String form = formNode.toNaturalString(plural);
        String color = colorNode.toNaturalString();
        
        
        if (!size.equals("-"))
        {  out.append(size);
           out.append(" ");
           if(!color.equals("-"))
        	   out.append(",");
           
        }
        
        if (!color.equals("-"))
        {  out.append(color);
           out.append(" ");   
        }
        
        
      
        out.append(form);
        
        return out.toString();
        
            
	}

	@Override
	public String toNaturalString() {
		
		return toNaturalString(false);
	}

	private AttributeNode formNode;
	private AttributeNode sizeNode;
	private AttributeNode colorNode;

	public ObjectNode(Node parent, String data) {
		super(parent, data);
	}

	public void setFormNode(AttributeNode formNode) {
		this.formNode = formNode;
	}

	public void setSizeNode(AttributeNode sizeNode) {
		this.sizeNode = sizeNode;
	}

	public void setColorNode(AttributeNode colorNode) {
		this.colorNode = colorNode;
	}

	public Node getColorNode() {
		return colorNode;
	}

	public Node getFormNode() {
		return formNode;
	}

	public Node getSizeNode() {
		return sizeNode;
	}

	@Override
	public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) throws Interpreter.InterpretationException {
		return v.visit(this, arg, arg2);
	}

	@Override
	public LinkedList<Node> getChildren() {
		LinkedList<Node> childs = new LinkedList<Node>();
		childs.add(formNode);
		childs.add(sizeNode);
		childs.add(colorNode);
		return childs;
	}

	@Override
	public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
		
		return null;
	}

}