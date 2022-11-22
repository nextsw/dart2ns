package classes;

import d3e.core.ListExt;
import d3e.core.StringBuilderExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Set;

public class FunctionType extends DataType {
  public DataType returnType;
  public List<MethodParam> params = ListExt.asList();
  public List<DataType> typeArgs = ListExt.asList();
  public String signature;

  public FunctionType(
      boolean optional, List<MethodParam> params, DataType returnType, List<DataType> typeArgs) {
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
      for (MethodParam m : this.params) {
        m.dataType.collectUsedTypes(types);
      }
    }
  }

  public String computeSignature() {
    StringBuilder sb = StringBuilderExt.StringBuffer("");
    StringBuilderExt.write(sb, "__fn_");
    if (this.params != null) {
      for (MethodParam m : this.params) {
        if (m.dataType.name == null || StringExt.length(m.dataType.name) == 1l) {
          StringBuilderExt.write(sb, "Object");
        } else {
          StringBuilderExt.write(sb, m.dataType.name);
        }
        StringBuilderExt.write(sb, "_");
      }
    }
    if (this.returnType != null && this.returnType.name != null) {
      if (StringExt.length(this.returnType.name) == 1l) {
        StringBuilderExt.write(sb, "Object");
      } else {
        StringBuilderExt.write(sb, this.returnType.name);
      }
    } else {
      StringBuilderExt.write(sb, "void");
    }
    return sb.toString();
  }
}
