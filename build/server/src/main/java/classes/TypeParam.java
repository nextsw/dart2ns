package classes;

public class TypeParam {
  public String name;
  public DataType extendType;
  public DataType resolvedType;

  public TypeParam(DataType extendType, String name) {
    this.extendType = extendType;
    this.name = name;
  }
}
