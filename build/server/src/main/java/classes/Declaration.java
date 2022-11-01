package classes;

public class Declaration extends Statement {
  public DataType type;
  public String name;
  public Expression assignment;

  public Declaration(Expression assignment, String name, DataType type) {
    this.assignment = assignment;
    this.name = name;
    this.type = type;
  }
}
