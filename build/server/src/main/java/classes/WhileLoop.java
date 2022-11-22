package classes;

import java.util.List;

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

  public void collectUsedTypes(List<DataType> types) {
    this.test.collectUsedTypes(types);
    this.body.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.test = s.makeSimple(this.test);
    if (!(this.body instanceof Block)) {
      Block b = new Block();
      b.statements.add(this.body);
      this.body = b;
    }
    this.body.simplify(s);
  }
}
