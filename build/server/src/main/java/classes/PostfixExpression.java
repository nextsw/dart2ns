package classes;

import java.util.List;

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

  public void collectUsedTypes(List<DataType> types) {
    this.on.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.on.simplify(s);
  }

  public void visit(ExpressionVisitor visitor) {}
}
