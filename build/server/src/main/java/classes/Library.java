package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Objects;

public class Library {
  public String base;
  public String path;
  public String fullPath;
  public String packagePath;
  public String id;
  public Library parent;
  public List<Import> imports = ListExt.asList();
  public List<Export> exports = ListExt.asList();
  public List<TopDecl> objects = ListExt.asList();
  public List<Part> parts = ListExt.asList();

  public Library(String base, String fullPath, String packagePath, Library parent, String path) {
    this.base = base;
    this.fullPath = fullPath;
    this.packagePath = packagePath;
    this.parent = parent;
    this.path = path;
  }

  public TopDecl get(String name) {
    return ListExt.firstWhere(
        this.objects,
        (o) -> {
          return Objects.equals(o.name, name);
        },
        null);
  }
}
