package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

public class ValueType extends DataType {
  public String in;
  public List<DataType> args = ListExt.asList();

  public ValueType(String name, boolean optional) {
    this.name = name;
    this.optional = optional;
  }

  public String toString() {
    String res = this.name;
    if (ListExt.isNotEmpty(this.args)) {
      res = res + "<" + this.args.toString() + ">";
    }
    return res;
  }

  public void collectUsedTypes(Set<String> types) {
    types.add(this.name);
  }
}
