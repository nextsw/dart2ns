package classes;

import d3e.core.ListExt;
import java.util.List;

public class Typedef extends TopDecl {
  public DefType type;
  public FunctionType fnType;
  public List<Annotation> annotations = ListExt.asList();

  public Typedef(String name, DefType type, FunctionType fnType) {
    super(name, TopDeclType.Typedef, "");
    this.name = name;
    this.type = type;
    this.fnType = fnType;
  }

  public void collectUsedTypes() {
    if (this.type != null) {
      this.usedTypes.add(this.type);
    }
    this.usedTypes.add(this.fnType);
  }

  public void resolve(ResolveContext context) {}

  public void simplify(Simplifier s) {}

  public void visit(ExpressionVisitor visitor) {}

  public void validate(ValidationContext ctx, long phase) {}

  public void register(ValidationContext ctx) {}
}
