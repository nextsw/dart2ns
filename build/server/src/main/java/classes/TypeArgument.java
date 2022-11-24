package classes;

public class TypeArgument {
  public String name;
  public PropType type;
  public PropType resolvedType;

  public TypeArgument(String name, PropType type) {
    this.name = name;
    this.type = type;
  }
}
