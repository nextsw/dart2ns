package classes;

import java.util.List;
import java.util.Map;

public class ParExpression extends Expression {
  public Expression exp;

  public ParExpression(Expression exp) {
    this.exp = exp;
  }

  public void resolve(ResolveContext context) {
    this.exp.resolve(context);
    this.resolvedType = this.exp.resolvedType;
  }

  public void collectUsedTypes(List<DataType> types) {
    this.exp.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.exp.simplify(s);
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.exp);
  }

  public void getTypeChecks(Map<String, String> checks) {
    this.exp.getTypeChecks(checks);
  }
}
