package classes;

import d3e.core.ListExt;
import java.util.List;

public class SwitchStatement extends Statement {
  public Expression test;
  public List<SwitchCaseBlock> cases = ListExt.asList();
  public List<Statement> defaults = ListExt.asList();

  public SwitchStatement(Expression test) {
    this.test = test;
  }

  public void resolve(ResolveContext context) {
    this.test.resolve(context);
    this.cases.forEach(
        (c) -> {
          c.resolve(context);
        });
    this.defaults.forEach(
        (d) -> {
          d.resolve(context);
        });
  }

  public void collectUsedTypes(List<DataType> types) {
    this.test.collectUsedTypes(types);
    this.cases.forEach(
        (c) -> {
          c.collectUsedTypes(types);
        });
    this.defaults.forEach(
        (d) -> {
          d.collectUsedTypes(types);
        });
  }

  public void simplify(Simplifier s) {
    this.test = s.makeSimple(this.test);
    this.cases.forEach(
        (c) -> {
          for (Statement ss : c.statements) {
            ss.simplify(s);
          }
        });
    this.defaults.forEach(
        (d) -> {
          d.simplify(s);
        });
  }
}
