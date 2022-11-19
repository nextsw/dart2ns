package classes;

import java.util.Set;

public class DoWhileLoop extends Statement {
  public Block body;
  public Expression test;

  public DoWhileLoop(Block body, Expression test) {
    this.body = body;
    this.test = test;
  }

  public void resolve(ResolveContext context) {
    this.test.resolve(context);
    this.body.resolve(context);
  }

  public void collectUsedTypes(Set<String> types) {
    this.test.collectUsedTypes(types);
    this.body.collectUsedTypes(types);
  }
}
