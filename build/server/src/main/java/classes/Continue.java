package classes;

import java.util.List;

public class Continue extends Statement {
  public String label;

  public Continue(String label) {
    this.label = label;
  }

  public void collectUsedTypes(List<DataType> types) {}

  public void resolve(ResolveContext context) {}

  public void simplify(Simplifier s) {}
}
