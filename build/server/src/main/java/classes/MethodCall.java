package classes;

import d3e.core.ListExt;
import java.util.List;

public class MethodCall extends Statement {
  public String name;
  public List<DataType> typeArgs = ListExt.asList();
  public List<Argument> positionArgs = ListExt.asList();
  public List<NamedArgument> namedArgs = ListExt.asList();
  public boolean checkNull = false;
  public boolean notNull = false;
  public Expression on;
  public String factoryNme;

  public MethodCall(
      String name,
      List<NamedArgument> namedArgs,
      List<Argument> positionArgs,
      List<DataType> typeArgs,
      String factoryName) {
    this.name = name;
    this.namedArgs = namedArgs;
    this.positionArgs = positionArgs;
    this.typeArgs = typeArgs;
    this.factoryNme = factoryName;
  }
}
