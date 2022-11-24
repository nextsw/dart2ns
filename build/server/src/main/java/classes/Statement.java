package classes;

import d3e.core.ListExt;
import java.util.List;

public abstract class Statement extends Expression {
  public List<Comment> comments = ListExt.asList();
  public List<LocalVar> finalVars = ListExt.asList();
  public PropType returnType;

  public abstract void simplify(Simplifier s);
}
