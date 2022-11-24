package classes;

import java.util.List;

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

  public void collectUsedTypes(List<DataType> types) {
    this.on.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.on.simplify(s);
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.on);
  }
}
