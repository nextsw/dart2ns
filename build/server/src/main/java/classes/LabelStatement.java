package classes;

import java.util.Set;

public class LabelStatement extends Statement {
  public String name;

  public LabelStatement(String name) {
    this.name = name;
  }

  public void resolve(ResolveContext context) {}

  public void collectUsedTypes(Set<String> types) {}
}
