package classes;

import d3e.core.MapExt;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BinaryExpression extends Statement {
  public String op;
  public Expression left;
  public Expression right;
  public MethodDecl method;

  public BinaryExpression(Expression left, String op, Expression right) {
    this.left = left;
    this.op = op;
    this.right = right;
  }

  public void resolve(ResolveContext context) {
    Map<String, String> typeChecks = MapExt.Map();
    this.left.getTypeChecks(typeChecks);
    this.left.resolve(context);
    boolean newScope = false;
    if (MapExt.isNotEmpty(typeChecks) && Objects.equals(this.op, "&&")) {
      /*
       Create new scope if needed
      */
      context.scope = new Scope(context.scope, null);
      typeChecks.forEach(
          (k, v) -> {
            context.scope.add(k, new ValueType(v, false));
          });
      newScope = true;
    }
    this.right.resolve(context);
    if (newScope) {
      context.scope = context.scope.parent;
    }
    this.resolvedType =
        this.op == "??"
            ? context.commonType(this.left.resolvedType, this.right.resolvedType)
            : this.left.resolvedType;
  }

  public void collectUsedTypes(List<DataType> types) {
    this.left.collectUsedTypes(types);
    this.right.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    if (Objects.equals(this.op, "??=")) {
      s.add(new Assignment(this.left, this.op, this.right));
      s.markDelete();
    } else {
      this.left = s.makeSimple(this.left);
      this.right = s.makeSimple(this.right);
    }
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.right);
    visitor.visit(this.left);
  }

  public void getTypeChecks(Map<String, String> checks) {
    if (Objects.equals(this.op, "&&")) {
      this.left.getTypeChecks(checks);
      this.right.getTypeChecks(checks);
    }
  }
}
