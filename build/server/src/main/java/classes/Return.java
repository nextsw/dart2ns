package classes;

import java.util.List;

public class Return extends Statement {
  public Expression expression;

  public Return() {}

  public void resolve(ResolveContext context) {
    if (this.expression != null) {
      this.expression.resolve(context);
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    if (this.expression != null) {
      this.expression.collectUsedTypes(types);
    }
  }

  public void simplify(Simplifier s) {
    this.expression = s.makeSimple(this.expression);
  }
}
