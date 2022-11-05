package classes;

import d3e.core.ListExt;
import java.util.List;

public class ForLoop extends Statement {
  public Expression body;
  public Declaration decl;
  public Expression test;
  public List<Statement> inits = ListExt.asList();
  public List<Statement> resets = ListExt.asList();

  public ForLoop(
      Expression body,
      Declaration decl,
      List<Statement> inits,
      List<Statement> resets,
      Expression test) {
    this.body = body;
    this.decl = decl;
    this.inits = inits;
    this.resets = resets;
    this.test = test;
  }
}
