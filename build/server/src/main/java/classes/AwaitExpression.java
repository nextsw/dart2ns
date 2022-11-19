package classes;

import java.util.Set;

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

  public void collectUsedTypes(Set<String> types) {
    this.exp.collectUsedTypes(types);
  }
}
