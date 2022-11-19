package classes;

import d3e.core.SetExt;
import java.util.Set;

public class InlineMethodStatement extends Statement {
  public MethodDecl method;

  public InlineMethodStatement(MethodDecl method) {
    this.method = method;
  }

  public void resolve(ResolveContext context) {
    this.method.resolve(context);
  }

  public void collectUsedTypes(Set<String> types) {
    this.method.collectUsedTypes();
    SetExt.addAll(types, this.method.usedTypes);
  }
}
