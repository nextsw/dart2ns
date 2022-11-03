package classes;

public class Declaration extends Statement {
  public DataType type;
  public String name;
  public Expression assignment;
  public boolean isFinal = false;
  public boolean isConst = false;
  public boolean isLate = false;

  public Declaration(
      Expression assignment,
      boolean isConst,
      boolean isFinal,
      boolean isLate,
      String name,
      DataType type) {
    this.assignment = assignment;
    this.isConst = isConst;
    this.isFinal = isFinal;
    this.isLate = isLate;
    this.name = name;
    this.type = type;
  }
}
