package classes;

public class MethodParamUtil {
  public MethodParamUtil() {}

  public static PropType type(MethodParam on, ValidationContext ctx, PropType declType) {
    if (on.dataType instanceof FunctionType) {
      FunctionType fnType = ((FunctionType) on.dataType);
      return ctx.createLambdaFunctionType(
          declType, ctx.typeOrObjectData(fnType.returnType), on.name, fnType.params, null, false);
    } else {
      return MethodParamUtil.findParamType(ctx, on, declType);
    }
  }

  public static PropType findParamType(
      ValidationContext ctx, MethodParam param, PropType declType) {
    if (param.thisToken != null) {
      FieldDecl field = declType.findField(param.name, false);
      if (field != null) {
        return field.type.resolvedType;
      } else {
        return ctx.object();
      }
    }
    return ctx.typeOrObjectData(param.dataType);
  }
}
