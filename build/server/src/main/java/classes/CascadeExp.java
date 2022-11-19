package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

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

  public void collectUsedTypes(Set<String> types) {
    this.on.collectUsedTypes(types);
  }
}
