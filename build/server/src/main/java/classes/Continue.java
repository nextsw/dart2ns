package classes;

import java.util.Set;

public class Continue extends Statement {
  public String label;

  public Continue(String label) {
    this.label = label;
  }

  public void collectUsedTypes(Set<String> types) {}

  public void resolve(ResolveContext context) {}
}
