package classes;

import d3e.core.ListExt;
import java.util.List;

public class Block extends Statement {
  public List<Comment> afterComments = ListExt.asList();
  public List<Statement> statements = ListExt.asList();

  public Block() {}

  public void resolve(ResolveContext context) {
    context.scope = new Scope(context.scope, null);
    for (Statement stmt : this.statements) {
      stmt.resolve(context);
    }
    context.scope = context.scope.parent;
  }

  public void collectUsedTypes(List<DataType> types) {
    for (Statement stmt : this.statements) {
      stmt.collectUsedTypes(types);
    }
  }

  public void simplify(Simplifier s) {
    for (long x = 0l; x < ListExt.length(this.statements); x++) {
      Statement st = ListExt.get(this.statements, x);
      s.push();
      st.simplify(s);
      SimplifierResult res = s.pop();
      List<Statement> temp = res.list;
      if (res.deleted) {
        ListExt.removeAt(this.statements, x);
      }
      if (ListExt.isNotEmpty(temp)) {
        ListExt.insertAll(this.statements, x, temp);
        x--;
      }
    }
  }

  public void visit(ExpressionVisitor visitor) {
    for (Statement stmt : this.statements) {
      visitor.visit(stmt);
    }
  }
}
