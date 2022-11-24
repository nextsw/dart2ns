package classes;

import d3e.core.ListExt;
import java.util.List;

public abstract class TopDecl {
  public TopDeclType type;
  public String name;
  public String path;
  public List<GenError> errors;
  public List<Annotation> annotations;
  public Library lib;
  private String _packagePath;
  public List<DataType> usedTypes = ListExt.List(0l);

  public TopDecl(String name, TopDeclType type, String path) {
    this.name = name;
    this.type = type;
    this.path = path;
  }

  public abstract void collectUsedTypes();

  public String getPackagePath() {
    return this.lib.packagePath + "lib";
  }

  public abstract void simplify(Simplifier s);

  public abstract void validate(ValidationContext ctx, long phase);

  public abstract void register(ValidationContext ctx);

  public abstract void visit(ExpressionVisitor visitor);
}
