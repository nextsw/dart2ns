package classes;

import d3e.core.ListExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Objects;

public class ArrayAccess extends Expression {
  public Expression on;
  public Expression index;
  public boolean checkNull = false;
  public boolean notNull = false;
  public MethodDecl method;

  public ArrayAccess(boolean checkNull, Expression index, boolean notNull, Expression on) {
    this.checkNull = checkNull;
    this.index = index;
    this.notNull = notNull;
    this.on = on;
  }

  public void resolve(ResolveContext context) {
    this.on.resolve(context);
    this.index.resolve(context);
    if (Objects.equals(this.on.resolvedType.name, "List")) {
      DataType value$ = ListExt.first((((ValueType) this.on.resolvedType)).args);
      if (value$ == null) {
        value$ = context.objectType;
      }
      this.resolvedType = value$;
    } else {
      /*
       We need to find the index method.
      */
      TopDecl top = context.get(this.on.resolvedType.name);
      if (top instanceof ClassDecl) {
        ClassDecl cls = ((ClassDecl) top);
        MethodDecl indexMethod = ((MethodDecl) context.getMember(cls, "[]", null, false, null));
        if (indexMethod == null) {
          this.resolvedType = context.ofUnknownType();
          context.error("Can not find [] operator in : " + cls.name);
        } else if (StringExt.length(indexMethod.returnType.name) == 1l) {
          DataType value$ = context.getListValueType(cls);
          if (value$ == null) {
            value$ = context.ofUnknownType();
          }
          this.resolvedType = value$;
        } else {
          this.resolvedType = indexMethod.returnType;
        }
      } else {
        this.resolvedType = context.ofUnknownType();
        context.error("It must be Class");
      }
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    this.on.collectUsedTypes(types);
    this.index.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.on = s.makeSimple(this.on);
    this.index = s.makeSimple(this.index);
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.on);
    visitor.visit(this.index);
  }
}
