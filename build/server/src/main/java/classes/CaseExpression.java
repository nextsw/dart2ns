package classes;

import d3e.core.ListExt;
import java.util.List;

public class CaseExpression {
  public List<Expression> tests = ListExt.asList();
  public Expression result;

  public CaseExpression() {}
}
