package classes;

import d3e.core.D3ELogger;
import d3e.core.ListExt;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
      } else if (this.on.resolvedType instanceof ValueType) {
        TopDecl top = context.get(this.on.resolvedType.name);
        if (top instanceof ClassDecl) {
          onType = ((ClassDecl) top);
        }
      }
    } else {
      if (context.instanceClass != null
          && context.getMember(context.instanceClass, this.name, null) != null) {
        onType = context.instanceClass;
      }
      if (this.onTypeName != null) {
        TopDecl top = context.get(this.onTypeName);
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
          this.resolvedType = context.resolveType(onType, md.returnType);
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
      if (topCm instanceof MethodDecl) {
        MethodDecl md = ((MethodDecl) topCm);
        this.resolvedType = md.returnType;
      } else {
        /*
         Error
        */
        this.resolvedType = context.ofUnknownType();
      }
    } else {
      TopDecl td = context.get(this.name);
      if (td instanceof MethodDecl) {
        MethodDecl md = ((MethodDecl) td);
        this.resolvedType = md.returnType;
        this.resolvedMethod = md;
        /*
        TODO need to resolve on method type generics
        */
      } else if (td instanceof ClassDecl) {
        this.resolvedType = new ValueType(this.name, false);
      } else {
        this.resolvedType = context.ofUnknownType();
      }
    }
  }

  public void collectUsedTypes(Set<String> types) {
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
}
