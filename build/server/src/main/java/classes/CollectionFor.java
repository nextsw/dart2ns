package classes;

import java.util.List;

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

  public void collectUsedTypes(List<DataType> types) {
    this.stmt.collectUsedTypes(types);
    this.value.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.stmt.simplify(s);
  }
}
