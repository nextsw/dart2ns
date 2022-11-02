package classes;

public class FnCallExpression extends Expression {
	Expression on;
	MethodParams params;
	public FnCallExpression(Expression on, MethodParams params) {
		this.on = on;
		this.params = params;
	}
}
