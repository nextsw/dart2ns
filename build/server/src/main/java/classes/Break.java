package classes;

import java.util.List;

public class Break extends Statement {
  public String label;

  public Break(String label) {
    this.label = label;
  }

  public void resolve(ResolveContext context) {}

  public void collectUsedTypes(List<DataType> types) {}

  public void simplify(Simplifier s) {}

  public void visit(ExpressionVisitor visitor) {}
}
