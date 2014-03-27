package tree;

import main.Goal;
import main.Interpreter;

import java.util.List;

public interface INodeVisitor<R, A> {

	public R visit(BasicEntityNode n, A a) throws Interpreter.InterpretationException;

	public R visit(RelativeEntityNode n, A a) throws Interpreter.InterpretationException;

	public R visit(RelativeNode n, A a) throws Interpreter.InterpretationException;

	public R visit(FloorNode n, A a);

	public R visit(QuantifierNode n, A a);

	public R visit(ObjectNode n, A a);

	public R visit(AttributeNode n, A a);

	public R visit(RelationNode n, A a);


}
