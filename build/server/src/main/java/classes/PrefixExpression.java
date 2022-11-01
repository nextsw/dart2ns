package classes;

public class PrefixExpression extends Statement {
  public String prefix;
  public Expression on;

  public PrefixExpression(Expression on, String prefix) {
    this.on = on;
    this.prefix = prefix;
  }
}
