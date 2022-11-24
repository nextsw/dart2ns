package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Objects;

public class TypeVariable extends PropType {
  public List<TypeResolutionPosition> positions = ListExt.asList();

  public TypeVariable(String name) {
    super(name);
  }

  public boolean canTypeSubstitute(PropType type) {
    if (type instanceof TypeVariable) {
      TypeVariable tv = (((TypeVariable) type));
      if (tv.extendsValue == null) {
        return this.extendsValue == null;
      }
      return canTypeSubstitute(tv.extendsValue);
    }
    if (this.extendsValue == null) {
      return true;
    }
    if (this.extendsValue.isAssignableFrom(type)) {
      return true;
    }
    return this.extendsValue.isAssignableFrom(type);
  }

  public boolean isAssignableFrom(PropType type) {
    if (Objects.equals(type, PropType.VOID)) {
      return Objects.equals(this, PropType.VOID);
    }
    if (this.extendsValue == null) {
      return true;
    }
    if (isAssignableFromInternal(type)) {
      return true;
    }
    return this.extendsValue.isAssignableFrom(type);
  }

  public List<MethodDecl> getAllMethods(ValidationContext ctx) {
    if (this.extendsValue != null) {
      return this.extendsValue.getAllMethods(ctx);
    } else {
      PropType objType = ctx.object();
      if (!(Objects.equals(this, objType))) {
        return objType.getAllMethods(ctx);
      }
    }
    return ListExt.List(0l);
  }

  public List<FieldDecl> getAllFields() {
    if (this.extendsValue != null) {
      return this.extendsValue.getAllFields();
    }
    return ListExt.List(0l);
  }

  public boolean isDependsOnReturn() {
    if (this.positions == null || this.positions.isEmpty()) {
      return false;
    }
    return ListExt.first(this.positions).type == TypeResolutionPositionType.RETURN;
  }
}
