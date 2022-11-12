package classes;

import d3e.core.ListExt;
import java.util.List;

public class Import {
  public String path;
  public Library lib;
  public Library parent;
  public List<String> show = ListExt.asList();
  public List<String> hide = ListExt.asList();
  public boolean differed = false;
  public String name;
  public String conditioned;

  public Import(Library parent, String path, Library lib) {
    this.parent = parent;
    this.path = path;
    this.lib = lib;
  }
}
