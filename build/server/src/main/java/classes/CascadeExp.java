package classes;

import d3e.core.ListExt;
import java.util.List;

public class CascadeExp extends Statement {
  public Expression on;
  public List<Statement> calls = ListExt.asList();

  public CascadeExp(Expression on) {
    this.on = on;
  }

  public void resolve(ResolveContext context) {
    this.on.resolve(context);
    this.resolvedType = this.on.resolvedType;
  }

  public void collectUsedTypes(List<DataType> types) {
    this.on.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.on = s.makeSimple(this.on);
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.on);
  }
}
