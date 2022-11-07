package classes;

public class Part {
  public String path;
  public Library parent;
  public Library lib;

  public Part(Library parent, String path, Library lib) {
    this.parent = parent;
    this.path = path;
    this.lib = lib;
  }
}
