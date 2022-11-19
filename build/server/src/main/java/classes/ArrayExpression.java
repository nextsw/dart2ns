package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

public class ArrayExpression extends Expression {
  public DataType enforceType;
  public DataType valueType;
  public List<ArrayItem> values = ListExt.asList();
  public ArrayType type;

  public ArrayExpression() {}

  public void resolve(ResolveContext context) {
    for (ArrayItem item : this.values) {
      item.resolve(context);
    }
    if (this.enforceType != null) {
      this.resolvedType = this.enforceType;
    } else if (ListExt.isNotEmpty(this.values)) {
      this.resolvedType = ListExt.first(this.values).resolvedType;
    } else {
      this.resolvedType = new ValueType("List", false);
    }
  }

  public void collectUsedTypes(Set<String> types) {
    for (ArrayItem item : this.values) {
      item.collectUsedTypes(types);
    }
    if (this.enforceType != null) {
      this.enforceType.collectUsedTypes(types);
    }
    if (this.valueType != null) {
      this.valueType.collectUsedTypes(types);
    }
  }
}
