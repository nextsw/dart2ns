package classes;

public class IfStatement extends Statement {
  public Expression test;
  public Statement thenStatement;
  public Statement elseStatement;

  public IfStatement(Statement elseStatement, Expression test, Statement thenStatement) {
    this.elseStatement = elseStatement;
    this.test = test;
    this.thenStatement = thenStatement;
  }
}
