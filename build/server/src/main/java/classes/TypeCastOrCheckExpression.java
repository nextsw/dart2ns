package classes;

public class TypeCastOrCheckExpression extends Statement {
  public boolean check = false;
  public DataType dataType;
  public Expression exp;
  public boolean isNot;

  public TypeCastOrCheckExpression(boolean check, DataType dataType, Expression exp, boolean isNot) {
    this.check = check;
    this.dataType = dataType;
    this.exp = exp;
    this.isNot = isNot;
  }
}
