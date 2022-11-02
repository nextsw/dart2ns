package classes;

import d3e.core.ListExt;
import java.util.List;

public class Typedef extends TopDecl {
  public TypeParams generics;
  public MethodDecl method;
  public List<Annotation> annotations = ListExt.asList();
  public String content;

  public Typedef(String name) {
    super(name, TopDeclType.Typedef, "");
  }
}
