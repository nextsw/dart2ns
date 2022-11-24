package classes;

import d3e.core.ListExt;
import java.util.List;

public class TypeResolutionPosition {
  public TypeResolutionPositionType type;
  public long index = 0l;
  public String name;
  public String genVar;
  public List<TypeResolutionPosition> gens = ListExt.asList();

  public TypeResolutionPosition(TypeResolutionPositionType type) {
    this.type = type;
  }
}
