package classes;

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
}
