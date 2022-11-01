package classes;

import d3e.core.ListExt;
import java.util.List;

public class ArrayItem extends Expression {
  public List<Comment> afterComments = ListExt.asList();
  public List<Comment> beforeComments = ListExt.asList();

  public ArrayItem() {}
}
