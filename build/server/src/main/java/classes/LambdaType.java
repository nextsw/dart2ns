package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import java.util.List;

public class LambdaType extends PropType {
  public PropType returnType;
  public List<PropType> params = ListExt.asList();

  public LambdaType(String name) {
    super(name);
  }

  public String toString() {
    return FormateUtil.toStringLambdaType(this);
  }

  public static LambdaType withMethod(MethodDecl method) {
    LambdaType type = new LambdaType("()->");
    type.returnType = method.returnType.resolvedType;
    type.params =
        IterableExt.toList(
            ListExt.map(
                method.allParams,
                (p) -> {
                  return p.dataType.resolvedType;
                }),
            false);
    return type;
  }

  public boolean isAssignableFrom(PropType type) {
    if (type instanceof LambdaType) {
      LambdaType other = ((LambdaType) type);
      if (!this.returnType.isAssignableFrom(other.returnType)) {
        return false;
      }
      if (ListExt.length(this.params) != ListExt.length(other.params)) {
        return false;
      }
      for (long x = 0l; x < ListExt.length(this.params); x++) {
        PropType ours = ListExt.get(this.params, x);
        PropType theirs = ListExt.get(other.params, x);
        if (!theirs.isAssignableFrom(ours)) {
          return false;
        }
      }
      return true;
    }
    return super.isAssignableFrom(type);
  }

  public boolean canAssignTo(PropType propType) {
    LambdaType findLambdaFunction = propType.findLambdaFunction();
    if (findLambdaFunction == null) {
      return false;
    }
    return findLambdaFunction.isAssignableFrom(this);
  }
}
