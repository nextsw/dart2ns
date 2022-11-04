package classes;

import java.util.List;

import d3e.core.ListExt;

public class Declaration extends Statement {
  public DataType type;
  public List<String> names = ListExt.asList();
  public Expression assignment;
  public boolean isFinal = false;
  public boolean isConst = false;
  public boolean isLate = false;

  public Declaration(
      Expression assignment,
      boolean isConst,
      boolean isFinal,
      boolean isLate,
      List<String> names,
      DataType type) {
    this.assignment = assignment;
    this.isConst = isConst;
    this.isFinal = isFinal;
    this.isLate = isLate;
    this.names = names;
    this.type = type;
  }
}
