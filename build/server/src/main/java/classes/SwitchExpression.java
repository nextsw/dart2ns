package classes;

import d3e.core.ListExt;
import java.util.List;

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

  public void collectUsedTypes(List<DataType> types) {
    this.on.collectUsedTypes(types);
    this.cases.forEach(
        (c) -> {
          c.collectUsedTypes(types);
        });
    if (this.onElse != null) {
      this.onElse.collectUsedTypes(types);
    }
  }

  public void simplify(Simplifier s) {
    this.on = s.makeSimple(this.on);
    this.cases.forEach(
        (c) -> {
          c.result = s.makeSimple(c.result);
        });
    if (this.onElse != null) {
      this.onElse = s.makeSimple(this.onElse);
    }
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.on);
    this.cases.forEach(
        (c) -> {
          visitor.visit(c.result);
        });
    visitor.visit(this.onElse);
  }
}
