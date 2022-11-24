package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Objects;

public class FieldDecl extends ClassMember {
  public String name;
  public DataType type;
  public Expression value;
  public boolean finalValue = false;
  public boolean constValue = false;
  public boolean external = false;
  public List<Comment> comments = ListExt.asList();
  public List<Annotation> annotations = ListExt.asList();

  public FieldDecl(
      List<Annotation> annotations,
      List<Comment> comments,
      boolean constValue,
      boolean external,
      boolean finalValue,
      String name,
      boolean staticValue,
      DataType type,
      Expression value) {
    super(name, TopDeclType.Field, "");
    this.annotations = annotations;
    this.comments = comments;
    this.constValue = constValue;
    this.external = external;
    this.finalValue = finalValue;
    this.name = name;
    this.staticValue = staticValue;
    this.type = type;
    this.value = value;
  }

  public void collectUsedTypes() {
    if (this.type != null) {
      this.usedTypes.add(this.type);
    }
  }

  public void resolve(ResolveContext context) {
    if (this.value != null) {
      this.value.resolve(context);
      if (this.type == null || Objects.equals(this.type.name, "var")) {
        this.type = this.value.resolvedType;
      }
    }
    if (context.scope != null) {
      context.scope.add(this.name, this.type);
    }
  }

  public String toString() {
    return this.name;
  }

  public void simplify(Simplifier s) {}

  public void visit(ExpressionVisitor visitor) {
    visitor.visit(this.value);
  }

  public void validate(ValidationContext ctx, long phase) {
    if (phase == 0l) {
      ctx.validateType(this.type);
    } else {
      ctx.validateExpression(this.value);
      if ((this.type == null || Objects.equals(this.type.name, "var")) && this.value != null) {
        this.type = this.value.expType.toDataType();
      }
    }
  }

  public void register(ValidationContext ctx) {}
}
