package classes;

import d3e.core.ListExt;
import java.util.List;
import java.util.function.Function;

public class LambdaCollector implements ExpressionVisitor {
  public List<Expression> list = ListExt.asList();
  public Function<Expression, Boolean> predicate;

  public LambdaCollector(Function<Expression, Boolean> predicate) {
    this.predicate = predicate;
  }

  public static List<Expression> forLibrary(Library lib) {
    LambdaCollector collector =
        new LambdaCollector(
            (exp) -> {
              return exp instanceof LambdaExpression;
            });
    lib.objects.forEach(
        (obj) -> {
          if (obj instanceof ClassDecl) {
          } else {
            obj.visit(collector);
          }
        });
    return collector.list;
  }

  public static List<Expression> forClass(ClassDecl cls) {
    LambdaCollector collector =
        new LambdaCollector(
            (exp) -> {
              return exp instanceof LambdaExpression;
            });
    cls.visit(collector);
    return collector.list;
  }

  public void visit(Expression exp) {
    if (exp == null) {
      return;
    }
    boolean res = predicate.apply(exp);
    if (res) {
      this.list.add(exp);
    }
    exp.visit(this);
  }

  public static List<FieldOrEnumExpression> getFieldsIn(Expression expression) {
    return ListExt.asList();
  }
}
