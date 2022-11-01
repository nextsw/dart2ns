package classes;

import d3e.core.ListExt;
import java.util.List;

public class Block extends Statement {
  public List<Comment> afterComments = ListExt.asList();
  public List<Statement> statements = ListExt.asList();

  public Block() {}
}
