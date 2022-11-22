package classes;

import d3e.core.ListExt;
import java.util.List;

public class CollectionSpread extends ArrayItem {
  public Expression values;
  public List<Comment> beforeComments = ListExt.asList();
  public boolean checkNull = false;

  public CollectionSpread(List<Comment> beforeComments, boolean checkNull, Expression values) {
    this.beforeComments = beforeComments;
    this.checkNull = checkNull;
    this.values = values;
  }

  public void resolve(ResolveContext context) {
    this.values.resolve(context);
  }

  public void collectUsedTypes(List<DataType> types) {
    this.values.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.values = s.makeSimple(this.values);
  }
}
