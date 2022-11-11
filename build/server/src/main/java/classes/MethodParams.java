package classes;

import d3e.core.ListExt;
import java.util.List;

public class MethodParams {
  public List<MethodParam> optionalParams = ListExt.asList();
  public List<MethodParam> namedParams = ListExt.asList();
  public List<MethodParam> positionalParams = ListExt.asList();

  public MethodParams() {}

  public String toString() {
    return this.positionalParams.toString()
        + this.optionalParams.toString()
        + this.namedParams.toString();
  }
}
