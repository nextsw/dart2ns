package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import java.util.List;

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
    List<MethodParam> mp =
        IterableExt.toList(
            ListExt.map(
                this.params,
                (p) -> {
                  return new MethodParam(ListExt.List(), p.type, null, false, p.name, false, null);
                }),
            false);
    this.resolvedType =
        new FunctionType(
            false,
            mp,
            this.body != null ? new ValueType("void", false) : this.expression.resolvedType,
            ListExt.List());
    context.scope = context.scope.parent;
  }

  public void collectUsedTypes(List<DataType> types) {
    for (Param p : this.params) {
      if (p.type != null) {
        types.add(p.type);
      }
    }
    if (this.expression != null) {
      this.expression.collectUsedTypes(types);
    }
    if (this.body != null) {
      this.body.collectUsedTypes(types);
    }
  }

  public void simplify(Simplifier s) {
    if (this.expression != null) {
      this.expression.simplify(s);
    }
    if (this.body != null) {
      this.body.simplify(s);
    }
  }
}
