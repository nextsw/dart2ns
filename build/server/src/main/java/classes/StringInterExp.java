package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import java.util.List;

public class StringInterExp extends Expression {
  public String str;
  public List<Expression> values = ListExt.asList();

  public StringInterExp(String str) {
    this.str = str;
  }

  public void resolve(ResolveContext context) {
    this.resolvedType = context.stringType;
    for (Expression exp : this.values) {
      exp.resolve(context);
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    for (Expression exp : this.values) {
      exp.collectUsedTypes(types);
    }
  }

  public void simplify(Simplifier s) {
    this.values =
        IterableExt.toList(
            ListExt.map(
                this.values,
                (v) -> {
                  return s.makeSimple(v);
                }),
            false);
  }
}
