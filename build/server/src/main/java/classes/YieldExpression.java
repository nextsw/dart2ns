package classes;

import java.util.List;

public class YieldExpression extends Statement {
  public Expression exp;
  public boolean pointer = false;

  public YieldExpression(Expression exp, boolean pointer) {
    this.exp = exp;
    this.pointer = pointer;
  }

  public void resolve(ResolveContext context) {
    if (this.exp != null) {
      this.exp.resolve(context);
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    if (this.exp != null) {
      this.exp.collectUsedTypes(types);
    }
  }

  public void simplify(Simplifier s) {
    this.exp.simplify(s);
  }
}
