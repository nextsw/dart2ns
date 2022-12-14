package classes;

import d3e.core.ListExt;
import java.util.List;

public class TokenFrame {
  public TypeToken tok;
  public TypeToken prevTok;
  public TypeToken peekTok;
  public TypeToken peekTok2;
  public TypeToken peekTok3;
  public TypeToken peekTok4;
  public long pos = 0l;
  public InsideInfo insideInfo = null;
  public boolean doingDollor = false;
  public List<InsideInfo> stack = ListExt.asList();

  public TokenFrame(
      boolean doingDollor,
      InsideInfo insideInfo,
      TypeToken peekTok,
      TypeToken peekTok2,
      TypeToken peekTok3,
      TypeToken peekTok4,
      long pos,
      TypeToken prevTok,
      TypeToken tok) {
    this.doingDollor = doingDollor;
    this.insideInfo = insideInfo;
    this.peekTok = peekTok;
    this.peekTok2 = peekTok2;
    this.peekTok3 = peekTok3;
    this.peekTok4 = peekTok4;
    this.pos = pos;
    this.prevTok = prevTok;
    this.tok = tok;
  }
}
