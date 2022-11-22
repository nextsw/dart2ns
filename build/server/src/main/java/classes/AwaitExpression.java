package classes;

import java.util.List;

public class AwaitExpression extends Statement {
  public Expression exp;

  public AwaitExpression(Expression exp) {
    this.exp = exp;
  }

  public void resolve(ResolveContext context) {
    this.exp.resolve(context);
    this.resolvedType = this.exp.resolvedType;
    this.exp.resolvedType = context.subType(this.exp.resolvedType, 0l);
  }

  public void collectUsedTypes(List<DataType> types) {
    this.exp.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.exp = s.makeSimple(this.exp);
  }
}
