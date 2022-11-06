package classes;

import d3e.core.ListExt;
import java.util.List;

public class Import {
  public String path;
  public Library lib;
  public List<String> show = ListExt.asList();
  public List<String> hide = ListExt.asList();
  public boolean differed = false;
  public String name;

  public Import(Library lib, String path) {
    this.lib = lib;
    this.path = path;
  }
}
