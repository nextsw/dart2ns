package classes;

import java.util.List;

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

  public void collectUsedTypes(List<DataType> types) {
    this.test.collectUsedTypes(types);
    this.body.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.test = s.makeSimple(this.test);
    this.body.simplify(s);
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.test);
    visitor.visit(this.body);
  }
}
