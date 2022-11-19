package classes;

import java.util.Set;

public class DefType extends DataType {
  public String in;
  public TypeParams params;

  public DefType(String name, boolean optional) {
    this.name = name;
    this.optional = optional;
  }

  public void collectUsedTypes(Set<String> types) {}
}
