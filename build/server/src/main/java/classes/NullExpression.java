package classes;

import java.util.Set;

public class NullExpression extends Expression {
  public NullExpression() {}

  public void resolve(ResolveContext context) {
    this.resolvedType = context.nullType;
  }

  public void collectUsedTypes(Set<String> types) {}
}
