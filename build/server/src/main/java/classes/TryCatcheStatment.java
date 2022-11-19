package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

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
      this.body.resolve(context);
    }
  }

  public void collectUsedTypes(Set<String> types) {
    this.body.collectUsedTypes(types);
    if (this.finallyBody != null) {
      this.finallyBody.collectUsedTypes(types);
    }
    for (CatchPart c : this.catchParts) {
      this.body.collectUsedTypes(types);
      if (c.onType != null) {
        c.onType.collectUsedTypes(types);
      }
    }
  }
}
