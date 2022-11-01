package classes;

public class PostfixExpression extends Statement {
  public String postfix;
  public Expression on;

  public PostfixExpression(Expression on, String postfix) {
    this.on = on;
    this.postfix = postfix;
  }
}
