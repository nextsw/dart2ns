package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.MapExt;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TypeParams {
  public List<TypeParam> params = ListExt.asList();

  public TypeParams() {}

  public List<TypeVariable> resolveRawTypes(
      ValidationContext ctx, PropType declarType, PropType retType, MethodParams methodParams) {
    if (this.params.isEmpty()) {
      return ListExt.List(0l);
    }
    List<PropType> positionalParams =
        (methodParams == null)
            ? null
            : IterableExt.toList(
                ListExt.map(
                    methodParams.positionalParams,
                    (m) -> {
                      return MethodParamUtil.type(m, ctx, declarType);
                    }),
                false);
    List<PropType> optionalParams =
        (methodParams == null)
            ? null
            : IterableExt.toList(
                ListExt.map(
                    methodParams.optionalParams,
                    (m) -> {
                      return MethodParamUtil.type(m, ctx, declarType);
                    }),
                false);
    Map<String, PropType> namedParams =
        (methodParams == null)
            ? null
            : MapExt.fromIterable(
                methodParams.namedParams,
                (m) -> {
                  return m.name;
                },
                (m) -> {
                  return MethodParamUtil.type(m, ctx, declarType);
                });
    return IterableExt.toList(
        ListExt.map(
            this.params,
            (m) -> {
              return m.createTypeVariable(
                  ctx, retType, positionalParams, namedParams, optionalParams);
            }),
        false);
  }

  public Map<String, PropType> resolve(ValidationContext ctx) {
    if (this.params.isEmpty()) {
      return MapExt.Map();
    }
    return MapExt.fromIterable(
        this.params,
        (p) -> {
          return p.name;
        },
        (m) -> {
          return m.resolve(ctx);
        });
  }

  public PropType resolveWithType(ValidationContext ctx, String type) {
    if (this.params.isEmpty()) {
      return null;
    }
    for (TypeParam param : this.params) {
      if (Objects.equals(type, param.name)) {
        return param.createTypeVariable(ctx, null, null, null, null);
      }
    }
    return null;
  }
}
