package classes;

import d3e.core.ListExt;
import java.util.List;

public class ArrayExpression extends Expression {
  public DataType enforceType;
  public DataType valueType;
  public List<ArrayItem> values = ListExt.asList();
  public ArrayType type;

  public ArrayExpression() {}
}
