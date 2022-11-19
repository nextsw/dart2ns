package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

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

  public void collectUsedTypes(Set<String> types) {
    for (Expression exp : this.values) {
      exp.collectUsedTypes(types);
    }
  }
}
