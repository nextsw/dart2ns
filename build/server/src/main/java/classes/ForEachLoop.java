package classes;

import java.util.Set;

public class ForEachLoop extends Statement {
  public Expression body;
  public DataType dataType;
  public String name;
  public Expression collection;

  public ForEachLoop(Expression body, Expression collection, DataType dataType, String name) {
    this.body = body;
    this.collection = collection;
    this.dataType = dataType;
    this.name = name;
  }

  public void resolve(ResolveContext context) {
    this.collection.resolve(context);
    if (this.body != null) {
      this.body.resolve(context);
    }
  }

  public void collectUsedTypes(Set<String> types) {
    if (this.dataType != null) {
      this.dataType.collectUsedTypes(types);
    }
    if (this.body != null) {
      this.body.collectUsedTypes(types);
    }
    this.collection.collectUsedTypes(types);
  }
}
