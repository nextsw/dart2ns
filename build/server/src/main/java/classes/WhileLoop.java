package classes;

import java.util.Set;

public class WhileLoop extends Statement {
  public Statement body;
  public Expression test;

  public WhileLoop(Statement body, Expression test) {
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
