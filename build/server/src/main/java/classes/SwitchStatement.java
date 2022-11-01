package classes;

import d3e.core.ListExt;
import java.util.List;

public class SwitchStatement extends Statement {
  public Expression test;
  public List<SwitchCaseBlock> cases = ListExt.asList();
  public List<Statement> defaults = ListExt.asList();

  public SwitchStatement(Expression test) {
    this.test = test;
  }
}
