package classes;

import java.util.Set;

public class DynamicTypeExpression extends Expression {
  public String name;

  public DynamicTypeExpression() {}

  public void resolve(ResolveContext context) {}

  public void collectUsedTypes(Set<String> types) {}
}
