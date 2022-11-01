package classes;

public class Assignment extends Statement {
  public String op;
  public Expression left;
  public Expression right;

  public Assignment(Expression left, String op, Expression right) {
    this.left = left;
    this.op = op;
    this.right = right;
  }
}
