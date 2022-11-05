package classes;

public class DefType extends DataType {
  public String in;
  public boolean optional = false;
  public TypeParams params;

  public DefType(String name, boolean optional) {
    this.name = name;
    this.optional = optional;
  }
}
