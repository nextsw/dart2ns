package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

public class FunctionType extends DataType {
  public DataType returnType;
  public MethodParams params;
  public List<DataType> typeArgs = ListExt.asList();

  public FunctionType(
      boolean optional, MethodParams params, DataType returnType, List<DataType> typeArgs) {
    this.optional = optional;
    this.params = params;
    this.returnType = returnType;
    this.typeArgs = typeArgs;
  }

  public String toString() {
    String res = this.returnType != null ? this.returnType.toString() : "void";
    if (ListExt.isNotEmpty(this.typeArgs)) {
      res = res + "<" + this.typeArgs.toString() + ">";
    }
    if (this.params != null) {
      res = res + this.params.toString();
    } else {
      res = res + "()";
    }
    return res;
  }

  public void collectUsedTypes(Set<String> types) {
    if (this.returnType != null) {
      this.returnType.collectUsedTypes(types);
    }
    if (this.params != null) {
      for (MethodParam m : this.params.positionalParams) {
        m.dataType.collectUsedTypes(types);
      }
      for (MethodParam m : this.params.optionalParams) {
        m.dataType.collectUsedTypes(types);
      }
      for (MethodParam m : this.params.namedParams) {
        m.dataType.collectUsedTypes(types);
      }
    }
  }
}
