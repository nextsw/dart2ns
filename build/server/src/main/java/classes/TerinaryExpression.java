package classes;

import java.util.List;

public class TerinaryExpression extends Statement {
  public Expression condition;
  public Expression ifTrue;
  public Expression ifFalse;

  public TerinaryExpression(Expression condition, Expression ifFalse, Expression ifTrue) {
    this.condition = condition;
    this.ifFalse = ifFalse;
    this.ifTrue = ifTrue;
  }

  public void resolve(ResolveContext context) {
    this.condition.resolve(context);
    this.ifTrue.resolve(context);
    this.ifFalse.resolve(context);
    this.resolvedType = context.commonType(this.ifTrue.resolvedType, this.ifFalse.resolvedType);
  }

  public void collectUsedTypes(List<DataType> types) {
    this.condition.collectUsedTypes(types);
    this.ifTrue.collectUsedTypes(types);
    this.ifFalse.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.condition = s.makeSimple(this.condition);
    this.ifTrue = s.makeSimple(this.ifTrue);
    this.ifFalse = s.makeSimple(this.ifFalse);
  }
}
