package classes;

public class BinaryExpression extends Expression {
  public String op;
  public Expression left;
  public Expression right;

  public BinaryExpression(Expression left, String op, Expression right) {
    this.left = left;
    this.op = op;
    this.right = right;
  }
}
