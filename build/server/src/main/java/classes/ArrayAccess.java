package classes;

public class ArrayAccess extends Expression {
  public Expression on;
  public Expression index;

  public ArrayAccess(Expression index, Expression on) {
    this.index = index;
    this.on = on;
  }
}
