package classes;

import d3e.core.ListExt;
import java.util.List;

public class CascadeExp extends Statement {
  public Expression on;
  public List<Statement> calls = ListExt.asList();

  public CascadeExp(Expression on) {
    this.on = on;
  }
}
