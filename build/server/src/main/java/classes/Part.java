package classes;

public class Part {
  public String path;
  public Library parent;

  public Part(Library parent, String path) {
    this.parent = parent;
    this.path = path;
  }
}
