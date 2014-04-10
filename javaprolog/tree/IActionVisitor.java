package tree;


import main.Interpreter;

public interface IActionVisitor<R,A> {

	public R visit(PutNode n, A a) throws Interpreter.InterpretationException, CloneNotSupportedException;

	public R visit(TakeNode n, A a) throws Interpreter.InterpretationException, CloneNotSupportedException;

	public R visit(MoveNode n, A a) throws Interpreter.InterpretationException, CloneNotSupportedException;

	
}
