package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Objects;

public class FieldOrEnumExpression extends Statement {
  public String name;
  public Expression on;
  public boolean checkNull = false;
  public boolean notNull = false;
  public boolean isGetter = false;
  public boolean setter = false;
  public EvalType evalType = EvalType.ERROR;
  public ClassMember resolvedMember;
  public ClassDecl fieldClass;
  public List<String> primitives = ListExt.asList("int", "bool", "double", "num");

  public FieldOrEnumExpression(boolean checkNull, String name, boolean notNull, Expression on) {
    this.checkNull = checkNull;
    this.name = name;
    this.notNull = notNull;
    this.on = on;
  }

  public void resolve(ResolveContext context) {
    ClassDecl onType = null;
    boolean onDynamic = false;
    if (this.on != null) {
      this.on.resolve(context);
      TopDecl decl = context.currentLib.get(this.on.resolvedType.name);
      if (decl instanceof ClassDecl) {
        onType = ((ClassDecl) decl);
      } else if (decl instanceof Enum) {
        Enum em = ((Enum) decl);
        if (Objects.equals(this.name, "index")) {
          this.resolvedType = context.integerType;
          return;
        } else if (Objects.equals(this.name, "name")) {
          this.resolvedType = context.stringType;
          return;
        }
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
        /*
         D3ELogger.error('Resolved Type is not Class in FEExp');
        */
      } else if (Objects.equals(this.on.resolvedType, context.libraryType)) {
        if (!(this.on instanceof FieldOrEnumExpression)) {
          context.error("Mist be FE Exp");
          this.resolvedType = context.ofUnknownType();
          return;
        }
        String importName = (((FieldOrEnumExpression) this.on)).name;
        Library libToCheck = context.currentLib;
        TopDecl topCm = libToCheck.get(this.name);
        if (topCm instanceof ClassMember) {
          resolveUsingClassMember(context, ((ClassMember) topCm), onType);
          return;
        } else {
          this.resolvedType = context.typeType;
          return;
        }
      } else if (Objects.equals(this.on.resolvedType.name, "dynamic")) {
        onDynamic = true;
      }
    }
    if (Objects.equals(this.name, "this")) {
      this.resolvedType = new ValueType(context.instanceClass.name, false);
      return;
    }
    DataType fieldType = context.fieldTypeFromScope(this.name);
    if (onDynamic) {
      this.resolvedType = context.objectType;
      return;
    }
    if (fieldType == null && onType != null) {
      ClassMember cm = context.getMember(onType, this.name, null, false);
      resolveUsingClassMember(context, cm, onType);
    } else if (fieldType != null) {
      /*
       this must be global field
      */
      this.resolvedType = fieldType;
    } else {
      if (this.on == null && ParserUtil.isTypeName(this.name)
          || this.primitives.contains(this.name)) {
        this.resolvedType = context.typeType;
        return;
      } else if (context.instanceClass != null) {
        ClassMember mem = context.getMember(context.instanceClass, this.name, null, false);
        if (mem != null) {
          resolveUsingClassMember(context, mem, context.instanceClass);
          fieldType = this.resolvedType;
        }
      }
      if (fieldType == null && context.currentLib != null) {
        /*
         Lets check if is a library import
        */
        Library libToCheck = context.currentLib;
        Import importValue =
            ListExt.firstWhere(
                libToCheck.imports,
                (i) -> {
                  return Objects.equals(i.name, this.name);
                },
                null);
        if (importValue != null) {
          this.resolvedType = context.libraryType;
          fieldType = this.resolvedType;
          return;
        }
        TopDecl top = libToCheck.get(this.name);
        if ((top instanceof FieldDecl) || (top instanceof MethodDecl)) {
          resolveUsingClassMember(context, ((ClassMember) top), null);
          fieldType = this.resolvedType;
        }
      }
      if (fieldType == null) {
        var value$ = context.instanceClass == null ? null : context.instanceClass.name;
        String cls = value$;
        var value$1 = context.method == null ? null : context.method.name;
        String method = value$1;
        context.error("No field found: " + this.name + " in Cls: " + cls + " Method: " + method);
        this.resolvedType = context.ofUnknownType();
      }
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    if (this.on != null) {
      this.on.collectUsedTypes(types);
    }
    types.add(this.resolvedType);
  }

  public void resolveUsingClassMember(ResolveContext context, ClassMember cm, ClassDecl onType) {
    if (cm instanceof MethodDecl) {
      MethodDecl md = ((MethodDecl) cm);
      if (md.getter) {
        this.isGetter = true;
        if (onType != null) {
          this.resolvedType = context.resolveType(onType, md.cls, md.returnType);
        } else {
          this.resolvedType = md.returnType;
        }
        this.resolvedMember = cm;
      } else {
        this.resolvedType = new FunctionType(false, md.allParams, md.returnType, ListExt.List());
        this.resolvedMember = cm;
      }
    } else {
      FieldDecl field = ((FieldDecl) cm);
      if (field != null) {
        if (field.type == null) {
          field.resolve(context);
        }
        if (onType != null) {
          this.resolvedType = context.resolveType(onType, field.cls, field.type);
        } else {
          this.resolvedType = field.type;
        }
        this.resolvedMember = cm;
      } else {
        var value$ = context.instanceClass == null ? null : context.instanceClass.name;
        String cls = value$;
        var value$1 = context.method == null ? null : context.method.name;
        String method = value$1;
        var value$2 = onType == null ? null : onType.name;
        String inType = value$2;
        context.error(
            "No field found: "
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
  }

  public String toString() {
    var value$1 = this.on == null ? null : this.on.toString();
    String value$ = value$1;
    if (value$ == null) {
      value$ = "";
    }
    return (value$) + this.name;
  }

  public void simplify(Simplifier s) {
    if (this.checkNull) {
      TerinaryExpression ter =
          new TerinaryExpression(
              new BinaryExpression(this.on, "==", new NullExpression()),
              new FieldOrEnumExpression(false, this.name, false, this.on),
              new NullExpression());
      s.add(ter);
      s.markDelete();
    } else {
      this.on = s.makeSimple(this.on);
    }
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.on);
  }
}
