package classes;

import d3e.core.ListExt;
import java.util.List;

public class Export {
  public String path;
  public Library parent;
  public Library lib;
  public List<String> show = ListExt.asList();
  public List<String> hide = ListExt.asList();

  public Export(Library parent, String path, Library lib) {
    this.parent = parent;
    this.path = path;
    this.lib = lib;
  }
}
