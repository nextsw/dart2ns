package classes;

public class YieldExpression extends Statement {
  public Expression exp;
  public boolean pointer = false;

  public YieldExpression(Expression exp, boolean pointer) {
    this.exp = exp;
    this.pointer = pointer;
  }
}
