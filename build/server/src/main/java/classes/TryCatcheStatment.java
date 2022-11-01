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
}
