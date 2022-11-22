package classes;

import java.util.List;
import java.util.Objects;

public class BinaryExpression extends Statement {
  public String op;
  public Expression left;
  public Expression right;

  public BinaryExpression(Expression left, String op, Expression right) {
    this.left = left;
    this.op = op;
    this.right = right;
  }

  public void resolve(ResolveContext context) {
    this.left.resolve(context);
    /*
     Create new scope if needed
    */
    boolean newScope = false;
    if (Objects.equals(this.op, "&&")) {
      Expression check = this.left;
      if (check instanceof ParExpression) {
        check = (((ParExpression) check)).exp;
      }
      if (this.left instanceof TypeCastOrCheckExpression) {
        TypeCastOrCheckExpression typeCheck = ((TypeCastOrCheckExpression) check);
        if (typeCheck.check && typeCheck.exp instanceof FieldOrEnumExpression) {
          FieldOrEnumExpression fe = ((FieldOrEnumExpression) typeCheck.exp);
          if (fe.on == null) {
            context.scope = new Scope(context.scope, null);
            context.scope.add(fe.name, typeCheck.dataType);
            newScope = true;
          }
        }
      }
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
}
