package classes;

public class InlineMethodStatement extends Statement {
  public MethodDecl method;

  public InlineMethodStatement(MethodDecl method) {
    this.method = method;
  }
}
