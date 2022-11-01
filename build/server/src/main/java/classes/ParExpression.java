package classes;

public class ParExpression extends Expression {
  public Expression exp;

  public ParExpression(Expression exp) {
    this.exp = exp;
  }
}
