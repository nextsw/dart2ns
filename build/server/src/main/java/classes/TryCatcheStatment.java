package classes;

import d3e.core.ListExt;
import java.util.List;

public class TryCatcheStatment extends Statement {
  public Block body;
  public Block finallyBody;
  public List<CatchPart> catchParts = ListExt.asList();

  public TryCatcheStatment(Block body, List<CatchPart> catchParts, Block finallyBody) {
    this.body = body;
    this.catchParts = catchParts;
    this.finallyBody = finallyBody;
  }

  public void resolve(ResolveContext context) {
    this.body.resolve(context);
    if (this.finallyBody != null) {
      this.finallyBody.resolve(context);
    }
    for (CatchPart c : this.catchParts) {
      c.body.resolve(context);
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    this.body.collectUsedTypes(types);
    if (this.finallyBody != null) {
      this.finallyBody.collectUsedTypes(types);
    }
    for (CatchPart c : this.catchParts) {
      this.body.collectUsedTypes(types);
      if (c.onType != null) {
        types.add(c.onType);
      }
    }
  }

  public void simplify(Simplifier s) {
    this.body.simplify(s);
    if (this.finallyBody != null) {
      this.finallyBody.simplify(s);
    }
    for (CatchPart c : this.catchParts) {
      c.body.simplify(s);
    }
  }

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.body);
    visitor.visit(this.finallyBody);
    for (CatchPart c : this.catchParts) {
      visitor.visit(c.body);
    }
  }
}
