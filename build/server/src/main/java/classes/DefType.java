package classes;

public class DefType implements DataType {
  public String in;
  public String name;
  public boolean optional = false;
  public TypeParams params;

  public DefType(String name, boolean optional) {
    this.name = name;
    this.optional = optional;
  }
}
