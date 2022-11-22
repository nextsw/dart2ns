package classes;

import d3e.core.ListExt;
import d3e.core.StringExt;
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

  public boolean getIsEmpty() {
    return this.optionalParams.isEmpty()
        && this.namedParams.isEmpty()
        && this.positionalParams.isEmpty();
  }

  public List<MethodParam> toFixedParams() {
    ListExt.addAll(this.positionalParams, this.optionalParams);
    ListExt.sort(
        this.namedParams,
        (a, b) -> {
          return StringExt.compareTo(a.name, b.name);
        });
    ListExt.addAll(this.positionalParams, this.namedParams);
    return this.positionalParams;
  }
}
