package classes;

import java.util.List;
import java.util.Set;

public abstract class DataType {
  public String name;
  public boolean optional = false;
  public PropType resolvedType;

  public abstract void collectUsedTypes(Set<String> types);

  public String toString() {
    return this.name;
  }

  public abstract PropType type(ValidationContext ctx);

  public abstract PropType rawType(ValidationContext ctx);

  public abstract List<DataType> getTypeArguments();
}
