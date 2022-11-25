package classes;

import d3e.core.MapExt;
import java.util.List;
import java.util.Map;

public class TypeCastOrCheckExpression extends Statement {
  public boolean check = false;
  public DataType dataType;
  public Expression exp;
  public boolean isNot = false;

  public TypeCastOrCheckExpression(
      boolean check, DataType dataType, Expression exp, boolean isNot) {
    this.check = check;
    this.dataType = dataType;
    this.exp = exp;
    this.isNot = isNot;
  }

  public void resolve(ResolveContext context) {
    this.exp.resolve(context);
    if (this.check) {
      this.resolvedType = context.booleanType;
    } else {
      this.resolvedType = this.dataType;
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    this.exp.collectUsedTypes(types);
    types.add(this.dataType);
  }

  public void simplify(Simplifier s) {
    this.exp = s.makeSimple(this.exp);
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.exp);
  }

  public void getTypeChecks(Map<String, String> checks) {
    if (this.check) {
      if (this.exp instanceof FieldOrEnumExpression) {
        FieldOrEnumExpression fe = ((FieldOrEnumExpression) this.exp);
        if (fe.on == null) {
          MapExt.set(checks, fe.name, this.dataType.name);
        }
      }
    }
  }
}
