package classes;

import d3e.core.ListExt;
import java.util.List;

public class SwitchExpression extends Expression {
  public Expression on;
  public List<CaseExpression> cases = ListExt.asList();
  public Expression onElse;

  public SwitchExpression(List<CaseExpression> cases, Expression on) {
    this.cases = cases;
    this.on = on;
  }
}
