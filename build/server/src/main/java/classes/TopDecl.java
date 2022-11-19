package classes;

import d3e.core.SetExt;
import java.util.List;
import java.util.Set;

public abstract class TopDecl {
  public TopDeclType type;
  public String name;
  public String path;
  public List<GenError> errors;
  public List<Annotation> annotations;
  public Library lib;
  public Set<String> usedTypes = SetExt.Set();

  public TopDecl(String name, TopDeclType type, String path) {
    this.name = name;
    this.type = type;
    this.path = path;
  }

  public abstract void collectUsedTypes();

  public abstract void resolve(ResolveContext context);
}
