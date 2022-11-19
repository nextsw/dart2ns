package classes;

import java.util.Set;

public class CollectionFor extends ArrayItem {
  public Statement stmt;
  public ArrayItem value;

  public CollectionFor(Statement stmt, ArrayItem value) {
    this.stmt = stmt;
    this.value = value;
  }

  public void resolve(ResolveContext context) {
    this.stmt.resolve(context);
    this.value.resolve(context);
  }

  public void collectUsedTypes(Set<String> types) {
    this.stmt.collectUsedTypes(types);
    this.value.collectUsedTypes(types);
  }
}
