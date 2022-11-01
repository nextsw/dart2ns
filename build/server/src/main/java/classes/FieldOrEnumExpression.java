package classes;

public class FieldOrEnumExpression extends Expression {
  public String name;
  public Expression on;
  public boolean checkNull = false;
  public boolean notNull = false;

  public FieldOrEnumExpression(boolean checkNull, String name, boolean notNull, Expression on) {
    this.checkNull = checkNull;
    this.name = name;
    this.notNull = notNull;
    this.on = on;
  }
}
