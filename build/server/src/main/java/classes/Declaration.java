package classes;

public class Declaration extends Statement {
  public DataType type;
  public String name;
  public Expression assignment;
  public boolean isFinal;
  public boolean isLate;

  public Declaration(Expression assignment, 
		  String name, DataType type, 
		  boolean isFinal,
		  boolean isLate) {
    this.assignment = assignment;
    this.name = name;
    this.type = type;
    this.isFinal = isFinal;
    this.isLate = isLate;
  }
}
