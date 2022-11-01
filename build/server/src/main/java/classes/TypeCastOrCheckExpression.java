package classes;

public class TypeCastOrCheckExpression extends Expression {
  public boolean check = false;
  public DataType dataType;
  public Expression exp;

  public TypeCastOrCheckExpression(boolean check, DataType dataType, Expression exp) {
    this.check = check;
    this.dataType = dataType;
    this.exp = exp;
  }
}
