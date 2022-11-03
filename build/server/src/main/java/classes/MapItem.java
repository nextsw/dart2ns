package classes;

import java.util.List;

public class MapItem extends ArrayItem {
  public Expression key;
  public Expression value;

  public MapItem(List<Comment> comments, Expression key, Expression value) {
    this.key = key;
    this.value = value;
    this.comments = comments;
  }
}
