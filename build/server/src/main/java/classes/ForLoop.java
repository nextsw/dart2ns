package classes;

import d3e.core.ListExt;
import java.util.List;

public class ForLoop extends Statement {
  public Expression body;
  public Declaration decl;
  public Expression test;
  public List<Statement> inits = ListExt.asList();
  public List<Statement> resets = ListExt.asList();

  public ForLoop(
      Expression body,
      Declaration decl,
      List<Statement> inits,
      List<Statement> resets,
      Expression test) {
    this.body = body;
    this.decl = decl;
    this.inits = inits;
    this.resets = resets;
    this.test = test;
  }

  public void resolve(ResolveContext context) {
    if (this.decl != null) {
      this.decl.resolve(context);
    }
    for (Statement s : this.inits) {
      s.resolve(context);
    }
    for (Statement s : this.resets) {
      s.resolve(context);
    }
    if (this.test != null) {
      this.test.resolve(context);
    }
    if (this.body != null) {
      this.body.resolve(context);
    }
  }

  public void collectUsedTypes(List<DataType> types) {
    this.inits.forEach(
        (i) -> {
          i.collectUsedTypes(types);
        });
    this.resets.forEach(
        (r) -> {
          r.collectUsedTypes(types);
        });
    if (this.decl != null) {
      this.decl.collectUsedTypes(types);
    }
    if (this.test != null) {
      this.test.collectUsedTypes(types);
    }
    if (this.body != null) {
      this.body.collectUsedTypes(types);
    }
  }

  public void simplify(Simplifier s) {
    /*
    test = s.makeSimple(test);
    */
    if (this.body == null) {
      this.body = new Block();
    }
    if (!(this.body instanceof Block)) {
      Block b = new Block();
      b.statements.add(((Statement) this.body));
      this.body = b;
    }
    this.body.simplify(s);
  }
}
