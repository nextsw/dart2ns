package classes;

import java.util.List;
import java.util.Objects;

public class ForEachLoop extends Statement {
  public Expression body;
  public DataType dataType;
  public String name;
  public Expression collection;

  public ForEachLoop(Expression body, Expression collection, DataType dataType, String name) {
    this.body = body;
    this.collection = collection;
    this.dataType = dataType;
    this.name = name;
  }

  public void resolve(ResolveContext context) {
    this.collection.resolve(context);
    if (this.dataType == null || Objects.equals(this.dataType.name, "var")) {
      this.dataType = context.subType(this.collection.resolvedType, 0l);
    }
    context.scope.add(this.name, this.dataType);
    if (this.body != null) {
      this.body.resolve(context);
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    if (this.dataType != null) {
      types.add(this.dataType);
    }
    if (this.body != null) {
      this.body.collectUsedTypes(types);
    }
    this.collection.collectUsedTypes(types);
  }

  public void simplify(Simplifier s) {
    this.collection = s.makeSimple(this.collection);
    if (this.body == null) {
      this.body = new Block();
    }
    if (!(this.body instanceof Block)) {
      Block b = new Block();
      b.statements.add(((Statement) this.body));
      this.body = b;
    }
    this.body.simplify(s);
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.collection);
    visitor.visit(this.body);
  }
}
