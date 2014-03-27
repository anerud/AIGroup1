package tree;

import java.util.List;

import main.Goal;

public interface IActionVisitor<R,A> {

	public List<Goal> visit(PutNode n, A a);

	public List<Goal> visit(TakeNode n, A a);

	public List<Goal> visit(MoveNode n, A a);

	
}
