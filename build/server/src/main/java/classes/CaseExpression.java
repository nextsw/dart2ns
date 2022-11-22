package classes;

import d3e.core.ListExt;
import java.util.List;

public class CaseExpression {
  public List<Expression> tests = ListExt.asList();
  public Expression result;

  public CaseExpression() {}

  public void resolve(ResolveContext context) {
    this.tests.forEach(
        (c) -> {
          c.resolve(context);
        });
    this.result.resolve(context);
  }

  public void collectUsedTypes(List<DataType> types) {
    this.tests.forEach(
        (c) -> {
          c.collectUsedTypes(types);
        });
    this.result.collectUsedTypes(types);
  }
}
