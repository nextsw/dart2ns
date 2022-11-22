package classes;

import d3e.core.ListExt;
import java.util.List;

public abstract class Expression {
  public List<Comment> comments = ListExt.asList();
  public DataType resolvedType;
  public boolean hasAwait = false;

  public abstract void resolve(ResolveContext conext);

  public abstract void collectUsedTypes(List<DataType> types);

  public abstract void simplify(Simplifier s);
}
