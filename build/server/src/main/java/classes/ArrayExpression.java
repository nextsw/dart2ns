package classes;

import d3e.core.ListExt;
import java.util.List;

public class ArrayExpression extends Expression {
  public static long LIST = 0;
  public static long SET = 1;
  public static long MAP = 2;
  public DataType enforceType;
  public DataType valueType;
  public List<ArrayItem> values = ListExt.asList();
  public long type;

  public ArrayExpression() {}
}
