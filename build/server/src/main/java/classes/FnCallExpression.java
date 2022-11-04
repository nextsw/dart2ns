package classes;

public class FnCallExpression extends Statement {
  public Expression on;
  public MethodCall call;

  public FnCallExpression(Expression on, MethodCall call) {
    this.on = on;
    this.call = call;
  }
}
