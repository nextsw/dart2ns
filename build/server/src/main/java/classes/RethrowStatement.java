package classes;

import java.util.List;

public class RethrowStatement extends Statement {
  public RethrowStatement() {}

  public void resolve(ResolveContext context) {}

  public void collectUsedTypes(List<DataType> types) {}

  public void simplify(Simplifier s) {}

  public void visit(ExpressionVisitor visitor) {}
}
