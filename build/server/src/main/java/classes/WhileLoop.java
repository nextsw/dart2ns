package classes;

public class WhileLoop extends Statement {
  public Block body;
  public Expression test;

  public WhileLoop(Block body, Expression test) {
    this.body = body;
    this.test = test;
  }
}
