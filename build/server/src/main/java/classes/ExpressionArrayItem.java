package classes;

import java.util.List;

public class ExpressionArrayItem extends ArrayItem {
  public Expression exp;

  public ExpressionArrayItem(Expression exp) {
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
    this.exp = s.makeSimple(this.exp);
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.exp);
  }
}
