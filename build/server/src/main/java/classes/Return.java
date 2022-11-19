package classes;

import java.util.Set;

public class Return extends Statement {
  public Expression expression;

  public Return() {}

  public void resolve(ResolveContext context) {
    if (this.expression != null) {
      this.expression.resolve(context);
    }
  }

  public void collectUsedTypes(Set<String> types) {
    if (this.expression != null) {
      this.expression.collectUsedTypes(types);
    }
  }
}
