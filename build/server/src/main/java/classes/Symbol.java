package classes;

import java.util.Set;

public class Symbol extends Expression {
  public String name;

  public Symbol(String name) {
    this.name = name;
  }

  public void resolve(ResolveContext context) {
    this.resolvedType = context.objectType;
  }

  public void collectUsedTypes(Set<String> types) {}
}
