package classes;

import d3e.core.ListExt;
import java.util.List;

public class DataType {
  public String name;
  public boolean optional = false;
  public List<DataType> args = ListExt.asList();

  public DataType(String name, boolean optional) {
    this.name = name;
    this.optional = optional;
  }
}
