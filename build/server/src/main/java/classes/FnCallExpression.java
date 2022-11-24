package classes;

import java.util.List;

public class FnCallExpression extends Statement {
  public Expression on;
  public MethodCall call;

  public FnCallExpression(MethodCall call, Expression on) {
    this.call = call;
    this.on = on;
  }

  public void resolve(ResolveContext context) {
    this.on.resolve(context);
    this.call.resolve(context);
    DataType onType = this.on.resolvedType;
    if (onType instanceof FunctionType) {
      FunctionType ft = ((FunctionType) onType);
      this.resolvedType = ft.returnType;
    } else {
      context.error("We should not be calling non function types");
      this.resolvedType = context.ofUnknownType();
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    this.on.collectUsedTypes(types);
    this.call.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.on = s.makeSimple(this.on);
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.on);
    visitor.visit(this.call);
  }
}
