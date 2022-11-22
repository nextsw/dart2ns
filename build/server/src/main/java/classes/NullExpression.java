package classes;

import java.util.List;

public class NullExpression extends Expression {
  public NullExpression() {}

  public void resolve(ResolveContext context) {
    this.resolvedType = context.nullType;
  }

  public void collectUsedTypes(List<DataType> types) {}

  public void simplify(Simplifier s) {}
}
