package classes;

import java.util.List;

import d3e.core.ListExt;

public class Enum extends TopDecl {
  public List<String> values = ListExt.asList();

  public Enum(String name) {
    super(name, TopDeclType.Enum, "");
  }
}
