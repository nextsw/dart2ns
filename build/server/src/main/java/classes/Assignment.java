package classes;

import java.util.Set;

public class Assignment extends Statement {
  public String op;
  public Expression left;
  public Expression right;

  public Assignment(Expression left, String op, Expression right) {
    this.left = left;
    this.op = op;
    this.right = right;
  }

  public void resolve(ResolveContext context) {
    this.left.resolve(context);
    this.right.resolve(context);
    this.resolvedType = this.left.resolvedType;
  }

  public void collectUsedTypes(Set<String> types) {
    this.left.collectUsedTypes(types);
    this.right.collectUsedTypes(types);
  }
}
