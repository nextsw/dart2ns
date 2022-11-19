package classes;

import java.util.Set;

public class ExpressionArrayItem extends ArrayItem {
  public Expression exp;

  public ExpressionArrayItem(Expression exp) {
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
