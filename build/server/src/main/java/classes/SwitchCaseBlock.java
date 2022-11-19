package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Set;

public class SwitchCaseBlock {
  public List<Expression> tests = ListExt.asList();
  public List<Statement> statements = ListExt.asList();
  public String label;

  public SwitchCaseBlock() {}

  public void resolve(ResolveContext context) {
    this.tests.forEach(
        (c) -> {
          c.resolve(context);
        });
    this.statements.forEach(
        (d) -> {
          d.resolve(context);
        });
  }

  public void collectUsedTypes(Set<String> types) {
    this.tests.forEach(
        (c) -> {
          c.collectUsedTypes(types);
        });
    this.statements.forEach(
        (d) -> {
          d.collectUsedTypes(types);
        });
  }
}
