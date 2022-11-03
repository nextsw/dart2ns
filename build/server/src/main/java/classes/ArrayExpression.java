package classes;

import d3e.core.ListExt;
import java.util.List;

public class ArrayExpression extends Expression {
  public DataType enforceType;
  public List<ArrayItem> values = ListExt.asList();
  public boolean list = false;
public DataType valueType;

  public ArrayExpression() {}
}
