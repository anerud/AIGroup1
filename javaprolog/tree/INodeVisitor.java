package tree;

public interface INodeVisitor<R, A> {

	public R visit(BasicEntityNode n, A a);

	public R visit(RelativeEntityNode n, A a);

	public R visit(RelativeNode n, A a);

	public R visit(PutNode n, A a);

	public R visit(TakeNode n, A a);

	public R visit(MoveNode n, A a);

	public R visit(FloorNode n, A a);

	public R visit(QuantifierNode n, A a);

	public R visit(ObjectNode n, A a);

	public R visit(AttributeNode n, A a);

	public R visit(RelationNode n, A a);


}
