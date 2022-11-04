package classes;

import java.util.List;

import d3e.core.ListExt;

public class Annotation {
  public MethodCall call;
public List<Comment> comments = ListExt.asList();

  public Annotation(MethodCall call) {
    this.call = call;
  }
}
