package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Objects;

public class MethodCall extends Statement {
  public String name;
  public List<DataType> typeArgs = ListExt.asList();
  public List<Argument> positionArgs = ListExt.asList();
  public List<NamedArgument> namedArgs = ListExt.asList();
  public boolean checkNull = false;
  public boolean notNull = false;
  public Expression on;
  public String onTypeName;
  public MethodDecl resolvedMethod;
  public FunctionType resolvedFunctionType;
  public MethodCallType callType;
  public PropType staticClass;

  public MethodCall(
      String name,
      List<NamedArgument> namedArgs,
      String onTypeName,
      List<Argument> positionArgs,
      List<DataType> typeArgs) {
    this.name = name;
    this.namedArgs = namedArgs;
    this.onTypeName = onTypeName;
    this.positionArgs = positionArgs;
    this.typeArgs = typeArgs;
  }

  public void resolve(ResolveContext context) {
    this.callType = MethodCallType.ERROR;
    if (Objects.equals(this.name, "assert")) {
      this.resolvedType = context.statementType;
      this.callType = MethodCallType.Assert;
      if (ListExt.isNotEmpty(this.positionArgs)) {
        Expression exp = ListExt.first(this.positionArgs).arg;
        if (exp instanceof LambdaExpression) {
          context.expectedType =
              new FunctionType(false, ListExt.List(), new ValueType("bool", false), ListExt.List());
        }
        exp.resolve(context);
      }
      return;
    }
    if (Objects.equals(this.name, "this") && context.instanceClass.isExtension) {
      DataType on = ListExt.first(context.instanceClass.ons);
      Typedef def = ((Typedef) context.currentLib.get(on.name));
      this.resolvedType = def.fnType.returnType;
      this.callType = MethodCallType.FunctionMethod;
      return;
    }
    ClassDecl onType = null;
    boolean onLibrary = false;
    if (this.on != null) {
      this.on.resolve(context);
      if (Objects.equals(this.on.resolvedType, context.libraryType)) {
        onLibrary = true;
      } else if (Objects.equals(this.on.resolvedType, context.typeType)) {
        if (!(this.on instanceof FieldOrEnumExpression)) {
          context.error("Mist be FE Exp");
          this.resolvedType = context.ofUnknownType();
          return;
        }
        String typeName = (((FieldOrEnumExpression) this.on)).name;
        TopDecl top = context.currentLib.get(typeName);
        if (top instanceof ClassDecl) {
          onType = ((ClassDecl) top);
        } else if (top instanceof Enum) {
          onType = (((Enum) top)).toClassDecl();
        }
      } else if (this.on.resolvedType instanceof ValueType) {
        TopDecl top = context.currentLib.get(this.on.resolvedType.name);
        if (top instanceof ClassDecl) {
          onType = ((ClassDecl) top);
        }
      }
    } else {
      if (context.instanceClass != null
          && context.getMember(context.instanceClass, this.name, null, false, null) != null) {
        onType = context.instanceClass;
      }
      if (this.onTypeName != null) {
        TopDecl top = context.currentLib.get(this.onTypeName);
        if (top instanceof ClassDecl) {
          onType = ((ClassDecl) top);
        }
      }
    }
    var value$ = context.scope == null ? null : context.scope.get(this.name);
    DataType scopeType = value$;
    if (onType != null) {
      MethodDecl md =
          ((MethodDecl) context.getMember(onType, this.name, MemberFilter.AllMethods, false, null));
      if (md == null) {
        md = onType.method(onType.name + "." + this.name);
      }
      if (md != null) {
        if (md.getter) {
          /*
           we are calling a getters return type
          */
          if (md.returnType instanceof FunctionType) {
            this.resolvedFunctionType = ((FunctionType) md.returnType);
            this.callType = MethodCallType.FunctionMethod;
          } else {
            TopDecl top = context.currentLib.get(md.returnType.name);
            if (top instanceof Typedef) {
              Typedef def = ((Typedef) top);
              this.resolvedFunctionType = def.fnType;
              this.callType = MethodCallType.FunctionMethod;
            } else {
              context.error("We can not call non function type getters");
              this.resolvedType = context.ofUnknownType();
            }
          }
        } else if (md.returnType == null) {
          /*
          Assuming it is constuctor
          */
          this.resolvedType = new ValueType(onType.name, false);
          if (Objects.equals(md.factoryName, this.name)) {
            this.callType = MethodCallType.FactoryConstructor;
          } else {
            this.callType = MethodCallType.Constructor;
          }
        } else {
          this.resolvedType = context.resolveType(onType, md.cls, md.returnType);
          if (md.staticValue) {
            this.callType = MethodCallType.StaticMethod;
          } else {
            this.callType = MethodCallType.InstanceMethod;
          }
        }
        this.resolvedMethod = md;
      } else {
        /*
         Error
        */
        this.resolvedType = context.ofUnknownType();
      }
    } else if (onLibrary) {
      if (!(this.on instanceof FieldOrEnumExpression)) {
        context.error("Mist be FE Exp");
        this.resolvedType = context.ofUnknownType();
      } else {
        String importName = (((FieldOrEnumExpression) this.on)).name;
        Library libToCheck = context.currentLib;
        TopDecl topCm = libToCheck.get(this.name);
        if (topCm instanceof MethodDecl) {
          MethodDecl md = ((MethodDecl) topCm);
          this.resolvedType = md.returnType;
          this.resolvedMethod = md;
          this.callType = MethodCallType.LibraryMethod;
        } else if (topCm instanceof ClassDecl) {
          ClassDecl cls = ((ClassDecl) topCm);
          ClassMember cm = context.getMember(cls, this.name, null, false, null);
          if (cm instanceof MethodDecl) {
            this.resolvedMethod = ((MethodDecl) cm);
          }
          this.resolvedType = new ValueType(cls.name, false);
          this.callType = MethodCallType.InstanceMethod;
        } else {
          this.resolvedType = context.ofUnknownType();
        }
      }
    } else if (scopeType != null) {
      if (scopeType instanceof FunctionType) {
        this.resolvedType = (((FunctionType) scopeType)).returnType;
        this.callType = MethodCallType.FunctionMethod;
      } else {
        var value$1 = context.instanceClass == null ? null : context.instanceClass.name;
        String cls = value$1;
        var value$2 = context.method == null ? null : context.method.name;
        String method = value$2;
        var value$3 = onType == null ? null : onType.name;
        String inType = value$3;
        context.error(
            "Referend method is not Function type: "
                + this.name
                + " in "
                + inType
                + " Cls: "
                + cls
                + " Method: "
                + method);
        this.resolvedType = context.ofUnknownType();
      }
    } else {
      TopDecl td = context.currentLib.get(this.name);
      if (td instanceof MethodDecl) {
        MethodDecl md = ((MethodDecl) td);
        this.resolvedType = md.returnType;
        this.resolvedMethod = md;
        this.callType = MethodCallType.LibraryMethod;
        /*
        TODO need to resolve on method type generics
        */
      } else if (td instanceof ClassDecl) {
        this.resolvedType = new ValueType(this.name, false);
        this.resolvedMethod = (((ClassDecl) td)).method(this.name);
        this.callType = MethodCallType.Constructor;
      } else {
        var value$1 = context.instanceClass == null ? null : context.instanceClass.name;
        String cls = value$1;
        var value$2 = context.method == null ? null : context.method.name;
        String method = value$2;
        var value$3 = onType == null ? null : onType.name;
        String inType = value$3;
        context.error(
            "No method found: "
                + this.name
                + " in "
                + inType
                + " Cls: "
                + cls
                + " Method: "
                + method);
        this.resolvedType = context.ofUnknownType();
      }
    }
    long x = 0l;
    for (Argument arg : this.positionArgs) {
      if (this.resolvedFunctionType != null) {
        MethodParam p = ListExt.get(this.resolvedFunctionType.params, x);
        if (p != null) {
          arg.resolvedType = p.dataType;
          context.expectedType = arg.resolvedType;
        } else {
          context.expectedType = context.ofUnknownType();
        }
      } else if (this.resolvedMethod != null) {
        MethodParam p = this.resolvedMethod.getParamAt(x);
        if (p != null) {
          arg.resolvedType = p.dataType;
          context.expectedType = arg.resolvedType;
        } else {
          context.expectedType = context.ofUnknownType();
        }
      } else {
        context.expectedType = context.ofUnknownType();
      }
      arg.arg.resolve(context);
    }
    for (NamedArgument arg : this.namedArgs) {
      if (this.resolvedMethod != null) {
        MethodParam p = this.resolvedMethod.getParam(arg.name);
        if (p != null) {
          arg.resolvedType = this.resolvedMethod.getParam(arg.name).dataType;
          context.expectedType = arg.resolvedType;
        } else {
          context.expectedType = context.ofUnknownType();
        }
      } else {
        context.expectedType = context.ofUnknownType();
      }
      arg.value.resolve(context);
    }
    if (this.resolvedMethod != null && ListExt.isNotEmpty(this.namedArgs)) {
      for (long y = ListExt.length(this.positionArgs);
          y < ListExt.length(this.resolvedMethod.allParams);
          y++) {
        MethodParam param = ListExt.get(this.resolvedMethod.allParams, y);
        NamedArgument arg =
            ListExt.firstWhere(
                this.namedArgs,
                (n) -> {
                  return Objects.equals(n.name, param.name);
                },
                null);
        if (arg == null) {
          Expression value$1 = param.defaultValue;
          if (value$1 == null) {
            value$1 = makeDefaultValue(param.dataType);
          }
          this.namedArgs.add(new NamedArgument(ListExt.List(), param.name, value$1));
        } else {
          this.namedArgs.add(arg);
        }
      }
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    if (this.on != null) {
      this.on.collectUsedTypes(types);
    }
    for (Argument arg : this.positionArgs) {
      arg.arg.collectUsedTypes(types);
    }
    for (NamedArgument arg : this.namedArgs) {
      arg.value.collectUsedTypes(types);
    }
  }

  public void simplify(Simplifier s) {
    if (this.checkNull) {
      TerinaryExpression ter =
          new TerinaryExpression(
              new BinaryExpression(this.on, "==", new NullExpression()),
              this,
              new NullExpression());
      this.checkNull = false;
      s.add(ter);
      s.markDelete();
    } else {
      this.on = s.makeSimple(this.on);
      for (Argument arg : this.positionArgs) {
        arg.arg = s.makeSimple(arg.arg);
      }
      for (NamedArgument arg : this.namedArgs) {
        arg.value = s.makeSimple(arg.value);
      }
    }
  }

  public Expression makeDefaultValue(DataType type) {
    if (type == null) {
      return new NullExpression();
    } else if (Objects.equals(type.name, "int")) {
      return new LiteralExpression(false, LiteralType.TypeInteger, "0");
    } else if (Objects.equals(type.name, "double")) {
      return new LiteralExpression(false, LiteralType.TypeDouble, "0.0");
    } else if (Objects.equals(type.name, "num")) {
      return new LiteralExpression(false, LiteralType.TypeInteger, "0");
    } else if (Objects.equals(type.name, "bool")) {
      return new LiteralExpression(false, LiteralType.TypeBoolean, "false");
    } else {
      return new NullExpression();
    }
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.on);
    for (Argument arg : this.positionArgs) {
      visitor.visit(arg.arg);
    }
    for (NamedArgument arg : this.namedArgs) {
      visitor.visit(arg.value);
    }
  }
}
