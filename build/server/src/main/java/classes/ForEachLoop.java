package classes;

public class ForEachLoop extends Statement {
  public Block body;
  public DataType dataType;
  public String name;
  public Expression collection;

  public ForEachLoop(Block body, Expression collection, DataType dataType, String name) {
    this.body = body;
    this.collection = collection;
    this.dataType = dataType;
    this.name = name;
  }
}
