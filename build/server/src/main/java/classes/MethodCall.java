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
          && context.getMember(context.instanceClass, this.name, null, false) != null) {
        onType = context.instanceClass;
      }
      if (this.onTypeName != null) {
        TopDecl top = context.currentLib.get(this.onTypeName);
        if (top instanceof ClassDecl) {
          onType = ((ClassDecl) top);
        }
      }
    }
    for (Argument arg : this.positionArgs) {
      arg.arg.resolve(context);
    }
    for (NamedArgument arg : this.namedArgs) {
      arg.value.resolve(context);
    }
    var value$ = context.scope == null ? null : context.scope.get(this.name);
    DataType scopeType = value$;
    if (onType != null) {
      ClassMember cm =
          ListExt.firstWhere(
              onType.members,
              (m) -> {
                return m instanceof MethodDecl
                    && (Objects.equals(m.name, this.name)
                        || Objects.equals((((MethodDecl) m)).factoryName, this.name));
              },
              null);
      if (cm != null && cm instanceof MethodDecl) {
        MethodDecl md = ((MethodDecl) cm);
        if (md.returnType == null) {
          /*
          Assuming it is constuctor
          */
          this.resolvedType = new ValueType(onType.name, false);
        } else {
          this.resolvedType = context.resolveType(onType, md.cls, md.returnType);
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
        return;
      }
      String importName = (((FieldOrEnumExpression) this.on)).name;
      Library libToCheck = context.currentLib;
      TopDecl topCm = libToCheck.get(this.name);
      if (topCm instanceof MethodDecl) {
        MethodDecl md = ((MethodDecl) topCm);
        this.resolvedType = md.returnType;
        this.resolvedMethod = md;
      } else if (topCm instanceof ClassDecl) {
        ClassDecl cls = ((ClassDecl) topCm);
        ClassMember cm = context.getMember(cls, this.name, null, false);
        if (cm instanceof MethodDecl) {
          this.resolvedMethod = ((MethodDecl) cm);
        }
        this.resolvedType = new ValueType(cls.name, false);
      } else {
        this.resolvedType = context.ofUnknownType();
      }
    } else if (scopeType != null) {
      if (scopeType instanceof FunctionType) {
        this.resolvedType = (((FunctionType) scopeType)).returnType;
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
        /*
        TODO need to resolve on method type generics
        */
      } else if (td instanceof ClassDecl) {
        this.resolvedType = new ValueType(this.name, false);
      } else if (Objects.equals(this.name, "assert")) {
        this.resolvedType = context.statementType;
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
    if (this.resolvedMethod != null && ListExt.isNotEmpty(this.namedArgs)) {
      for (long x = ListExt.length(this.positionArgs);
          x < ListExt.length(this.resolvedMethod.allParams);
          x++) {
        MethodParam param = ListExt.get(this.resolvedMethod.allParams, x);
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
          this.positionArgs.add(new Argument(ListExt.List(), value$1));
        } else {
          this.positionArgs.add(new Argument(ListExt.List(), arg.value));
        }
      }
      this.namedArgs.clear();
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
