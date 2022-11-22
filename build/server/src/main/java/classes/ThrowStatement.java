package classes;

import java.util.List;

public class ThrowStatement extends Statement {
  public Expression exp;

  public ThrowStatement() {}

  public void resolve(ResolveContext context) {
    this.exp.resolve(context);
    this.resolvedType = this.exp.resolvedType;
  }

  public void collectUsedTypes(List<DataType> types) {
    this.exp.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.exp = s.makeSimple(this.exp);
  }
}
