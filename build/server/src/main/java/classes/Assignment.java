package classes;

import java.util.List;
import java.util.Objects;

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

  public void collectUsedTypes(List<DataType> types) {
    this.left.collectUsedTypes(types);
    this.right.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    if (Objects.equals(this.op, "??=")) {
      this.left.simplify(s);
      IfStatement ifs =
          new IfStatement(
              null,
              new BinaryExpression(this.left, "==", new NullExpression()),
              new Assignment(this.left, "=", this.right));
      s.add(ifs);
      s.markDelete();
    } else {
      this.left.simplify(s);
      this.right.simplify(s);
    }
  }
}
