package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

public abstract class Expression {
  public List<Comment> comments = ListExt.asList();
  public DataType resolvedType;
  public boolean hasAwait = false;

  public abstract void resolve(ResolveContext conext);

  public abstract void collectUsedTypes(Set<String> types);
}
