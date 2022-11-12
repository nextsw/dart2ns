package classes;

public class InsideInfo {
  public boolean insideString = false;
  public boolean isMultiLine = false;
  public String quote;
  public boolean isRaw = false;
  public boolean insideDollar = false;
  public boolean insideDollarExpr = false;

  public InsideInfo(
      boolean insideDollar,
      boolean insideDollarExpr,
      boolean insideString,
      boolean isMultiLine,
      boolean isRaw,
      String quote) {
    this.insideDollar = insideDollar;
    this.insideDollarExpr = insideDollarExpr;
    this.insideString = insideString;
    this.isMultiLine = isMultiLine;
    this.isRaw = isRaw;
    this.quote = quote;
  }
}
