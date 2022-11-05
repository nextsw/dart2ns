package classes;

import d3e.core.ListExt;
import java.util.List;

public class LambdaExpression extends Expression {
  public List<Param> params = ListExt.asList();
  public Expression expression;
  public Block body;
  public ASyncType asyncType = ASyncType.NONE;

  public LambdaExpression(List<Param> params) {
    this.params = params;
  }
}
