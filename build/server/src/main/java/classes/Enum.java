package classes;

import java.util.List;

public class Enum extends TopDecl {
  public List<String> values;

  public Enum(String name) {
    super(name, TopDeclType.Enum, null);
  }
}
