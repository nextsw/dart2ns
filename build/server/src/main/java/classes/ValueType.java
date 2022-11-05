package classes;

import d3e.core.ListExt;
import java.util.List;

public class ValueType extends DataType {
  public String in;
  public List<DataType> args = ListExt.asList();

  public ValueType(String name, boolean optional) {
    this.name = name;
    this.optional = optional;
  }
}
