package classes;

public class WhileLoop extends Statement {
  public Statement body;
  public Expression test;

  public WhileLoop(Statement body, Expression test) {
    this.body = body;
    this.test = test;
  }
}
