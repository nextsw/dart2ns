package classes;

public class CollectionFor extends ArrayItem {
  public Statement stmt;
  public ArrayItem value;

  public CollectionFor(Statement stmt, ArrayItem value) {
    this.stmt = stmt;
    this.value = value;
  }
}
