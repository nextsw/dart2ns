package classes;

public class ConstExpression extends Expression {
  public Expression exp;

  public ConstExpression(Expression exp) {
    this.exp = exp;
  }
}
