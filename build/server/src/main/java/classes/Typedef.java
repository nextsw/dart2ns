package classes;

import d3e.core.ListExt;
import java.util.List;

public class Typedef extends TopDecl {
  public DefType type;
  public FunctionType fnType;
  public List<Annotation> annotations = ListExt.asList();

  public Typedef(DefType type, FunctionType fnType) {
    super(fnType.name, TopDeclType.Typedef, "");
    this.type = type;
    this.fnType = fnType;
  }
}
