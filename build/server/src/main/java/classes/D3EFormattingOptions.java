package classes;

import d3e.core.ListExt;
import d3e.core.StringBuilderExt;
import java.util.List;
import java.util.function.Consumer;

public class D3EFormattingOptions {
  private FormattingOptions _options;
  private long _depth = 0l;
  private String _tab;

  public D3EFormattingOptions(FormattingOptions _options, String _tab, long _depth) {
    this._options = _options;
    this._tab = _tab;
    this._depth = _depth;
  }

  public static D3EFormattingOptions withDefault() {
    return new D3EFormattingOptions(null, "    ", 0l);
  }

  public static D3EFormattingOptions withOptions(FormattingOptions options) {
    long tabSize = options.getTabSize();
    String tab = "";
    for (long x = 0l; x < tabSize; x++) {
      tab += " ";
    }
    return new D3EFormattingOptions(options, tab, 0l);
  }

  public D3EFormattingOptions inc() {
    return new D3EFormattingOptions(this._options, this._tab, this._depth + 1l);
  }

  public D3EFormattingOptions dec() {
    return new D3EFormattingOptions(this._options, this._tab, this._depth - 1l);
  }

  public void appendDepth(StringBuilder sb) {
    for (long x = 0l; x < this._depth; x++) {
      appendTab(sb);
    }
  }

  public void appendTab(StringBuilder sb) {
    StringBuilderExt.write(sb, this._tab);
  }

  public <T> void appendCollection(StringBuilder sb, List<T> coll, Consumer<T> each) {
    appendCollectionWithSep(
        sb,
        coll,
        () -> {
          StringBuilderExt.write(sb, ", ");
        },
        each);
  }

  public <T> void appendCollectionWithSep(
      StringBuilder sb, List<T> coll, Runnable sep, Consumer<T> each) {
    if (!coll.isEmpty()) {
      each.accept(ListExt.first(coll));
      for (long i = 1l; i < ListExt.length(coll); i++) {
        sep.run();
        each.accept(ListExt.get(coll, i));
      }
    }
  }
}
