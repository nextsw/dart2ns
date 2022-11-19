package classes;

import java.util.Set;

public class LiteralExpression extends Expression {
  public String value;
  public LiteralType type;
  public boolean isRawString = false;

  public LiteralExpression(boolean isRawString, LiteralType type, String value) {
    this.isRawString = isRawString;
    this.type = type;
    this.value = value;
  }

  public void resolve(ResolveContext context) {
    switch (this.type) {
      case TypeBoolean:
        {
          this.resolvedType = context.booleanType;
          break;
        }
      case TypeString:
        {
          this.resolvedType = context.stringType;
          break;
        }
      case TypeDouble:
        {
          this.resolvedType = context.doubleType;
          break;
        }
      case TypeInteger:
        {
          this.resolvedType = context.integerType;
          break;
        }
      default:
        {
        }
    }
  }

  public void collectUsedTypes(Set<String> types) {}
}
