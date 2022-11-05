package classes;

import d3e.core.ListExt;
import java.util.List;

public class Declaration extends Statement {
  public DataType type;
  public List<NameAndValue> names = ListExt.asList();
  public boolean isFinal = false;
  public boolean isConst = false;
  public boolean isLate = false;

  public Declaration(
      boolean isConst, boolean isFinal, boolean isLate, List<NameAndValue> names, DataType type) {
    this.isConst = isConst;
    this.isFinal = isFinal;
    this.isLate = isLate;
    this.names = names;
    this.type = type;
  }
}
