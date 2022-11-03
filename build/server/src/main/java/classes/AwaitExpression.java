package classes;

public class AwaitExpression extends Statement {
  public Expression exp;

  public AwaitExpression(Expression exp) {
    this.exp = exp;
  }
}
