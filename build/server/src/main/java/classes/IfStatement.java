package classes;

import d3e.core.MapExt;
import java.util.List;
import java.util.Map;

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
    Map<String, String> typeChecks = MapExt.Map();
    this.test.getTypeChecks(typeChecks);
    this.test.resolve(context);
    if (MapExt.isNotEmpty(typeChecks)) {
      context.scope = new Scope(context.scope, null);
      typeChecks.forEach(
          (k, v) -> {
            context.scope.add(k, new ValueType(v, false));
          });
    }
    this.thenStatement.resolve(context);
    if (MapExt.isNotEmpty(typeChecks)) {
      context.scope = context.scope.parent;
    }
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

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.test);
    visitor.visit(this.thenStatement);
    visitor.visit(this.elseStatement);
  }
}
