package classes;

import java.util.List;
import java.util.Set;

public class MapItem extends ArrayItem {
  public Expression key;
  public Expression value;

  public MapItem(List<Comment> comments, Expression key, Expression value) {
    this.key = key;
    this.value = value;
    this.comments = comments;
  }

  public void resolve(ResolveContext context) {
    this.key.resolve(context);
    this.value.resolve(context);
  }

  public void collectUsedTypes(Set<String> types) {
    this.key.collectUsedTypes(types);
    this.value.collectUsedTypes(types);
  }
}
