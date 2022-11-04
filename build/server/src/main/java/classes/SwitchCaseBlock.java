package classes;

import d3e.core.ListExt;
import java.util.List;

public class SwitchCaseBlock {
  public List<Expression> tests = ListExt.asList();
  public List<Statement> statements = ListExt.asList();
public String label;

  public SwitchCaseBlock() {}
}
