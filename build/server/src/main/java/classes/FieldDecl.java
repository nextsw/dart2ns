package classes;

import d3e.core.ListExt;
import java.util.List;

public class FieldDecl extends ClassMember {
  public String name;
  public DataType type;
  public Expression value;
  public boolean staticValue = false;
  public boolean finalValue = false;
  public boolean constValue = false;
  public List<Comment> comments = ListExt.asList();

  public FieldDecl(
      List<Comment> comments,
      boolean constValue,
      boolean finalValue,
      String name,
      boolean staticValue,
      DataType type,
      Expression value) {
    this.comments = comments;
    this.constValue = constValue;
    this.finalValue = finalValue;
    this.name = name;
    this.staticValue = staticValue;
    this.type = type;
    this.value = value;
  }
}
