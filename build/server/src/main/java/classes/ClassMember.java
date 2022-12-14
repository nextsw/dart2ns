package classes;

import d3e.core.ListExt;
import java.util.List;

public abstract class ClassMember extends TopDecl {
  public List<Comment> comments = ListExt.asList();
  public boolean staticValue = false;
  public ClassDecl cls;

  public ClassMember(String name, TopDeclType type, String path) {
    super(name, type, path);
  }
}
