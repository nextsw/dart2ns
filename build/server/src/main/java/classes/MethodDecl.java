package classes;

import d3e.core.D3ELogger;
import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MethodDecl extends ClassMember {
  public String name;
  public List<Annotation> annotations = ListExt.asList();
  public MethodParams params;
  public List<MethodParam> allParams = ListExt.asList();
  public DataType returnType;
  public boolean finalValue = false;
  public boolean constValue = false;
  public boolean external = false;
  public boolean setter = false;
  public boolean getter = false;
  public boolean factory = false;
  public boolean operator = false;
  public Expression init;
  public String factoryName;
  public TypeParams generics;
  public Block body;
  public ASyncType asyncType;
  public Expression exp;
  public String nativeString;
  public LambdaType lambdaType;
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
      boolean operator,
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
    this.operator = operator;
    this.returnType = returnType;
    this.setter = setter;
    this.staticValue = staticValue;
    MethodParams value$ = params;
    if (value$ == null) {
      value$ = new MethodParams();
    }
    this.params = value$;
    this.allParams = this.params.toFixedParams();
  }

  public void resolve(ResolveContext context) {
    /*
     D3ELogger.info('Resolving Method: ' + name);
    */
    MethodDecl prev = context.method;
    context.method = this;
    context.scope = new Scope(context.scope, null);
    for (MethodParam p : this.allParams) {
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
    for (MethodParam p : this.allParams) {
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

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.exp);
    visitor.visit(this.body);
  }

  public boolean getHaveTypeParams() {
    return this.generics != null;
  }

  public boolean isSignatureMatched(
      ValidationContext ctx,
      PropType _this,
      String methodName,
      boolean isStatic,
      boolean isGetter,
      boolean isSetter,
      List<PropType> positionParamTypes,
      Map<String, PropType> namedParamTypes,
      List<PropType> typeArguments) {
    if (this.staticValue != isStatic) {
      return false;
    }
    if (this.setter != isSetter) {
      return false;
    }
    if (this.getter != isGetter) {
      return false;
    }
    if (!(Objects.equals(methodName, this.name))) {
      return false;
    }
    if (positionParamTypes == null) {
      return true;
    }
    if (ListExt.length(this.params.positionalParams) > ListExt.length(positionParamTypes)) {
      return false;
    }
    if (ListExt.length(this.params.namedParams) < MapExt.length(namedParamTypes)) {
      return false;
    }
    if (ListExt.length(this.params.optionalParams)
        < (ListExt.length(positionParamTypes) - ListExt.length(this.params.positionalParams))) {
      return false;
    }
    Map<String, PropType> typeVars = createTypeArguments(ctx, typeArguments);
    long i = 0l;
    for (long j = 0l; i < ListExt.length(this.params.positionalParams); i++) {
      MethodParam parameter = ListExt.get(this.params.positionalParams, i);
      PropType propType = ListExt.get(positionParamTypes, i);
      if (!_this
          .resolveType(null, parameter.dataType.resolvedType, typeVars)
          .isAssignableFrom(propType)) {
        return false;
      }
      i++;
    }
    for (MethodParam p : this.params.namedParams) {
      PropType propType = namedParamTypes.get(p.name);
      if (propType == null) {
        if (p.required) {
          return false;
        }
        continue;
      }
      if (!_this.resolveType(null, p.dataType.resolvedType, typeVars).isAssignableFrom(propType)) {
        return false;
      }
    }
    for (long j = 0l; i < ListExt.length(positionParamTypes); j++, i++) {
      MethodParam parameter = ListExt.get(this.params.optionalParams, j);
      PropType propType = ListExt.get(positionParamTypes, i);
      if (!_this
          .resolveType(null, parameter.dataType.resolvedType, typeVars)
          .isAssignableFrom(propType)) {
        return false;
      }
    }
    return true;
  }

  public Map<String, PropType> createTypeArguments(ValidationContext ctx, List<PropType> typeArgs) {
    if (ctx == null || this.generics == null || this.generics.params.isEmpty()) {
      return MapExt.Map();
    }
    Map<String, PropType> result = MapExt.Map();
    for (TypeParam t : this.generics.params) {
      List<TypeResolutionPosition> positions = t.typeVar.positions;
      List<PropType> all = ListExt.List(0l);
      for (TypeResolutionPosition p : positions) {
        PropType res = getTypeAtPosition(ctx, p);
        if (res != null) {
          all.add(res);
        }
      }
      MapExt.set(result, t.name, ctx.findSuperType(all));
    }
    /*
    for (Integer x = 0; x < typeArgs.length && x < typeVars.length; x++) {
    	result.set(typeVars.get(x).name, typeArgs.get(x));
    }
    */
    return result;
  }

  public PropType getTypeAtPosition(ValidationContext ctx, TypeResolutionPosition pos) {
    if (pos.type == TypeResolutionPositionType.RETURN) {
      return getTypeAtPostion(ctx, ctx.getExpectedType(), pos.gens);
    }
    TypeResolutionPositionType type = pos.type;
    switch (type) {
      case NAMED:
        {
          for (MethodParam p : this.params.namedParams) {
            if (Objects.equals(p.name, pos.name)) {
              return getTypeAtPostion(ctx, p.dataType.resolvedType, pos.gens);
            }
          }
          break;
        }
      case OPTIONAL:
        {
          if (pos.index < ListExt.length(this.params.optionalParams)) {
            return getTypeAtPostion(
                ctx,
                ListExt.get(this.params.optionalParams, pos.index).dataType.resolvedType,
                pos.gens);
          }
          break;
        }
      case POSITIONAL:
        {
          if (pos.index < ListExt.length(this.params.positionalParams)) {
            return getTypeAtPostion(
                ctx,
                ListExt.get(this.params.positionalParams, pos.index).dataType.resolvedType,
                pos.gens);
          }
          break;
        }
      case RETURN:
        {
          return null;
        }
      default:
        {
        }
    }
    return null;
  }

  public PropType getTypeAtPostion(
      ValidationContext ctx, PropType type, List<TypeResolutionPosition> gens) {
    if (type == null || gens == null || gens.isEmpty()) {
      return type;
    }
    List<PropType> all = ListExt.List(0l);
    for (TypeResolutionPosition p : gens) {
      if (p.genVar == null) {
        /*
         MethodDecl method = type.findLambdaFunction().method;
        */
        D3ELogger.error("Not sure what this getTypeAtPostion is");
        /*
         PropType pt;
         if (p.type == TypeResolutionPositionType.RETURN) {
             pt = method.getTypeAtPostion(ctx, method.returnType.resolvedType, p.gens);
         } else {
             pt = method.getTypeAtPosition(ctx, p);
         }
         addType(all, pt);
        */
      } else {
        PropType ele = type.elemenetType(p.genVar);
        if (ele != null) {
          MethodDecl.addType(all, getTypeAtPostion(ctx, ele, p.gens));
        }
      }
    }
    if (all.isEmpty()) {
      return null;
    }
    return ctx.findSuperType(all);
  }

  public static void addType(List<PropType> all, PropType type) {
    if (type == null || (type instanceof TypeVariable) || all.contains(type)) {
      return;
    }
    all.add(type);
  }

  public List<PropType> createTypesArray(ValidationContext ctx, PropType type) {
    if (this.generics == null) {
      return ListExt.asList();
    }
    return IterableExt.toList(
        ListExt.map(
            this.generics.params,
            (a) -> {
              return a.typeVar;
            }),
        false);
  }

  public LambdaType asLambdaType() {
    if (this.lambdaType == null) {
      this.lambdaType = LambdaType.withMethod(this);
    }
    return this.lambdaType;
  }

  public void validate(ValidationContext ctx, long phase) {
    ValidationContext sub = ctx.sub();
    var value$1 = this.cls == null ? null : this.cls.name;
    String value$ = value$1;
    if (value$ == null) {
      value$ = "";
    }
    D3ELogger.info("Validating " + (value$) + "." + this.name);
    if (phase == 0l) {
      validateTypes(sub);
    } else {
      sub.validateExpression(this.body);
    }
  }

  public void register(ValidationContext ctx) {}

  public void validateTypes(ValidationContext ctx) {
    ctx.validateType(this.returnType);
    var value$ = this.cls == null ? null : this.cls.type;
    ExpressionValidationUtil.validateMethodParams(this.params, ctx, value$);
    for (MethodParam p : this.allParams) {
      if (p.dataType != null) {
        ctx.validateType(p.dataType);
      }
    }
  }
}
