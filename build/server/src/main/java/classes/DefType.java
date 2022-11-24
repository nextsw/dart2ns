package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

public class DefType extends DataType {
  public String in;
  public TypeParams params;

  public DefType(String name, boolean optional) {
    this.name = name;
    this.optional = optional;
  }

  public void collectUsedTypes(Set<String> types) {}

  public PropType type(ValidationContext ctx) {
    /*
     FIXME
    */
    return null;
  }

  public PropType rawType(ValidationContext ctx) {
    /*
     FIXME
    */
    return null;
  }

  public List<DataType> getTypeArguments() {
    return ListExt.asList();
  }
}
