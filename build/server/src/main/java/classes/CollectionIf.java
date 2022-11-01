package classes;

public class CollectionIf extends ArrayItem {
  public Expression test;
  public ArrayItem thenItem;
  public ArrayItem elseItem;

  public CollectionIf(ArrayItem elseItem, Expression test, ArrayItem thenItem) {
    this.elseItem = elseItem;
    this.test = test;
    this.thenItem = thenItem;
  }
}
