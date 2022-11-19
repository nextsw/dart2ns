package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

public class LambdaExpression extends Expression {
  public List<Param> params = ListExt.asList();
  public Expression expression;
  public Block body;
  public ASyncType asyncType = ASyncType.NONE;

  public LambdaExpression(List<Param> params) {
    this.params = params;
  }

  public void resolve(ResolveContext context) {
    context.scope = new Scope(context.scope, null);
    for (Param p : this.params) {
      if (p.type != null) {
        context.scope.add(p.name, p.type);
      }
    }
    if (this.expression != null) {
      this.expression.resolve(context);
    }
    if (this.body != null) {
      this.body.resolve(context);
    }
    /*
     TODO
    */
    this.resolvedType = context.ofUnknownType();
    context.scope = context.scope.parent;
  }

  public void collectUsedTypes(Set<String> types) {
    for (Param p : this.params) {
      if (p.type != null) {
        p.type.collectUsedTypes(types);
      }
    }
    if (this.expression != null) {
      this.expression.collectUsedTypes(types);
    }
    if (this.body != null) {
      this.body.collectUsedTypes(types);
    }
  }
}
