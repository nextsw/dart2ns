package classes;

public class StatementUtil {
  public StatementUtil() {}

  public static boolean needSemicolon(Statement on) {
    if (on instanceof Assignment) {
      return true;
    } else if (on instanceof Block) {
      return false;
    } else if (on instanceof Break) {
      return true;
    } else if (on instanceof Continue) {
      return true;
    } else if (on instanceof Declaration) {
      return true;
    } else if (on instanceof DoWhileLoop) {
      return true;
    } else if (on instanceof ForLoop) {
      return false;
    } else if (on instanceof IfStatement) {
      return false;
    } else if (on instanceof MethodCall) {
      return true;
    } else if (on instanceof PostfixExpression) {
      return true;
    } else if (on instanceof PrefixExpression) {
      return true;
    } else if (on instanceof Return) {
      return true;
    } else if (on instanceof SwitchStatement) {
      return false;
    } else if (on instanceof ThrowStatement) {
      return true;
    } else if (on instanceof TryCatcheStatment) {
      return false;
    } else if (on instanceof WhileLoop) {
      return false;
    }
    return false;
  }
}
