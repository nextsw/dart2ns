package classes;

import d3e.core.ListExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Objects;

public class MethodDecl extends ClassMember {
  public String name;
  public List<Annotation> annotations = ListExt.asList();
  public MethodParams params;
  public DataType returnType;
  public boolean finalValue = false;
  public boolean constValue = false;
  public boolean external = false;
  public boolean setter = false;
  public boolean getter = false;
  public boolean factory = false;
  public Expression init;
  public String factoryName;
  public TypeParams generics;
  public Block body;
  public ASyncType asyncType;
  public Expression exp;
  public String nativeString;
  public String content;

  public MethodDecl(
      List<Annotation> annotations,
      ASyncType asyncType,
      Block body,
      boolean constValue,
      Expression exp,
      boolean external,
      boolean factory,
      String factoryName,
      boolean finalValue,
      TypeParams generics,
      boolean getter,
      Expression init,
      String name,
      MethodParams params,
      DataType returnType,
      boolean setter,
      boolean staticValue) {
    super(name, TopDeclType.Method, "");
    this.annotations = annotations;
    this.asyncType = asyncType;
    this.body = body;
    this.constValue = constValue;
    this.exp = exp;
    this.external = external;
    this.factory = factory;
    this.factoryName = factoryName;
    this.finalValue = finalValue;
    this.generics = generics;
    this.getter = getter;
    this.init = init;
    this.name = name;
    this.params = params;
    this.returnType = returnType;
    this.setter = setter;
    this.staticValue = staticValue;
  }

  public void resolve(ResolveContext context) {
    MethodDecl prev = context.method;
    context.method = this;
    context.scope = new Scope(context.scope, null);
    if (this.params != null) {
      for (MethodParam p : context.sortMethodParams(this.params)) {
        if (p.dataType != null) {
          if (p.dataType.name != null && StringExt.length(p.dataType.name) == 1l) {
            /*
             Need to resolve the type...
            */
            DataType paramType = resolveGegeric(context, p.dataType.name);
            context.scope.add(p.name, paramType);
          } else {
            context.scope.add(p.name, p.dataType);
          }
        }
      }
    }
    if (this.init != null) {
      this.init.resolve(context);
    }
    if (this.body != null) {
      this.body.resolve(context);
    } else if (this.exp != null) {
      this.exp.resolve(context);
    }
    context.scope = context.scope.parent;
    context.method = prev;
  }

  public void collectUsedTypes() {
    if (this.init != null) {
      this.init.collectUsedTypes(this.usedTypes);
    }
    if (this.body != null) {
      this.body.collectUsedTypes(this.usedTypes);
    } else if (this.exp != null) {
      this.exp.collectUsedTypes(this.usedTypes);
    }
  }

  public String toString() {
    return this.name;
  }

  public DataType resolveGegeric(ResolveContext context, String name) {
    if (this.generics != null) {
      TypeParam p =
          ListExt.firstWhere(
              this.generics.params,
              (x) -> {
                return Objects.equals(x.name, name);
              },
              null);
      if (p != null && p.extendType != null) {
        return p.extendType;
      }
    }
    if (context.instanceClass != null && context.instanceClass.generics != null) {
      TypeParam p =
          ListExt.firstWhere(
              context.instanceClass.generics.params,
              (x) -> {
                return Objects.equals(x.name, name);
              },
              null);
      if (p != null && p.extendType != null) {
        return p.extendType;
      }
    }
    return context.ofUnknownType();
  }
}
