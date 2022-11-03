package classes;

import java.util.List;

public class MapItem extends ArrayItem {
	private Expression key;
	private Expression value;

	public MapItem(Expression key, Expression value, List<Comment> comments) {
		this.key = key;
		this.value = value;
		this.comments = comments;

	}
}
