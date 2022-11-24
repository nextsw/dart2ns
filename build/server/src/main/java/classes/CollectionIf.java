package classes;

import java.util.List;

public class CollectionIf extends ArrayItem {
  public Expression test;
  public ArrayItem thenItem;
  public ArrayItem elseItem;

  public CollectionIf(ArrayItem elseItem, Expression test, ArrayItem thenItem) {
    this.elseItem = elseItem;
    this.test = test;
    this.thenItem = thenItem;
  }

  public void resolve(ResolveContext context) {
    this.test.resolve(context);
    this.thenItem.resolve(context);
    if (this.elseItem != null) {
      this.elseItem.resolve(context);
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    this.test.collectUsedTypes(types);
    this.thenItem.collectUsedTypes(types);
    if (this.elseItem != null) {
      this.elseItem.collectUsedTypes(types);
    }
  }

  public void simplify(Simplifier s) {
    this.test = s.makeSimple(this.test);
    /*
     thenItem = s.makeSimple(thenItem);
     elseItem = s.makeSimple(elseItem);
    */
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.test);
    visitor.visit(this.thenItem);
    visitor.visit(this.elseItem);
  }
}
