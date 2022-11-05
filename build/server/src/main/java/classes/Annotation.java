package classes;

import d3e.core.ListExt;
import java.util.List;

public class Annotation {
  public MethodCall call;
  public List<Comment> comments = ListExt.asList();

  public Annotation(MethodCall call) {
    this.call = call;
  }
}
