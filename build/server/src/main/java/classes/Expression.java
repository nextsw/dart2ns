package classes;

import d3e.core.ListExt;
import java.util.List;

public abstract class Expression {
  public List<Comment> comments = ListExt.asList();
  public DataType resolvedType;
}
