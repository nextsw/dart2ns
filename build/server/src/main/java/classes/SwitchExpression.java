package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

public class SwitchExpression extends Expression {
  public Expression on;
  public List<CaseExpression> cases = ListExt.asList();
  public Expression onElse;

  public SwitchExpression(List<CaseExpression> cases, Expression on) {
    this.cases = cases;
    this.on = on;
  }

  public void resolve(ResolveContext context) {
    this.on.resolve(context);
    this.resolvedType = null;
    this.cases.forEach(
        (c) -> {
          c.resolve(context);
          if (this.resolvedType == null) {
            this.resolvedType = c.result.resolvedType;
          } else {
            this.resolvedType = context.commonType(this.resolvedType, c.result.resolvedType);
          }
        });
    if (this.onElse != null) {
      this.onElse.resolve(context);
      this.resolvedType = context.commonType(this.resolvedType, this.onElse.resolvedType);
    }
  }

  public void collectUsedTypes(Set<String> types) {
    this.on.collectUsedTypes(types);
    this.cases.forEach(
        (c) -> {
          c.collectUsedTypes(types);
        });
    if (this.onElse != null) {
      this.onElse.collectUsedTypes(types);
    }
  }
}
