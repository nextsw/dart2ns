package classes;

import d3e.core.ListExt;
import java.util.List;

public class Argument {
  public Expression arg;
  public Range range;
  public List<Comment> afterComments = ListExt.asList();

  public Argument(List<Comment> afterComments, Expression arg) {
    this.afterComments = afterComments;
    this.arg = arg;
  }
}
