package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Declaration extends Statement {
  public DataType type;
  public List<NameAndValue> names = ListExt.asList();
  public boolean isFinal = false;
  public boolean isConst = false;
  public boolean isLate = false;

  public Declaration(
      boolean isConst, boolean isFinal, boolean isLate, List<NameAndValue> names, DataType type) {
    this.isConst = isConst;
    this.isFinal = isFinal;
    this.isLate = isLate;
    this.names = names;
    this.type = type;
  }

  public void collectUsedTypes(Set<String> types) {
    this.names.forEach(
        (n) -> {
          if (n.value != null) {
            n.value.collectUsedTypes(types);
          }
        });
  }

  public void resolve(ResolveContext context) {
    this.resolvedType = null;
    if (this.type != null) {
      this.resolvedType = this.type;
    }
    boolean haveType = this.type != null && !(Objects.equals(this.type.name, "var"));
    this.names.forEach(
        (n) -> {
          if (n.value != null) {
            n.value.resolve(context);
            this.resolvedType = n.value.resolvedType;
          }
          context.scope.add(n.name, haveType ? this.type : this.resolvedType);
        });
    if (!haveType) {
      DataType value$ = this.resolvedType;
      if (value$ == null) {
        value$ = context.ofUnknownType();
      }
      this.resolvedType = value$;
      this.type = this.resolvedType;
    } else {
      this.resolvedType = this.type;
    }
  }
}
