package classes;

import d3e.core.ListExt;
import java.util.List;

public class SimplifierResult {
  public boolean deleted = false;
  public List<Statement> list = ListExt.asList();

  public SimplifierResult() {}
}
