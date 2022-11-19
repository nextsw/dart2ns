package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

public class Block extends Statement {
  public List<Comment> afterComments = ListExt.asList();
  public List<Statement> statements = ListExt.asList();

  public Block() {}

  public void resolve(ResolveContext context) {
    context.scope = new Scope(context.scope, null);
    for (Statement stmt : this.statements) {
      stmt.resolve(context);
    }
    context.scope = context.scope.parent;
  }

  public void collectUsedTypes(Set<String> types) {
    for (Statement stmt : this.statements) {
      stmt.collectUsedTypes(types);
    }
  }
}
