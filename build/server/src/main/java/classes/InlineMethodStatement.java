package classes;

import d3e.core.ListExt;
import java.util.List;

public class InlineMethodStatement extends Statement {
  public MethodDecl method;

  public InlineMethodStatement(MethodDecl method) {
    this.method = method;
  }

  public void resolve(ResolveContext context) {
    this.method.resolve(context);
    FunctionType fnType =
        new FunctionType(
            false,
            ListExt.from(this.method.allParams, false),
            this.method.returnType,
            ListExt.List());
    context.scope.add(this.method.name, fnType);
  }

  public void collectUsedTypes(List<DataType> types) {
    this.method.collectUsedTypes();
    ListExt.addAll(types, this.method.usedTypes);
  }

  public void simplify(Simplifier s) {
    this.method.simplify(s);
  }

  public void visit(ExpressionVisitor visitor) {
    this.method.visit(visitor);
  }
}
