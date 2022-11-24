package classes;

public class LocalVar {
  public String name;
  public PropType type;
  public boolean initialized = false;
  public boolean isFinal = false;
  public Object value;

  public LocalVar() {}

  public void markNotFinal() {}

  public void markInitialized() {}

  public void markNotInitialized() {}
}
