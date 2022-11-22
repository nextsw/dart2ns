package classes;

import java.util.List;

public class LabelStatement extends Statement {
  public String name;

  public LabelStatement(String name) {
    this.name = name;
  }

  public void resolve(ResolveContext context) {}

  public void collectUsedTypes(List<DataType> types) {}

  public void simplify(Simplifier s) {}
}
