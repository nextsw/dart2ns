package classes;

import java.util.List;

public class ConstExpression extends Statement {
  public Expression exp;

  public ConstExpression(Expression exp) {
    this.exp = exp;
  }

  public void resolve(ResolveContext context) {
    if (this.exp instanceof Assignment) {
      Assignment ass = ((Assignment) this.exp);
      FieldOrEnumExpression fe = ((FieldOrEnumExpression) ass.left);
      ass.right.resolve(context);
      this.resolvedType = ass.right.resolvedType;
      context.scope.add(fe.name, this.resolvedType);
    } else {
      this.exp.resolve(context);
      this.resolvedType = this.exp.resolvedType;
    }
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
