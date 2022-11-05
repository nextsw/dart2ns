package classes;

public class FnCallExpression extends Statement {
  public Expression on;
  public MethodCall call;

  public FnCallExpression(MethodCall call, Expression on) {
    this.call = call;
    this.on = on;
  }
}
