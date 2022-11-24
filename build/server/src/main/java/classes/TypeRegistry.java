package classes;

public interface TypeRegistry {
  public PropType getType(String name);

  public void addType(PropType type);

  public TopDecl get(String name);
}
