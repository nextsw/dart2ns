package classes;

import d3e.core.D3ELogger;
import d3e.core.ListExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Objects;

public class MethodDecl extends ClassMember {
  public String name;
  public List<Annotation> annotations = ListExt.asList();
  public List<MethodParam> params = ListExt.asList();
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
    this.returnType = returnType;
    this.setter = setter;
    this.staticValue = staticValue;
    var value$1 = params == null ? null : params.toFixedParams();
    List<MethodParam> value$ = value$1;
    if (value$ == null) {
      value$ = ListExt.asList();
    }
    this.params = value$;
  }

  public void resolve(ResolveContext context) {
    D3ELogger.info("Resolving Method: " + this.name);
    MethodDecl prev = context.method;
    context.method = this;
    context.scope = new Scope(context.scope, null);
    for (MethodParam p : this.params) {
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
    this.body.resolve(context);
    if (this.exp != null) {
      this.exp.resolve(context);
      if (this.returnType == null) {
        this.returnType = this.exp.resolvedType;
      }
    }
    context.scope = context.scope.parent;
    context.method = prev;
  }

  public void simplify(Simplifier s) {
    s.reset();
    if (this.body == null) {
      this.body = new Block();
    }
    if (this.init != null) {
      ListExt.insert(this.body.statements, 0l, ((Statement) this.init));
      this.init = null;
    }
    if (this.exp != null) {
      Return r = new Return();
      r.expression = this.exp;
      this.body.statements.add(r);
    }
    this.body.simplify(s);
  }

  public void collectUsedTypes() {
    if (this.returnType != null) {
      this.usedTypes.add(this.returnType);
    }
    for (MethodParam p : this.params) {
      if (p.dataType != null) {
        this.usedTypes.add(p.dataType);
      }
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
      if (p != null) {
        if (p.extendType != null) {
          return p.extendType;
        } else {
          return context.objectType;
        }
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
      if (p != null) {
        if (p.extendType != null) {
          return p.extendType;
        } else {
          return context.objectType;
        }
      }
    }
    return context.ofUnknownType();
  }
}
