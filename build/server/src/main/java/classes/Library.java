package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Library {
  public String fullPath;
  public String packagePath;
  public String id;
  public Library parent;
  public List<Import> imports = ListExt.asList();
  public List<Export> exports = ListExt.asList();
  public List<TopDecl> objects = ListExt.asList();
  public List<Part> parts = ListExt.asList();
  public String partOf;

  public Library(String fullPath, String packagePath, Library parent) {
    this.fullPath = fullPath;
    this.packagePath = packagePath;
    this.parent = parent;
  }

  public TopDecl get(String name) {
    return ListExt.firstWhere(
        this.objects,
        (o) -> {
          return Objects.equals(o.name, name);
        },
        null);
  }

  public void subs(List<TopDecl> libs, Set<Library> collected) {
    if (collected.contains(this)) {
      return;
    }
    collected.add(this);
    for (Part p : this.parts) {
      p.lib.subs(libs, collected);
    }
    for (Export p : this.exports) {
      List<TopDecl> exported = ListExt.List(0l);
      p.lib.subs(exported, collected);
      if (ListExt.isNotEmpty(p.hide)) {
        for (TopDecl top : exported) {
          if (p.hide.contains(top.name)) {
            continue;
          }
          libs.add(top);
        }
      } else if (ListExt.isNotEmpty(p.show)) {
        for (TopDecl top : exported) {
          if (p.show.contains(top.name)) {
            libs.add(top);
          }
        }
      } else {
        ListExt.addAll(libs, exported);
      }
    }
    ListExt.addAll(libs, this.objects);
  }
}
