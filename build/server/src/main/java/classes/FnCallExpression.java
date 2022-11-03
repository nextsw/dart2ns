package classes;

public class FnCallExpression extends Expression {
  public Expression on;
  public MethodParams params;

  public FnCallExpression(Expression on, MethodParams params) {
    this.on = on;
    this.params = params;
  }
}
