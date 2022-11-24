package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.MethodType;
import d3e.core.SetExt;
import d3e.core.Type;
import d3e.core.WrappedType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PropType {
  public String name;
  public PropType extendsValue;
  public List<TypeVariable> typeVars = ListExt.asList();
  public boolean abstractValue = false;
  public boolean types = false;
  public List<PropType> impls = ListExt.asList();
  public ClassDecl cls;
  public Enum enm;
  public Typedef typedef;
  public List<PropType> subs = ListExt.asList();
  public List<PropType> usedTypes = ListExt.asList();
  public boolean typeDef = false;
  public static final String ANY_TYPE = "Object";
  public static final String TEXT_TYPE = "String";
  public static final String INTEGER_TYPE = "Integer";
  public static final String DOUBLE_TYPE = "Double";
  public static final String BOOLEAN_TYPE = "Boolean";
  public static final String TYPE_TYPE = "Type";
  public static final String PROPERTY_PATH_TYPE = "PropertyPath";
  public static final String VOID_TYPE = "void";
  public static final String FUNCTION_TYPE = "Function";
  public static final String LIST_TYPE = "List";
  public static final String DURATION_TYPE = "Duration";
  public static final String DATE_TYPE = "Date";
  public static final String DATETIME_TYPE = "DateTime";
  public static final String TIME_TYPE = "Time";
  public static final String SET_TYPE = "Set";
  public static final String ITERABLE_TYPE = "Iterable";
  public static final PropType VOID = PropType.createVoid();
  public static final Set<String> PRIMITVES = SetExt.from(ListExt.asList());

  public PropType(String name) {
    this.name = name;
  }

  public String toString() {
    return FormateUtil.toStringPropType(this);
  }

  public DataType toDataType() {
    ValueType t = new ValueType(this.name, false);
    t.resolvedType = this;
    return t;
  }

  public static PropType createVoid() {
    PropType pt = new PropType(PropType.VOID_TYPE);
    return pt;
  }

  public void setExtends(PropType extnds) {
    this.extendsValue = extnds;
  }

  public PropType getGen(String name) {
    for (TypeVariable varType : this.typeVars) {
      if (Objects.equals(varType.name, name)) {
        return varType.extendsValue;
      }
    }
    return null;
  }

  public boolean canTypeSubstitute(PropType type) {
    return isAssignableFrom(type);
  }

  public boolean isAssignableFrom(PropType type) {
    return isAssignableFromInternal(type);
  }

  public boolean isAssignableFromInternal(PropType type) {
    if (Objects.equals(type, PropType.VOID)) {
      return Objects.equals(this, PropType.VOID);
    }
    if (Objects.equals(this, type) || Objects.equals(this.name, "Object")) {
      return true;
    }
    if (Objects.equals(type.name, "null")) {
      return !(Objects.equals(this, PropType.VOID));
    }
    if (type == null) {
      return false;
    }
    if (type instanceof ParameterizedType) {
      return isAssignableFrom(((((ParameterizedType) type))).baseType);
    }
    if (type instanceof LambdaType) {
      return (((LambdaType) type)).canAssignTo(this);
    }
    if (type.extendsValue != null) {
      if (isAssignableFrom(type.extendsValue)) {
        return true;
      }
    }
    return ListExt.any(
        type.impls,
        (i) -> {
          return isAssignableFrom(i);
        });
  }

  public boolean canCompare(PropType type) {
    if (Objects.equals(type.name, "null")
        && (Objects.equals(this.name, "Integer")
            || Objects.equals(this.name, "Boolean")
            || Objects.equals(this.name, "Double"))) {
      return false;
    }
    return isAssignableFrom(type) || type.isAssignableFrom(this);
  }

  public PropType fieldType(ValidationContext ctx, String fieldName, boolean isStatic) {
    FieldDecl field = findField(fieldName, isStatic);
    if (field == null) {
      return ctx.object();
    }
    return resolveType(
        ctx,
        field.type.resolvedType,
        MapExt.fromIterable(
            field.type.resolvedType.typeVars,
            (i) -> {
              return i.name;
            },
            (i) -> {
              return i;
            }));
  }

  public boolean hasField(ValidationContext ctx, String fieldName, boolean isStatic) {
    return findField(fieldName, isStatic) != null;
  }

  public FieldDecl findField(String fieldName, boolean isStatic) {
    for (FieldDecl f : getAllFields()) {
      if (Objects.equals(f.name, fieldName)) {
        return f.staticValue == isStatic ? f : null;
      }
    }
    return null;
  }

  public MethodDecl findMethod(ValidationContext ctx, String methodName) {
    return ListExt.firstWhere(
        getAllMethods(ctx),
        (m) -> {
          return Objects.equals(m.name, methodName);
        },
        null);
  }

  public MethodDecl findMethodByName(
      ValidationContext ctx, String methodName, boolean isSetter, boolean isGetter) {
    return ListExt.firstWhere(
        ListExt.where(
            ListExt.where(
                getAllMethods(ctx),
                (m) -> {
                  return m.setter == isSetter;
                }),
            (m) -> {
              return m.getter == isGetter;
            }),
        (m) -> {
          return m.name.equals(methodName);
        },
        () -> {
          return null;
        });
  }

  public PropType resolveType(
      ValidationContext ctx, PropType type, Map<String, PropType> typeArgument) {
    if (typeArgument == null) {
      typeArgument = MapExt.Map();
    }
    if (type instanceof TypeVariable) {
      PropType et = resolveTypeWithTypeVar(ctx, this, typeArgument, (((TypeVariable) type)));
      return et == null ? type : et;
    }
    if (type.typeVars.isEmpty()) {
      return type;
    }
    boolean isSame = true;
    ParameterizedType pt;
    if (type instanceof ParameterizedType) {
      pt = ParameterizedType.from((((ParameterizedType) type)).baseType);
    } else {
      isSame = false;
      pt = ParameterizedType.from(type);
    }
    for (TypeVariable t : type.typeVars) {
      PropType varType = type.elemenetType(t.name);
      if (!(varType instanceof TypeVariable)) {
        PropType rt = resolveType(ctx, varType, typeArgument);
        if (!(Objects.equals(rt, varType))) {
          isSame = false;
        }
        pt.addArgumentWithName(t.name, rt);
        continue;
      }
      PropType propType =
          resolveTypeWithTypeVar(ctx, type, typeArgument, (((TypeVariable) varType)));
      if (propType != null) {
        isSame = false;
        pt.addArgumentWithName(t.name, propType);
      }
    }
    return isSame ? type : pt;
  }

  public PropType resolveTypeWithTypeVar(
      ValidationContext ctx, PropType type, Map<String, PropType> typeArgument, TypeVariable t) {
    PropType propType = typeArgument.get(t.name);
    if (propType == null) {
      List<PropType> all = ListExt.asList();
      if (this.extendsValue != null) {
        all.add(this.extendsValue);
      }
      ListExt.addAll(all, this.impls);
      for (PropType e : all) {
        PropType r = e.resolveType(ctx, t, MapExt.Map());
        if (!(Objects.equals(t, r))) {
          return r;
        }
      }
      return t;
    }
    return propType;
  }

  public PropType methodType(ValidationContext ctx, String fieldName, PropType genType) {
    FieldDecl field = findField(fieldName, false);
    if (field == null) {
      return PropType.VOID;
    }
    PropType retType = field.type.resolvedType;
    if (!this.typeVars.isEmpty() && genType != null) {
      ParameterizedType parameterizedType = ParameterizedType.from(this);
      parameterizedType.addArgument(genType);
      /*
       if(retType is LambdaType) {
           return retType;
       } else {
      */
      return parameterizedType.resolveType(ctx, retType, null);
      /*
       }
      */
    }
    return retType;
  }

  public List<MethodDecl> getAllMethods(ValidationContext ctx) {
    List<MethodDecl> all = ListExt.from(this.cls.getMethods(), false);
    if (this.extendsValue != null) {
      ListExt.addAll(all, this.extendsValue.getAllMethods(ctx));
    } else {
      PropType objType = ctx.object();
      if (!(Objects.equals(this, objType))) {
        ListExt.addAll(all, objType.getAllMethods(ctx));
      }
    }
    this.impls.forEach(
        (i) -> {
          ListExt.addAll(all, i.getAllMethods(ctx));
        });
    return all;
  }

  public List<FieldDecl> getAllFields() {
    List<FieldDecl> all = ListExt.from(this.cls.getFields(), false);
    if (this.extendsValue != null) {
      ListExt.addAll(all, this.extendsValue.getAllFields());
    }
    this.impls.forEach(
        (i) -> {
          ListExt.addAll(all, i.getAllFields());
        });
    return all;
  }

  public PropType getElemenetType() {
    if (this.typeVars.isEmpty()) {
      return null;
    }
    return ListExt.first(this.typeVars);
  }

  public MethodDecl findOperatorMethod(ValidationContext ctx, String method, PropType rt) {
    return ListExt.firstWhere(
        ListExt.where(
            getAllMethods(ctx),
            (m) -> {
              return m.operator
                  && Objects.equals(m.name, method)
                  && ListExt.isNotEmpty(m.params.positionalParams);
            }),
        (m) -> {
          return resolveType(
                  ctx, ListExt.first(m.params.positionalParams).dataType.resolvedType, null)
              .isAssignableFrom(rt);
        },
        null);
  }

  public FieldDecl getField(String name) {
    return ListExt.firstWhere(
        getAllFields(),
        (f) -> {
          return Objects.equals(f.name, name);
        },
        null);
  }

  public LambdaType findLambdaFunction() {
    if (!this.typeDef) {
      return null;
    }
    if (IterableExt.length(this.cls.getMethods()) == 1l) {
      MethodDecl method = IterableExt.getFirst(this.cls.getMethods());
      return LambdaType.withMethod(method);
    }
    return null;
  }

  public PropType elemenetType(String type) {
    for (TypeVariable t : this.typeVars) {
      if (Objects.equals(t.name, type)) {
        return t;
      }
    }
    if (this.extendsValue != null) {
      return this.extendsValue.elemenetType(type);
    }
    return null;
  }

  public PropType wrap(List<PropType> args) {
    ParameterizedType pt = ParameterizedType.from(this);
    for (PropType arg : args) {
      pt.addArgument(((PropType) arg));
    }
    return pt;
  }

  public MethodDecl getOverride(ValidationContext ctx, MethodDecl method) {
    return findMethodByName(ctx, method.name, method.setter, method.getter);
  }

  public static boolean isPrimitive(PropType type) {
    return PropType.PRIMITVES.contains(type.name);
  }

  public static PropType fromType(Type k, ValidationContext ctx) {
    if (k == null) {
      return null;
    }
    if (k instanceof WrappedType) {
      WrappedType wrap = ((WrappedType) k);
      PropType outer = PropType.fromType(wrap.getOuter(), ctx);
      List<PropType> args =
          IterableExt.toList(
              ListExt.map(
                  wrap.getSubs(),
                  (t) -> {
                    return PropType.fromType(t, ctx);
                  }),
              false);
      return outer.wrap(args);
    } else if (k instanceof MethodType) {
      MethodType wrap = ((MethodType) k);
      PropType on = PropType.fromType(wrap.getOn(), ctx);
      PropType gen = PropType.fromType(wrap.getGen(), ctx);
      return on.methodType(ctx, wrap.getName(), gen);
    } else {
      if (k.getName().isEmpty()) {
        return ctx.getType("Object");
      }
      return ctx.getType(k.getName());
    }
  }

  public boolean isCollection() {
    if (this instanceof ParameterizedType) {
      return (((ParameterizedType) this)).baseType.isCollection();
    }
    if (Objects.equals(this.name, PropType.LIST_TYPE)
        || Objects.equals(this.name, PropType.SET_TYPE)
        || Objects.equals(this.name, PropType.ITERABLE_TYPE)) {
      return true;
    }
    if (this.extendsValue != null) {
      return this.extendsValue.isCollection();
    }
    return false;
  }

  public PropType elementType() {
    if (isCollection()) {
      return getElemenetType();
    }
    return this;
  }

  public PropType applyArg(PropType arg) {
    return applyArgs(ListExt.asList(arg));
  }

  public PropType applyArgs(List<PropType> args) {
    if (args.isEmpty() || this.typeVars.isEmpty()) {
      return this;
    }
    ParameterizedType pt = ParameterizedType.from(this);
    if (args != null) {
      args.forEach(
          (a) -> {
            pt.addArgument(a);
          });
    }
    return pt;
  }

  public PropType elementTypeWithGeneric(PropType type, String genericName) {
    for (TypeVariable typeVar : type.typeVars) {
      if (Objects.equals(typeVar.name, genericName)) {
        return typeVar;
      }
    }
    return type;
  }

  public PropType elementTypeWithIndex(long index) {
    if (this instanceof ParameterizedType) {
      List<PropType> arguments = (((ParameterizedType) this)).getTypeArgumentsList();
      if (ListExt.length(arguments) > index) {
        return ListExt.get(arguments, index);
      }
      return this;
    }
    if (ListExt.length(this.typeVars) > index) {
      return ListExt.get(this.typeVars, index);
    }
    return this;
  }

  public boolean isTypeType() {
    if (this.extendsValue != null && !(Objects.equals(this.extendsValue.name, "Object"))) {
      return this.extendsValue.isTypeType();
    }
    if (this instanceof ParameterizedType) {
      return (((ParameterizedType) this)).baseType.isTypeType();
    }
    return Objects.equals(this.name, PropType.TYPE_TYPE);
  }

  public boolean isIntOrLong() {
    return Objects.equals(this.name, PropType.INTEGER_TYPE);
  }

  public boolean isFloatOrDouble() {
    return Objects.equals(this.name, PropType.DOUBLE_TYPE);
  }

  public boolean isNumber() {
    return isIntOrLong() || isFloatOrDouble();
  }

  public boolean isBool() {
    return Objects.equals(this.name, PropType.BOOLEAN_TYPE);
  }
}
