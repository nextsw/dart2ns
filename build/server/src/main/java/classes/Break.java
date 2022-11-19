package classes;

import java.util.Set;

public class Break extends Statement {
  public String label;

  public Break(String label) {
    this.label = label;
  }

  public void resolve(ResolveContext context) {}

  public void collectUsedTypes(Set<String> types) {}
}
