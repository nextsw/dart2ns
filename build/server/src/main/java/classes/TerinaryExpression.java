package classes;

public class TerinaryExpression extends Expression {
  public Expression condition;
  public Expression ifTrue;
  public Expression ifFalse;

  public TerinaryExpression(Expression condition, Expression ifFalse, Expression ifTrue) {
    this.condition = condition;
    this.ifFalse = ifFalse;
    this.ifTrue = ifTrue;
  }
}
