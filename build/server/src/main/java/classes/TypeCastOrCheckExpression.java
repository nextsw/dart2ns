package classes;

import java.util.List;

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
}
