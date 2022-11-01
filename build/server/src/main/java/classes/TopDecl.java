package classes;

import java.util.List;

public abstract class TopDecl {
  public TopDeclType type;
  public String name;
  public String path;
  public List<GenError> errors;
  public List<Annotation> annotations;

  public TopDecl(String name, TopDeclType type, String path) {
    this.name = name;
    this.type = type;
    this.path = path;
  }
}
