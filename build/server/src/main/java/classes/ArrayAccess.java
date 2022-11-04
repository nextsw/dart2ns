package classes;

public class ArrayAccess extends Expression {
  public Expression on;
  public Expression index;
public boolean checkNull;
public boolean notNull;

  public ArrayAccess(Expression index, Expression on, boolean checkNull, boolean notNull) {
    this.index = index;
    this.on = on;
	this.checkNull = checkNull;
	this.notNull = notNull;
  }
}
