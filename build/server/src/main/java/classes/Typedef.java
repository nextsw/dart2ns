package classes;

import d3e.core.ListExt;
import java.util.List;

public class Typedef extends TopDecl {
  public ValueType type;
  public FunctionType fnType;
  public List<Annotation> annotations = ListExt.asList();

  public Typedef(ValueType type, FunctionType fnType) {
    super(type.name, TopDeclType.Typedef, "");
    this.type = type;
    this.fnType = fnType;
  }
}
