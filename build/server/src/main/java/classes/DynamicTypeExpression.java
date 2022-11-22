package classes;

import java.util.List;

public class DynamicTypeExpression extends Expression {
  public String name;

  public DynamicTypeExpression() {}

  public void resolve(ResolveContext context) {}

  public void collectUsedTypes(List<DataType> types) {}

  public void simplify(Simplifier s) {}
}
