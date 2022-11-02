package classes;

public class FunctionType implements DataType {
  public DataType returnType;
  public MethodParams params;
  public boolean optional = false;

  public FunctionType(boolean optional, MethodParams params, DataType returnType) {
    this.optional = optional;
    this.params = params;
    this.returnType = returnType;
  }
}
