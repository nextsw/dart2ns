package classes;

import java.util.Set;

public class ThrowStatement extends Statement {
  public Expression exp;

  public ThrowStatement() {}

  public void resolve(ResolveContext context) {
    this.exp.resolve(context);
    this.resolvedType = this.exp.resolvedType;
  }

  public void collectUsedTypes(Set<String> types) {
    this.exp.collectUsedTypes(types);
  }
}
