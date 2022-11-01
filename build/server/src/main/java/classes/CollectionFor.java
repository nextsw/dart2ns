package classes;

public class CollectionFor extends ArrayItem {
  public String name;
  public DataType dataType;
  public Expression collection;
  public ArrayItem value;

  public CollectionFor(Expression collection, DataType dataType, String name, ArrayItem value) {
    this.collection = collection;
    this.dataType = dataType;
    this.name = name;
    this.value = value;
  }
}
