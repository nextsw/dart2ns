package classes;

import d3e.core.IntegerExt;
import d3e.core.ListExt;
import java.util.List;

public class Simplifier {
  public List<SimplifierResult> stack = ListExt.asList();
  public long tempCount = 0l;

  public Simplifier() {}

  public Expression makeSimple(Expression exp) {
    if (exp == null) {
      return exp;
    }
    if (exp instanceof FieldOrEnumExpression) {
      FieldOrEnumExpression fe = ((FieldOrEnumExpression) exp);
      if (fe.on == null) {
        return exp;
      } else {
        fe.on = makeSimple(fe.on);
        String name = makeTempName();
        Declaration d =
            new Declaration(false, false, false, ListExt.List(), new ValueType("var", false));
        d.names.add(new NameAndValue(name, fe));
        ListExt.last(this.stack).list.add(d);
        return new FieldOrEnumExpression(false, name, false, null);
      }
    } else {
      exp.simplify(this);
    }
    return exp;
  }

  public void push() {
    this.stack.add(new SimplifierResult());
  }

  public SimplifierResult pop() {
    return ListExt.removeLast(this.stack);
  }

  public String makeTempName() {
    this.tempCount++;
    return "__t" + IntegerExt.toString(this.tempCount);
  }

  public void add(Statement s) {
    ListExt.last(this.stack).list.add(s);
  }

  public void markDelete() {
    ListExt.last(this.stack).deleted = true;
  }

  public void reset() {
    this.tempCount = 0l;
  }
}
