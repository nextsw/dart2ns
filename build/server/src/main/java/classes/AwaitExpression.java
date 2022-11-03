package classes;

public class AwaitExpression extends Expression {
  public Expression exp;

  public AwaitExpression(Expression exp) {
    this.exp = exp;
  }
}
