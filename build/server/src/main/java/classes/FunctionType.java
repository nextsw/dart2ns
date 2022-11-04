package classes;

import java.util.List;

import d3e.core.ListExt;

public class FunctionType implements DataType {
  public DataType returnType;
  public MethodParams params;
  public boolean optional = false;
  public List<DataType> typeArgs = ListExt.asList();

  public FunctionType(boolean optional, MethodParams params, DataType returnType, List<DataType> typeArgs) {
    this.optional = optional;
    this.params = params;
    this.returnType = returnType;
    this.typeArgs = typeArgs;
  }
}
