package tree;


public interface IActionVisitor<R,A> {

	public R visit(PutNode n, A a);

	public R visit(TakeNode n, A a);

	public R visit(MoveNode n, A a);

	
}
