package classes;

import d3e.core.MapExt;
import java.util.Map;

public class Scope {
  public Scope parent;
  public Map<String, DataType> variables = MapExt.Map();
  public Map<String, String> casts = MapExt.Map();
  public ClassDecl thisType;

  public Scope(Scope parent, ClassDecl thisType) {
    this.parent = parent;
    this.thisType = thisType;
  }

  public void add(String name, DataType type) {
    MapExt.set(this.variables, name, type);
  }
}
