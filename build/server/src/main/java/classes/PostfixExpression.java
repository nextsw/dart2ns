package classes;

import java.util.Set;

public class PostfixExpression extends Statement {
  public String postfix;
  public Expression on;

  public PostfixExpression(Expression on, String postfix) {
    this.on = on;
    this.postfix = postfix;
  }

  public void resolve(ResolveContext context) {
    this.on.resolve(context);
    this.resolvedType = this.on.resolvedType;
  }

  public void collectUsedTypes(Set<String> types) {
    this.on.collectUsedTypes(types);
  }
}
