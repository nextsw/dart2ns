package classes;

public class AwaitExpression extends Expression {
	Expression exp;
	public AwaitExpression(Expression exp) {
		this.exp = exp;
	}
}
