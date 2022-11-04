package classes;

public class ConstExpression extends Statement {
  public Expression exp;

  public ConstExpression(Expression exp) {
    this.exp = exp;
  }
}
