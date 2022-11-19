package classes;

import java.util.Set;

public class PrefixExpression extends Statement {
  public String prefix;
  public Expression on;

  public PrefixExpression(Expression on, String prefix) {
    this.on = on;
    this.prefix = prefix;
  }

  public void resolve(ResolveContext context) {
    this.on.resolve(context);
    this.resolvedType = this.on.resolvedType;
  }

  public void collectUsedTypes(Set<String> types) {
    this.on.collectUsedTypes(types);
  }
}
