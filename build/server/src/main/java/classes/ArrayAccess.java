package classes;

public class ArrayAccess extends Expression {
  public Expression on;
  public Expression index;
  public boolean checkNull = false;
  public boolean notNull = false;

  public ArrayAccess(boolean checkNull, Expression index, boolean notNull, Expression on) {
    this.checkNull = checkNull;
    this.index = index;
    this.notNull = notNull;
    this.on = on;
  }
}
