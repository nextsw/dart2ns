package classes;

import d3e.core.ListExt;
import java.util.List;

public class Export {
  public String path;
  public Library lib;
  public List<String> show = ListExt.asList();
  public List<String> hide = ListExt.asList();

  public Export(Library lib, String path) {
    this.lib = lib;
    this.path = path;
  }
}
