package classes;

import java.util.List;

public class Symbol extends Expression {
  public String name;

  public Symbol(String name) {
    this.name = name;
  }

  public void resolve(ResolveContext context) {
    this.resolvedType = context.objectType;
  }

  public void collectUsedTypes(List<DataType> types) {}

  public void simplify(Simplifier s) {}

  public void visit(ExpressionVisitor visitor) {}
}
