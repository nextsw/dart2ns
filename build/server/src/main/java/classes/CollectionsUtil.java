package classes;

import d3e.core.IteratorExt;
import d3e.core.ListExt;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class CollectionsUtil {
  public CollectionsUtil() {}

  public static <T, U> void forEach(
      Iterable<T> first, Iterable<U> next, BiConsumer<T, U> consumer) {
    Iterator<T> fi = first.iterator();
    Iterator<U> ni = next.iterator();
    while (IteratorExt.moveNext(fi) && IteratorExt.moveNext(ni)) {
      consumer.accept(IteratorExt.getCurrent(fi), IteratorExt.getCurrent(ni));
    }
  }

  public static <T> List<T> sort(List<T> from, BiFunction<T, T, Long> comparator) {
    List<T> list = ListExt.from(from, false);
    ListExt.sort(list, comparator);
    return list;
  }
}
