package classes;

import java.util.Set;

public class ParExpression extends Expression {
  public Expression exp;

  public ParExpression(Expression exp) {
    this.exp = exp;
  }

  public void resolve(ResolveContext context) {
    this.exp.resolve(context);
    this.resolvedType = this.exp.resolvedType;
  }

  public void collectUsedTypes(Set<String> types) {
    this.exp.collectUsedTypes(types);
  }
}
