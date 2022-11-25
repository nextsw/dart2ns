package classes;

import d3e.core.ListExt;
import java.util.List;

public class NamedArgument {
  public String name;
  public List<Comment> beforeComments = ListExt.asList();
  public List<Comment> afterComments = ListExt.asList();
  public Expression value;
  public DataType resolvedType;

  public NamedArgument(List<Comment> afterComments, String name, Expression value) {
    this.afterComments = afterComments;
    this.name = name;
    this.value = value;
  }
}
