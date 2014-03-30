package tree;

import logic.LogicalExpression;
import main.Goal;
import main.Interpreter;
import world.WorldObject;

import java.util.List;

public interface INodeVisitor<R, A, A2> {

	public R visit(BasicEntityNode n, A a, A2 a2) throws Interpreter.InterpretationException;

	public R visit(RelativeEntityNode n, A a, A2 a2) throws Interpreter.InterpretationException;

	public R visit(RelativeNode n, A a, A2 a2) throws Interpreter.InterpretationException;

	public R visit(FloorNode n, A a, A2 a2);

	public R visit(QuantifierNode n, A a, A2 a2) throws Interpreter.InterpretationException;

	public R visit(ObjectNode n, A a, A2 a2) throws Interpreter.InterpretationException;

	public R visit(AttributeNode n, A a, A2 a2) throws Interpreter.InterpretationException;

	public R visit(RelationNode n, A a, A2 a2) throws Interpreter.InterpretationException;


}
