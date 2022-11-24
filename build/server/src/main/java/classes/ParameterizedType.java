package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ParameterizedType extends PropType {
  public PropType baseType;
  public List<TypeArgument> arguments = ListExt.asList();

  public ParameterizedType() {
    super(null);
  }

  public static ParameterizedType from(PropType baseType) {
    ParameterizedType type = new ParameterizedType();
    type.baseType = baseType;
    type.name = baseType.name;
    type.typeVars = baseType.typeVars;
    type.extendsValue = baseType.extendsValue;
    type.impls = baseType.impls;
    type.types = baseType.types;
    type.abstractValue = baseType.abstractValue;
    return type;
  }

  public void addArgument(PropType type) {
    if (ListExt.length(this.baseType.typeVars) <= ListExt.length(this.arguments)) {
      return;
    }
    TypeVariable typeVar = ListExt.get(this.baseType.typeVars, ListExt.length(this.arguments));
    addArgumentWithName(typeVar.name, type);
  }

  public void addArgumentWithName(String name, PropType type) {
    TypeArgument arg =
        ListExt.firstWhere(
            this.arguments,
            (i) -> {
              return Objects.equals(i.name, name);
            },
            null);
    if (arg == null) {
      TypeArgument typeArgument = new TypeArgument(name, type);
      this.arguments.add(typeArgument);
    } else {
      arg.type = type;
    }
  }

  public PropType getElemenetType() {
    if (this.baseType.typeVars.isEmpty()) {
      return null;
    }
    TypeArgument arg =
        ListExt.firstWhere(
            this.arguments,
            (i) -> {
              return Objects.equals(i.name, ListExt.first(this.baseType.typeVars).name);
            },
            null);
    if (arg != null) {
      return arg.type;
    }
    return this.baseType.getElemenetType();
  }

  public boolean isAssignableFrom(PropType type) {
    if (isAssignableFromInternal(type)) {
      return true;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType pt = (((ParameterizedType) type));
      if (this.baseType.isAssignableFrom(pt.baseType)) {
        if (this.arguments.isEmpty() || pt.arguments.isEmpty()) {
          return true;
        }
        if (ListExt.length(this.arguments) == ListExt.length(pt.arguments)) {
          long index = 0l;
          for (TypeArgument e : this.arguments) {
            PropType leftType = e.type;
            PropType rightType = ListExt.get(pt.arguments, index).type;
            index++;
            if ((leftType == null || rightType == null) || !leftType.isAssignableFrom(rightType)) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  public PropType elemenetType(String type) {
    TypeArgument arg =
        ListExt.firstWhere(
            this.arguments,
            (i) -> {
              return Objects.equals(i.name, type);
            },
            null);
    if (arg != null) {
      return arg.type;
    }
    return this.baseType.elemenetType(type);
  }

  public PropType resolveTypeWithTypeVar(
      ValidationContext ctx, PropType type, Map<String, PropType> typeArgument, TypeVariable t) {
    PropType propType = typeArgument.get(t.name);
    if (propType == null) {
      TypeArgument arg =
          ListExt.firstWhere(
              this.arguments,
              (i) -> {
                return Objects.equals(i.name, t.name);
              },
              null);
      if (arg == null) {
        return t;
      } else {
        return arg.type;
      }
    } else if (propType instanceof TypeVariable) {
      TypeArgument arg =
          ListExt.firstWhere(
              this.arguments,
              (i) -> {
                return Objects.equals(i.name, propType.name);
              },
              null);
      if (arg != null) {
        return arg.type;
      }
    }
    return propType;
  }

  public List<PropType> getTypeArgumentsList() {
    return IterableExt.toList(
        ListExt.map(
            this.typeVars,
            (t) -> {
              return elemenetType(t.name);
            }),
        false);
  }

  public DataType toDataType() {
    DataType base = this.baseType.toDataType();
    ValueType t = ((ValueType) base);
    t.args =
        IterableExt.toList(
            ListExt.map(
                this.arguments,
                (x) -> {
                  return x.type.toDataType();
                }),
            false);
    return t;
  }
}
