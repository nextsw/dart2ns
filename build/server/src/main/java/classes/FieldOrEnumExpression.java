package classes;

import d3e.core.D3ELogger;
import d3e.core.ListExt;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FieldOrEnumExpression extends Statement {
  public String name;
  public Expression on;
  public boolean checkNull = false;
  public boolean notNull = false;
  public boolean isGetter = false;
  public ClassMember resolvedMember;
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
      TopDecl decl = context.get(this.on.resolvedType.name);
      if (decl instanceof ClassDecl) {
        onType = ((ClassDecl) decl);
      } else if (Objects.equals(this.on.resolvedType, context.typeType)) {
        if (!(this.on instanceof FieldOrEnumExpression)) {
          D3ELogger.error("Mist be FE Exp");
          this.resolvedType = context.ofUnknownType();
          return;
        }
        String typeName = (((FieldOrEnumExpression) this.on)).name;
        TopDecl top = context.get(typeName);
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
          D3ELogger.error("Mist be FE Exp");
          this.resolvedType = context.ofUnknownType();
          return;
        }
        String importName = (((FieldOrEnumExpression) this.on)).name;
        Library libToCheck = context.instanceClass.lib;
        if (context.instanceClass.lib.partOf != null) {
          libToCheck = libToCheck.parent;
        }
        Import importValue =
            ListExt.firstWhere(
                libToCheck.imports,
                (i) -> {
                  return Objects.equals(i.name, importName);
                },
                null);
        TopDecl topCm = importValue.lib.get(this.name);
        if (topCm instanceof ClassMember) {
          resolveUsingClassMember(context, ((ClassMember) topCm), onType);
          return;
        } else {
          D3ELogger.error("It must be ClassMember");
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
      ClassMember cm = context.getMember(onType, this.name, null);
      resolveUsingClassMember(context, cm, onType);
    } else if (fieldType != null) {
      /*
       this must be global field
      */
      this.resolvedType = fieldType;
    } else {
      if (context.method != null && !context.method.staticValue) {
        ClassMember mem =
            context.getMember(context.instanceClass, this.name, MemberFilter.FieldsAndGetters);
        if (mem instanceof FieldDecl) {
          fieldType = (((FieldDecl) mem)).type;
          this.resolvedType = fieldType;
          this.resolvedMember = mem;
        } else if (mem instanceof MethodDecl) {
          fieldType = (((MethodDecl) mem)).returnType;
          this.resolvedType = fieldType;
          this.resolvedMember = mem;
        }
      }
      if (fieldType == null) {
        if (this.on == null && ParserUtil.isTypeName(this.name)
            || this.primitives.contains(this.name)) {
          this.resolvedType = context.typeType;
        } else {
          /*
           Lets check if is a library import
          */
          Library libToCheck = context.instanceClass.lib;
          if (context.instanceClass.lib.partOf != null) {
            libToCheck = libToCheck.parent;
          }
          Import importValue =
              ListExt.firstWhere(
                  libToCheck.imports,
                  (i) -> {
                    return Objects.equals(i.name, this.name);
                  },
                  null);
          if (importValue != null) {
            this.resolvedType = context.libraryType;
            return;
          }
          var value$ = context.instanceClass == null ? null : context.instanceClass.name;
          String cls = value$;
          var value$1 = context.method == null ? null : context.method.name;
          String method = value$1;
          D3ELogger.error(
              "No field found: " + this.name + " in Cls: " + cls + " Method: " + method);
          this.resolvedType = context.ofUnknownType();
        }
      }
    }
  }

  public void collectUsedTypes(Set<String> types) {
    if (this.on != null) {
      this.on.collectUsedTypes(types);
    }
  }

  public void resolveUsingClassMember(ResolveContext context, ClassMember cm, ClassDecl onType) {
    if (cm instanceof MethodDecl) {
      MethodDecl md = ((MethodDecl) cm);
      if (md.getter) {
        this.isGetter = true;
        if (onType != null) {
          this.resolvedType = context.resolveType(onType, md.returnType);
        } else {
          this.resolvedType = md.returnType;
        }
        this.resolvedMember = cm;
      } else {
        this.resolvedType = context.ofUnknownType();
      }
    } else {
      FieldDecl field = ((FieldDecl) cm);
      if (field != null) {
        if (onType != null) {
          this.resolvedType = context.resolveType(onType, field.type);
        } else {
          this.resolvedType = field.type;
        }
        this.resolvedMember = cm;
      } else {
        var value$ = context.instanceClass == null ? null : context.instanceClass.name;
        String cls = value$;
        var value$1 = context.method == null ? null : context.method.name;
        String method = value$1;
        D3ELogger.error(
            "No field found: "
                + this.name
                + " in "
                + onType.name
                + " Cls: "
                + cls
                + " Method: "
                + method);
        this.resolvedType = context.ofUnknownType();
      }
    }
  }

  public String toString() {
    return this.on == null ? this.on.toString() : "" + this.name;
  }
}
