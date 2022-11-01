package classes;

public class DoWhileLoop extends Statement {
  public Block body;
  public Expression test;

  public DoWhileLoop(Block body, Expression test) {
    this.body = body;
    this.test = test;
  }
}
