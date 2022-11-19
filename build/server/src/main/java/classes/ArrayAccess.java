package classes;

import d3e.core.D3ELogger;
import d3e.core.ListExt;
import d3e.core.StringExt;
import java.util.Objects;
import java.util.Set;

public class ArrayAccess extends Expression {
  public Expression on;
  public Expression index;
  public boolean checkNull = false;
  public boolean notNull = false;

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
        MethodDecl indexMethod = ((MethodDecl) context.getMember(cls, "[]", null));
        if (indexMethod == null) {
          this.resolvedType = context.ofUnknownType();
          D3ELogger.error("Can not find [] operator in : " + cls.name);
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
        D3ELogger.error("It must be Class");
      }
    }
  }

  public void collectUsedTypes(Set<String> types) {
    this.on.collectUsedTypes(types);
    this.index.collectUsedTypes(types);
  }
}
