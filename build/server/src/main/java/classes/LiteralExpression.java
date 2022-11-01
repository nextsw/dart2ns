package classes;

public class LiteralExpression extends Expression {
  public String value;
  public LiteralType type;
  public boolean isRawString = false;

  public LiteralExpression(boolean isRawString, LiteralType type, String value) {
    this.isRawString = isRawString;
    this.type = type;
    this.value = value;
  }
}
