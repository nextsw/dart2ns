package classes;

import d3e.core.D3ELogger;
import java.util.Set;

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
      D3ELogger.error("We should not be calling non function types");
      this.resolvedType = context.ofUnknownType();
    }
  }

  public void collectUsedTypes(Set<String> types) {
    this.on.collectUsedTypes(types);
    this.call.collectUsedTypes(types);
  }
}
