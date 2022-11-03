package classes;

import d3e.core.ListExt;
import java.util.List;

public class Enum extends TopDecl {
  public List<String> values = ListExt.asList();

  public Enum(String name) {
    super(name, TopDeclType.Enum, "");
  }
}
