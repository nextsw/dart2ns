package classes;

import java.util.List;

public class IfStatement extends Statement {
  public Expression test;
  public Statement thenStatement;
  public Statement elseStatement;

  public IfStatement(Statement elseStatement, Expression test, Statement thenStatement) {
    this.elseStatement = elseStatement;
    this.test = test;
    this.thenStatement = thenStatement;
  }

  public void resolve(ResolveContext context) {
    this.test.resolve(context);
    this.thenStatement.resolve(context);
    if (this.elseStatement != null) {
      this.elseStatement.resolve(context);
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    this.test.collectUsedTypes(types);
    this.thenStatement.collectUsedTypes(types);
    if (this.elseStatement != null) {
      this.elseStatement.collectUsedTypes(types);
    }
  }

  public void simplify(Simplifier s) {
    this.test = s.makeSimple(this.test);
    this.thenStatement.simplify(s);
    if (this.elseStatement != null) {
      this.elseStatement.simplify(s);
    }
  }
}
