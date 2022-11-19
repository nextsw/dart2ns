package classes;

import java.util.Set;

public abstract class DataType {
  public String name;
  public boolean optional = false;

  public abstract void collectUsedTypes(Set<String> types);

  public String toString() {
    return this.name;
  }
}
