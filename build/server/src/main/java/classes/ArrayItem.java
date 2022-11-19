package classes;

import d3e.core.ListExt;
import java.util.List;

public abstract class ArrayItem extends Expression {
  public List<Comment> afterComments = ListExt.asList();
  public List<Comment> beforeComments = ListExt.asList();
}
