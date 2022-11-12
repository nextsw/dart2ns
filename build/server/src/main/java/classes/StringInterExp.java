package classes;

import d3e.core.ListExt;
import java.util.List;

public class StringInterExp extends Expression {
  public String str;
  public List<Expression> values = ListExt.asList();

  public StringInterExp(String str) {
    this.str = str;
  }
}
