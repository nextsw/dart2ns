package classes;

import d3e.core.ListExt;
import d3e.core.MapExt;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TypeParam {
  public String name;
  public DataType extendType;
  public DataType resolvedType;
  public TypeVariable typeVar;
  public PropType actualType;

  public TypeParam(DataType extendType, String name) {
    this.extendType = extendType;
    this.name = name;
  }

  public TypeVariable createTypeVariable(
      ValidationContext ctx,
      PropType retType,
      List<PropType> positionalParams,
      Map<String, PropType> namedParams,
      List<PropType> optionalParams) {
    if (this.typeVar == null) {
      this.typeVar = new TypeVariable(this.name);
    }
    if (this.extendType != null) {
      PropType parent = ctx.typeOrObjectData(this.extendType);
      this.typeVar.extendsValue = parent;
    }
    List<TypeResolutionPosition> positions =
        TypeParam.createTypePositions(
            retType, positionalParams, namedParams, optionalParams, this.name);
    if (!positions.isEmpty()) {
      this.typeVar.positions = positions;
    }
    return this.typeVar;
  }

  public static List<TypeResolutionPosition> createTypePositions(
      PropType retType,
      List<PropType> positionalParams,
      Map<String, PropType> namedParams,
      List<PropType> optionalParams,
      String value) {
    List<TypeResolutionPosition> positions = ListExt.List(0l);
    if (positionalParams != null) {
      for (long i = 0l; i < ListExt.length(positionalParams); i++) {
        PropType param = ListExt.get(positionalParams, i);
        TypeResolutionPosition position =
            TypeParam.createTypePosition(param, value, TypeResolutionPositionType.POSITIONAL);
        if (position != null) {
          position.index = i;
          positions.add(position);
        }
      }
    }
    if (optionalParams != null) {
      for (long i = 0l; i < ListExt.length(optionalParams); i++) {
        PropType param = ListExt.get(optionalParams, i);
        TypeResolutionPosition position =
            TypeParam.createTypePosition(param, value, TypeResolutionPositionType.OPTIONAL);
        if (position != null) {
          position.index = i;
          positions.add(position);
        }
      }
    }
    if (namedParams != null) {
      namedParams.forEach(
          (n, type) -> {
            TypeResolutionPosition position =
                TypeParam.createTypePosition(type, value, TypeResolutionPositionType.NAMED);
            if (position != null) {
              position.name = n;
              positions.add(position);
            }
          });
    }
    if (retType != null && positions.isEmpty()) {
      TypeResolutionPosition position =
          TypeParam.createTypePosition(retType, value, TypeResolutionPositionType.RETURN);
      if (position != null) {
        positions.add(position);
      }
    }
    return positions;
  }

  public static TypeResolutionPosition createTypePosition(
      PropType type, String gen, TypeResolutionPositionType posType) {
    if (Objects.equals(type.name, gen)) {
      return new TypeResolutionPosition(posType);
    }
    List<TypeResolutionPosition> gens = ListExt.List(0l);
    if (type instanceof ParameterizedType) {
      List<TypeArgument> args = (((ParameterizedType) type)).arguments;
      for (TypeArgument e : args) {
        TypeResolutionPosition pos =
            TypeParam.createTypePosition(e.type, gen, TypeResolutionPositionType.GEN);
        if (pos != null) {
          pos.genVar = e.name;
          gens.add(pos);
        }
      }
    }
    if (type instanceof LambdaType) {
      LambdaType lt = (((LambdaType) type));
      TypeResolutionPosition ret =
          TypeParam.createTypePosition(lt.returnType, gen, TypeResolutionPositionType.RETURN);
      if (ret != null) {
        gens.add(ret);
      }
      List<TypeResolutionPosition> positions =
          TypeParam.createTypePositions(
              lt.returnType, lt.params, MapExt.Map(), ListExt.asList(), gen);
      ListExt.addAll(gens, positions);
    }
    if (!gens.isEmpty()) {
      TypeResolutionPosition pos = new TypeResolutionPosition(posType);
      pos.gens = gens;
      return pos;
    }
    return null;
  }

  public PropType resolve(ValidationContext ctx) {
    if (this.actualType != null) {
      return this.actualType;
    }
    if (this.typeVar != null) {
      return this.typeVar;
    }
    ClassType type = new ClassType(this.name);
    PropType parent = ctx.typeOrObjectData(this.extendType);
    type.extendsValue = parent;
    this.actualType = type;
    return this.actualType;
  }
}
