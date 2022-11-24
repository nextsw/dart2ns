package classes;

import d3e.core.ListExt;
import java.util.List;

public class ArrayExpression extends Expression {
  public DataType enforceType;
  public DataType valueType;
  public List<ArrayItem> values = ListExt.asList();
  public ArrayType type;
  public PropType elementType;

  public ArrayExpression() {}

  public boolean getList() {
    return this.type == ArrayType.List;
  }

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

  public void collectUsedTypes(List<DataType> types) {
    for (ArrayItem item : this.values) {
      item.collectUsedTypes(types);
    }
    if (this.enforceType != null) {
      types.add(this.enforceType);
    }
    if (this.valueType != null) {
      types.add(this.valueType);
    }
  }

  public void simplify(Simplifier s) {
    for (ArrayItem item : this.values) {
      item.simplify(s);
    }
  }

  public void visit(ExpressionVisitor visitor) {
    for (ArrayItem item : this.values) {
      visitor.visit(item);
    }
  }
}
