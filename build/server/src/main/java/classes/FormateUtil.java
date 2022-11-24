package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.StringBuilderExt;
import java.util.List;

public class FormateUtil {
  public FormateUtil() {}

  public static String toStringExpression(Expression on) {
    StringBuilder sb = StringBuilderExt.StringBuffer("");
    D3EFormattingOptions options = D3EFormattingOptions.withDefault();
    if (on instanceof Block) {
      FormateUtil.formateBlock(((Block) on), sb, options.dec(), false);
    } else {
      FormateUtil.formate(on, sb, options);
    }
    return sb.toString();
  }

  public static String toStringNamedArgument(NamedArgument on) {
    StringBuilder sb = StringBuilderExt.StringBuffer("");
    FormateUtil.formateNamedArgument(on, sb, D3EFormattingOptions.withDefault());
    return sb.toString();
  }

  public static void formate(Expression on, StringBuilder sb, D3EFormattingOptions options) {
    if (on instanceof ArrayAccess) {
      FormateUtil.formateArrayAccess(((ArrayAccess) on), sb, options);
    } else if (on instanceof ArrayExpression) {
      FormateUtil.formateArrayExpression(((ArrayExpression) on), sb, options);
    } else if (on instanceof BinaryExpression) {
      FormateUtil.formateBinaryExpression(((BinaryExpression) on), sb, options);
    } else if (on instanceof FieldOrEnumExpression) {
      FieldOrEnumExpression exp = ((FieldOrEnumExpression) on);
      FormateUtil.formateFieldOrEnumExpression(exp, sb, options);
    } else if (on instanceof LambdaExpression) {
      FormateUtil.formateLambdaExpression(((LambdaExpression) on), sb, options);
    } else if (on instanceof LiteralExpression) {
      FormateUtil.formateLiteralExpression(((LiteralExpression) on), sb, options);
    } else if (on instanceof NullExpression) {
      FormateUtil.formateNullExpression(((NullExpression) on), sb, options);
    } else if (on instanceof ParExpression) {
      FormateUtil.formateParExpression(((ParExpression) on), sb, options);
    } else if (on instanceof SwitchExpression) {
      FormateUtil.formateSwitchExpression(((SwitchExpression) on), sb, options);
    } else if (on instanceof TerinaryExpression) {
      FormateUtil.formateTerinaryExpression(((TerinaryExpression) on), sb, options);
    } else if (on instanceof TypeCastOrCheckExpression) {
      FormateUtil.formateTypeCastOrCheckExpression(((TypeCastOrCheckExpression) on), sb, options);
    } else if (on instanceof CollectionIf) {
      FormateUtil.formateCollectionIf(((CollectionIf) on), sb, options);
    } else if (on instanceof CollectionFor) {
      FormateUtil.formateCollectionFor(((CollectionFor) on), sb, options);
    } else if (on instanceof CollectionSpread) {
      FormateUtil.formateCollectionSpread(((CollectionSpread) on), sb, options);
    } else if (on instanceof ExpressionArrayItem) {
      FormateUtil.formateExpressionArrayItem(((ExpressionArrayItem) on), sb, options);
    } else if (on instanceof Assignment) {
      FormateUtil.formateAssignment(((Assignment) on), sb, options);
    } else if (on instanceof Block) {
      FormateUtil.formateBlock(((Block) on), sb, options, true);
    } else if (on instanceof Break) {
      FormateUtil.formateBreak(((Break) on), sb, options);
    } else if (on instanceof Continue) {
      FormateUtil.formateContinue(((Continue) on), sb, options);
    } else if (on instanceof Declaration) {
      FormateUtil.formateDeclaration(((Declaration) on), sb, options);
    } else if (on instanceof DoWhileLoop) {
      FormateUtil.formateDoWhileLoop(((DoWhileLoop) on), sb, options);
    } else if (on instanceof ForLoop) {
      FormateUtil.formateForLoop(((ForLoop) on), sb, options);
    } else if (on instanceof ForEachLoop) {
      FormateUtil.formateForEachLoop(((ForEachLoop) on), sb, options);
    } else if (on instanceof IfStatement) {
      FormateUtil.formateIfStatement(((IfStatement) on), sb, options);
    } else if (on instanceof MethodCall) {
      FormateUtil.formateMethodCall(((MethodCall) on), sb, options);
    } else if (on instanceof PostfixExpression) {
      FormateUtil.formatePostfixExpression(((PostfixExpression) on), sb, options);
    } else if (on instanceof PrefixExpression) {
      FormateUtil.formatePrefixExpression(((PrefixExpression) on), sb, options);
    } else if (on instanceof Return) {
      FormateUtil.formateReturn(((Return) on), sb, options);
    } else if (on instanceof SwitchStatement) {
      FormateUtil.formateSwitchStatement(((SwitchStatement) on), sb, options);
    } else if (on instanceof ThrowStatement) {
      FormateUtil.formateThrowStatement(((ThrowStatement) on), sb, options);
    } else if (on instanceof TryCatcheStatment) {
      FormateUtil.formateTryCatcheStatment(((TryCatcheStatment) on), sb, options);
    } else if (on instanceof WhileLoop) {
      FormateUtil.formateWhileLoop(((WhileLoop) on), sb, options);
    }
  }

  public static void formateArrayAccess(
      ArrayAccess a, StringBuilder sb, D3EFormattingOptions options) {
    if (a.on != null) {
      FormateUtil.formate(a.on, sb, options);
    }
    StringBuilderExt.write(sb, "[");
    if (a.index != null) {
      FormateUtil.formate(a.index, sb, options);
    }
    StringBuilderExt.write(sb, "]");
  }

  public static void formateArrayExpression(
      ArrayExpression on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.enforceType != null) {
      StringBuilderExt.write(sb, "<");
      FormateUtil.formateDataType(on.enforceType, sb, options);
      StringBuilderExt.write(sb, ">");
    }
    if (on.values.isEmpty()) {
      StringBuilderExt.write(sb, on.getList() ? "[" : "{");
      StringBuilderExt.write(sb, on.getList() ? "]" : "}");
      return;
    }
    StringBuilderExt.write(sb, on.getList() ? "[" : "{");
    StringBuilderExt.write(sb, "\n");
    D3EFormattingOptions inc = options.inc();
    inc.appendDepth(sb);
    if (ListExt.isNotEmpty(on.values)) {
      FormateUtil.formate(ListExt.first(on.values), sb, inc);
      for (long i = 1l; i < ListExt.length(on.values); i++) {
        StringBuilderExt.write(sb, ",\n");
        inc.appendDepth(sb);
        FormateUtil.formate(ListExt.get(on.values, i), sb, inc);
      }
    }
    StringBuilderExt.write(sb, ",");
    StringBuilderExt.write(sb, "\n");
    options.appendDepth(sb);
    StringBuilderExt.write(sb, on.getList() ? "]" : "}");
  }

  public static void formateBinaryExpression(
      BinaryExpression on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.left != null) {
      FormateUtil.formate(on.left, sb, options);
    }
    StringBuilderExt.write(sb, " ");
    StringBuilderExt.write(sb, on.op);
    StringBuilderExt.write(sb, " ");
    if (on.right != null) {
      FormateUtil.formate(on.right, sb, options);
    }
  }

  public static void formateFieldOrEnumExpression(
      FieldOrEnumExpression fe, StringBuilder sb, D3EFormattingOptions options) {
    if (fe.on != null) {
      FormateUtil.formate(fe.on, sb, options);
      StringBuilderExt.write(sb, ".");
    }
    if (fe.name != null) {
      StringBuilderExt.write(sb, fe.name);
    }
  }

  public static void formateLambdaExpression(
      LambdaExpression on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "(");
    /*
     sb.write(on.parameters.map((i) => i.name.value).join(', '));
    */
    StringBuilderExt.write(sb, ")");
    if (on.expression != null) {
      StringBuilderExt.write(sb, " => ");
      FormateUtil.formate(on.expression, sb, options);
    } else if (on.body != null) {
      StringBuilderExt.write(sb, " ");
      FormateUtil.formate(on.body, sb, options);
    }
  }

  public static void formateLiteralExpression(
      LiteralExpression on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.value == null) {
      return;
    }
    if (on.type == LiteralType.TypeString) {
      if (on.isRawString) {
        StringBuilderExt.write(sb, "r");
      }
      StringBuilderExt.write(sb, "'");
      if (on.isRawString) {
        StringBuilderExt.write(sb, on.value);
      } else {
        StringBuilderExt.write(sb, on.value);
      }
      StringBuilderExt.write(sb, "'");
    } else {
      StringBuilderExt.write(sb, on.value);
    }
  }

  public static void formateNullExpression(
      NullExpression on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "null");
  }

  public static void formateParExpression(
      ParExpression on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "(");
    if (on.exp != null) {
      FormateUtil.formate(on.exp, sb, options);
    }
    StringBuilderExt.write(sb, ")");
  }

  public static void formateSwitchExpression(
      SwitchExpression se, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "switch (");
    if (se.on != null) {
      FormateUtil.formate(se.on, sb, options);
    }
    StringBuilderExt.write(sb, ")");
    StringBuilderExt.write(sb, "{\n");
    D3EFormattingOptions inc = options.inc();
    se.cases.forEach(
        (c) -> {
          FormateUtil.formateCaseExpression(c, sb, inc);
        });
    if (se.onElse != null) {
      inc.appendDepth(sb);
      StringBuilderExt.write(sb, "default: ");
      FormateUtil.formate(se.onElse, sb, options);
      StringBuilderExt.write(sb, "\n");
    }
    options.appendDepth(sb);
    StringBuilderExt.write(sb, "}");
  }

  public static void formateCaseExpression(
      CaseExpression on, StringBuilder sb, D3EFormattingOptions options) {
    on.tests.forEach(
        (t) -> {
          options.appendDepth(sb);
          StringBuilderExt.write(sb, "case ");
          FormateUtil.formate(t, sb, options);
          StringBuilderExt.write(sb, ":");
        });
    StringBuilderExt.write(sb, " ");
    if (on.result != null) {
      FormateUtil.formate(on.result, sb, options);
    }
    StringBuilderExt.write(sb, "\n");
  }

  public static void formateTerinaryExpression(
      TerinaryExpression on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.condition != null) {
      FormateUtil.formate(on.condition, sb, options);
    }
    StringBuilderExt.write(sb, " ? ");
    if (on.ifTrue != null) {
      FormateUtil.formate(on.ifTrue, sb, options);
    }
    StringBuilderExt.write(sb, " : ");
    if (on.ifFalse != null) {
      FormateUtil.formate(on.ifFalse, sb, options);
    }
  }

  public static void formateTypeCastOrCheckExpression(
      TypeCastOrCheckExpression on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.exp != null) {
      FormateUtil.formate(on.exp, sb, options);
    }
    StringBuilderExt.write(sb, on.check ? " is " : " as ");
    if (on.dataType != null) {
      FormateUtil.formateDataType(on.dataType, sb, options);
    }
  }

  public static void formateCollectionIf(
      CollectionIf on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "if (");
    if (on.test != null) {
      FormateUtil.formate(on.test, sb, options);
    }
    StringBuilderExt.write(sb, ") ");
    StringBuilderExt.write(sb, "\n");
    if (on.thenItem != null) {
      D3EFormattingOptions inc = options.inc();
      inc.appendDepth(sb);
      FormateUtil.formate(on.thenItem, sb, inc);
    }
    if (on.elseItem != null) {
      StringBuilderExt.write(sb, "\n");
      options.appendDepth(sb);
      StringBuilderExt.write(sb, "else");
      StringBuilderExt.write(sb, "\n");
      D3EFormattingOptions inc = options.inc();
      inc.appendDepth(sb);
      FormateUtil.formate(on.elseItem, sb, inc);
    }
  }

  public static void formateCollectionFor(
      CollectionFor on, StringBuilder sb, D3EFormattingOptions options) {
    FormateUtil.formateForEachLoop(((ForEachLoop) on.stmt), sb, options);
    FormateUtil.formateForLoop(((ForLoop) on.stmt), sb, options);
    StringBuilderExt.write(sb, "\n");
    if (on.value != null) {
      D3EFormattingOptions inc = options.inc();
      inc.appendDepth(sb);
      FormateUtil.formate(on.value, sb, inc);
    }
  }

  public static void formateCollectionSpread(
      CollectionSpread on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "...");
    if (on.checkNull) {
      StringBuilderExt.write(sb, "?");
    }
    if (on.values != null) {
      FormateUtil.formate(on.values, sb, options);
    }
  }

  public static void formateExpressionArrayItem(
      ExpressionArrayItem on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.exp != null) {
      FormateUtil.formate(on.exp, sb, options);
    }
  }

  public static void formateAssignment(
      Assignment on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.left != null) {
      FormateUtil.formate(on.left, sb, options);
    }
    StringBuilderExt.write(sb, " ");
    StringBuilderExt.write(sb, on.op);
    StringBuilderExt.write(sb, " ");
    if (on.right != null) {
      FormateUtil.formate(on.right, sb, options);
    }
  }

  public static void formateBlock(
      Block on, StringBuilder sb, D3EFormattingOptions options, boolean needBraces) {
    if (needBraces) {
      StringBuilderExt.write(sb, "{\n");
    }
    D3EFormattingOptions inc = options.inc();
    on.statements.forEach(
        (s) -> {
          if (s == null) {
            return;
          }
          inc.appendDepth(sb);
          FormateUtil.formate(s, sb, inc);
          if (StatementUtil.needSemicolon(s)) {
            StringBuilderExt.write(sb, ";");
          }
          StringBuilderExt.write(sb, "\n");
        });
    if (on.afterComments != null) {
      inc.appendDepth(sb);
    }
    options.appendDepth(sb);
    if (needBraces) {
      StringBuilderExt.write(sb, "}");
    }
  }

  public static void formateBreak(Break on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "break");
  }

  public static void formateContinue(Continue on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "continue");
  }

  public static void formateDeclaration(
      Declaration on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.type != null) {
      FormateUtil.formateDataType(on.type, sb, options);
    }
    StringBuilderExt.write(sb, " ");
    for (NameAndValue nv : on.names) {
      StringBuilderExt.write(sb, nv.name);
      if (nv.value != null) {
        StringBuilderExt.write(sb, " = ");
        FormateUtil.formate(nv.value, sb, options);
      }
    }
  }

  public static void formateDoWhileLoop(
      DoWhileLoop on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "do ");
    if (on.body != null) {
      FormateUtil.formate(on.body, sb, options);
    }
    StringBuilderExt.write(sb, " while ");
    StringBuilderExt.write(sb, "(");
    if (on.test != null) {
      FormateUtil.formate(on.test, sb, options);
    }
    StringBuilderExt.write(sb, ")");
  }

  public static void formateForLoop(ForLoop on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "for (");
    if (on.decl != null) {
      FormateUtil.formate(on.decl, sb, options);
      if (!on.inits.isEmpty()) {
        StringBuilderExt.write(sb, ", ");
      }
    }
    options.appendCollection(
        sb,
        on.inits,
        (i) -> {
          FormateUtil.formate(i, sb, options);
        });
    StringBuilderExt.write(sb, "; ");
    if (on.test != null) {
      FormateUtil.formate(on.test, sb, options);
    }
    StringBuilderExt.write(sb, "; ");
    options.appendCollection(
        sb,
        on.resets,
        (i) -> {
          FormateUtil.formate(i, sb, options);
        });
    StringBuilderExt.write(sb, ")");
    if (on.body != null) {
      StringBuilderExt.write(sb, " ");
      FormateUtil.formate(on.body, sb, options);
    }
  }

  public static void formateForEachLoop(
      ForEachLoop on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "for ");
    StringBuilderExt.write(sb, "(");
    if (on.dataType != null) {
      FormateUtil.formateDataType(on.dataType, sb, options);
    }
    StringBuilderExt.write(sb, " ");
    if (on.name != null) {
      StringBuilderExt.write(sb, on.name);
    }
    StringBuilderExt.write(sb, " in ");
    if (on.collection != null) {
      FormateUtil.formate(on.collection, sb, options);
    }
    StringBuilderExt.write(sb, ")");
  }

  public static void formateIfStatement(
      IfStatement on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "if (");
    if (on.test != null) {
      FormateUtil.formate(on.test, sb, options);
    }
    StringBuilderExt.write(sb, ")");
    if (on.thenStatement != null) {
      StringBuilderExt.write(sb, " ");
      FormateUtil.formate(on.thenStatement, sb, options);
    }
    if (on.elseStatement != null) {
      StringBuilderExt.write(sb, " else ");
      FormateUtil.formate(on.elseStatement, sb, options);
    }
  }

  public static void formateMethodCall(
      MethodCall mc, StringBuilder sb, D3EFormattingOptions options) {
    if (mc.on != null) {
      FormateUtil.formate(mc.on, sb, options);
      StringBuilderExt.write(sb, ".");
    }
    if (mc.name != null) {
      StringBuilderExt.write(sb, mc.name);
    }
    /*
     if (mc.typeArgs != null) {
         formateTypeArguments(mc.typeArgs, sb, options);
     }
    */
    StringBuilderExt.write(sb, "(");
    if (ListExt.isNotEmpty(mc.positionArgs)) {
      FormateUtil.formateArgument(ListExt.first(mc.positionArgs), sb, options);
      for (long i = 1l; i < ListExt.length(mc.positionArgs); i++) {
        StringBuilderExt.write(sb, ", ");
        FormateUtil.formateArgument(ListExt.get(mc.positionArgs, i), sb, options);
      }
    }
    if (!mc.namedArgs.isEmpty()) {
      if (!mc.positionArgs.isEmpty()) {
        StringBuilderExt.write(sb, ",");
      }
      if (ListExt.isNotEmpty(mc.namedArgs)) {
        FormateUtil.formateNamedArgument(ListExt.first(mc.namedArgs), sb, options);
        for (long i = 1l; i < ListExt.length(mc.namedArgs); i++) {
          StringBuilderExt.write(sb, ",");
          FormateUtil.formateNamedArgument(ListExt.get(mc.namedArgs, i), sb, options);
        }
      }
      StringBuilderExt.write(sb, ",");
      StringBuilderExt.write(sb, "\n");
      options.appendDepth(sb);
    }
    StringBuilderExt.write(sb, ")");
  }

  public static void formatePostfixExpression(
      PostfixExpression pe, StringBuilder sb, D3EFormattingOptions options) {
    if (pe.on != null) {
      FormateUtil.formate(pe.on, sb, options);
    }
    StringBuilderExt.write(sb, pe.postfix);
  }

  public static void formatePrefixExpression(
      PrefixExpression pe, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, pe.prefix);
    if (pe.on != null) {
      FormateUtil.formate(pe.on, sb, options);
    }
  }

  public static void formateReturn(Return on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "return");
    if (on.expression != null) {
      StringBuilderExt.write(sb, " ");
      FormateUtil.formate(on.expression, sb, options);
    }
  }

  public static void formateSwitchStatement(
      SwitchStatement on, StringBuilder sb, D3EFormattingOptions main) {
    StringBuilderExt.write(sb, "switch (");
    if (on.test != null) {
      FormateUtil.formate(on.test, sb, main);
    }
    StringBuilderExt.write(sb, ")");
    StringBuilderExt.write(sb, " ");
    StringBuilderExt.write(sb, "{");
    D3EFormattingOptions options = main.inc();
    on.cases.forEach(
        (c) -> {
          FormateUtil.formateSwitchCaseBlock(c, sb, options);
        });
    if (!on.defaults.isEmpty()) {
      StringBuilderExt.write(sb, "\n");
      options.appendDepth(sb);
      StringBuilderExt.write(sb, "default: ");
      StringBuilderExt.write(sb, "\n");
      D3EFormattingOptions sub = options.inc();
      on.defaults.forEach(
          (d) -> {
            sub.appendDepth(sb);
            FormateUtil.formate(d, sb, sub);
            if (StatementUtil.needSemicolon(d)) {
              StringBuilderExt.write(sb, ";");
            }
            StringBuilderExt.write(sb, "\n");
          });
    }
    StringBuilderExt.write(sb, "\n");
    main.appendDepth(sb);
    StringBuilderExt.write(sb, "}");
  }

  public static void formateSwitchCaseBlock(
      SwitchCaseBlock on, StringBuilder sb, D3EFormattingOptions options) {
    on.tests.forEach(
        (t) -> {
          StringBuilderExt.write(sb, "\n");
          options.appendDepth(sb);
          StringBuilderExt.write(sb, "case ");
          FormateUtil.formate(t, sb, options);
          StringBuilderExt.write(sb, ":");
        });
    StringBuilderExt.write(sb, "\n");
    D3EFormattingOptions inc = options.inc();
    on.statements.forEach(
        (s) -> {
          inc.appendDepth(sb);
          FormateUtil.formate(s, sb, inc);
          if (StatementUtil.needSemicolon(s)) {
            StringBuilderExt.write(sb, ";");
          }
          StringBuilderExt.write(sb, "\n");
        });
  }

  public static void formateThrowStatement(
      ThrowStatement on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "throw ");
    if (on.exp != null) {
      FormateUtil.formate(on.exp, sb, options);
    }
  }

  public static void formateTryCatcheStatment(
      TryCatcheStatment on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "try ");
    if (on.body != null) {
      FormateUtil.formate(on.body, sb, options);
    }
    on.catchParts.forEach(
        (c) -> {
          FormateUtil.formateCatchPart(c, sb, options);
        });
    if (on.finallyBody != null) {
      StringBuilderExt.write(sb, " finally ");
      FormateUtil.formate(on.finallyBody, sb, options);
    }
  }

  public static void formateCatchPart(
      CatchPart on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.onType != null) {
      StringBuilderExt.write(sb, " on ");
      FormateUtil.formateDataType(on.onType, sb, options);
    }
    if (on.exp != null) {
      StringBuilderExt.write(sb, " catch ");
      StringBuilderExt.write(sb, "(");
      StringBuilderExt.write(sb, on.exp);
      if (on.stackTrace != null) {
        StringBuilderExt.write(sb, ", ");
        StringBuilderExt.write(sb, on.stackTrace);
      }
      StringBuilderExt.write(sb, ") ");
    }
    if (on.body != null) {
      FormateUtil.formate(on.body, sb, options);
    }
  }

  public static void formateWhileLoop(
      WhileLoop on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "while ");
    StringBuilderExt.write(sb, "(");
    if (on.test != null) {
      FormateUtil.formate(on.test, sb, options);
    }
    StringBuilderExt.write(sb, ")");
    if (on.body != null) {
      StringBuilderExt.write(sb, " ");
      FormateUtil.formate(on.body, sb, options);
    }
  }

  public static void formateArgument(Argument on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.arg != null) {
      FormateUtil.formate(on.arg, sb, options);
    }
  }

  public static void formateDataType(DataType on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, on.name);
    /*
     if (on.args != null) {
         formateTypeArguments(on.args, sb, options);
     }
    */
  }

  public static void formateNamedArgument(
      NamedArgument on, StringBuilder sb, D3EFormattingOptions options) {
    D3EFormattingOptions inc = options.inc();
    if (on.beforeComments != null) {
      if (on.name != null) {
        StringBuilderExt.write(sb, "\n");
        inc.appendDepth(sb);
      }
    }
    if (on.name != null) {
      if (on.beforeComments == null) {
        StringBuilderExt.write(sb, "\n");
        inc.appendDepth(sb);
      }
      StringBuilderExt.write(sb, on.name);
    }
    if (on.value != null) {
      StringBuilderExt.write(sb, ": ");
      FormateUtil.formate(on.value, sb, inc);
    }
  }

  public static void formateMethodParam(
      MethodParam on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.deprecated) {
      StringBuilderExt.write(sb, "@deprecated ");
    }
    if (on.dataType != null) {
      FormateUtil.formateDataType(on.dataType, sb, options);
      StringBuilderExt.write(sb, " ");
    } else if (on.thisToken != null) {
      StringBuilderExt.write(sb, on.thisToken);
      StringBuilderExt.write(sb, ".");
    }
    if (on.name != null) {
      StringBuilderExt.write(sb, on.name);
    }
    /*
     if (on.params != null) {
         formateMethodParams(on.params, sb, options);
     }
    */
    if (on.defaultValue != null) {
      StringBuilderExt.write(sb, " = ");
      FormateUtil.formate(on.defaultValue, sb, options);
    }
  }

  public static void formateMethodParams(
      MethodParams on, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "(");
    options.appendCollection(
        sb,
        on.positionalParams,
        (i) -> {
          FormateUtil.formateMethodParam(i, sb, options);
        });
    if (!on.namedParams.isEmpty()) {
      if (!on.positionalParams.isEmpty()) {
        StringBuilderExt.write(sb, ", ");
      }
      StringBuilderExt.write(sb, "{");
      options.appendCollection(
          sb,
          on.namedParams,
          (i) -> {
            FormateUtil.formateMethodParam(i, sb, options);
          });
      StringBuilderExt.write(sb, "}");
    }
    if (!on.optionalParams.isEmpty()) {
      if (!on.positionalParams.isEmpty()) {
        StringBuilderExt.write(sb, ", ");
      }
      StringBuilderExt.write(sb, "[");
      options.appendCollection(
          sb,
          on.optionalParams,
          (i) -> {
            FormateUtil.formateMethodParam(i, sb, options);
          });
      StringBuilderExt.write(sb, "]");
    }
    StringBuilderExt.write(sb, ")");
  }

  public static void formateTypeParams(
      List<TypeParam> list, StringBuilder sb, D3EFormattingOptions options) {
    StringBuilderExt.write(sb, "<");
    if (ListExt.isNotEmpty(list)) {
      options.appendCollection(
          sb,
          list,
          (i) -> {
            FormateUtil.formateTypeParam(i, sb, options);
          });
    }
    StringBuilderExt.write(sb, ">");
  }

  public static void formateTypeParam(
      TypeParam on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.name != null) {
      StringBuilderExt.write(sb, on.name);
    }
    if (on.extendType != null) {
      StringBuilderExt.write(sb, " extends ");
      FormateUtil.formateDataType(on.extendType, sb, options);
    }
  }

  public static void formateFieldDecl(
      FieldDecl on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.staticValue) {
      StringBuilderExt.write(sb, "static ");
    }
    if (on.finalValue) {
      StringBuilderExt.write(sb, "final ");
    }
    if (on.constValue) {
      StringBuilderExt.write(sb, "const ");
    }
    if (on.type != null) {
      FormateUtil.formateDataType(on.type, sb, options);
    }
    StringBuilderExt.write(sb, " ");
    StringBuilderExt.write(sb, on.name);
    if (on.value != null) {
      StringBuilderExt.write(sb, " = ");
      FormateUtil.formate(on.value, sb, options);
    }
    StringBuilderExt.write(sb, ";");
  }

  public static void formateTypedef(
      MethodDecl on,
      StringBuilder sb,
      D3EFormattingOptions options,
      String name,
      TypeParams generics) {
    StringBuilderExt.write(sb, "typedef");
    StringBuilderExt.write(sb, " ");
    if (on.returnType != null) {
      FormateUtil.formateDataType(on.returnType, sb, options);
    }
    StringBuilderExt.write(sb, " ");
    if (on.name != null) {
      StringBuilderExt.write(sb, on.name);
    }
    if (on.generics != null) {
      FormateUtil.formateTypeParams(on.generics.params, sb, options);
    }
    /*
     if (on.params != null) {
         formateMethodParams(on.params, sb, options);
     }
    */
  }

  public static void formateMethodDecl(
      MethodDecl on, StringBuilder sb, D3EFormattingOptions options) {
    if (on.staticValue) {
      StringBuilderExt.write(sb, "static ");
    }
    if (on.finalValue) {
      StringBuilderExt.write(sb, "final ");
    }
    if (on.constValue) {
      StringBuilderExt.write(sb, "const ");
    }
    if (on.factory) {
      StringBuilderExt.write(sb, "factory ");
    }
    if (on.factoryName != null) {
      StringBuilderExt.write(sb, on.factoryName);
      StringBuilderExt.write(sb, ".");
      StringBuilderExt.write(sb, on.name);
    } else {
      if (on.returnType != null) {
        FormateUtil.formateDataType(on.returnType, sb, options);
        StringBuilderExt.write(sb, " ");
      }
      if (on.setter) {
        StringBuilderExt.write(sb, "set ");
      }
      if (on.getter) {
        StringBuilderExt.write(sb, "get ");
      }
      StringBuilderExt.write(sb, on.name);
    }
    if (on.generics != null) {
      FormateUtil.formateTypeParams(on.generics.params, sb, options);
    }
    /*
     if (on.params != null) {
         formateMethodParams(on.params, sb, options);
     }
    */
    if (on.init != null) {
      StringBuilderExt.write(sb, ": ");
      FormateUtil.formate(on.init, sb, options);
    }
    if (on.body != null) {
      if (on.asyncType != ASyncType.NONE) {
        StringBuilderExt.write(sb, " ");
        StringBuilderExt.write(sb, on.asyncType.toString().toLowerCase());
      }
      StringBuilderExt.write(sb, " ");
      FormateUtil.formateBlock(on.body, sb, options, true);
    } else {
      if (on.exp != null) {
        StringBuilderExt.write(sb, " => ");
        FormateUtil.formate(on.exp, sb, options);
      }
      StringBuilderExt.write(sb, ";");
    }
  }

  public static void formateParam(Param on, StringBuilder sb, D3EFormattingOptions options) {
    /*
     if (on.paramType != null) {
         sb.write(on.paramType, sb, options);
     }
    */
    StringBuilderExt.write(sb, " ");
    if (on.name != null) {
      StringBuilderExt.write(sb, on.name);
    }
  }

  public static String toStringLambdaType(LambdaType on) {
    StringBuilder sb = StringBuilderExt.StringBuffer("");
    StringBuilderExt.write(sb, "(");
    if (!on.params.isEmpty()) {
      StringBuilderExt.write(sb, ListExt.get(on.params, 0l));
      for (long i = 1l; i < ListExt.length(on.params); i++) {
        StringBuilderExt.write(sb, ", ");
        StringBuilderExt.write(sb, ListExt.get(on.params, i));
      }
    }
    StringBuilderExt.write(sb, ")");
    StringBuilderExt.write(sb, " => ");
    StringBuilderExt.write(sb, on.returnType);
    StringBuilderExt.write(sb, ";");
    return sb.toString();
  }

  public static String toStringDataType(DataType on) {
    StringBuilder sb = StringBuilderExt.StringBuffer("");
    FormateUtil.formateDataType(on, sb, D3EFormattingOptions.withDefault());
    return sb.toString();
  }

  public static String toStringStatement(Statement on) {
    StringBuilder sb = StringBuilderExt.StringBuffer("");
    FormateUtil.formate(on, sb, D3EFormattingOptions.withDefault());
    return sb.toString();
  }

  public static String toStringTypeParams(TypeParams on) {
    StringBuilder sb = StringBuilderExt.StringBuffer("");
    FormateUtil.formateTypeParams(on.params, sb, D3EFormattingOptions.withDefault());
    return sb.toString();
  }

  public static String toStringPropType(PropType on) {
    if (!on.typeVars.isEmpty()) {
      StringBuilder sb = StringBuilderExt.StringBuffer(on.name);
      StringBuilderExt.write(sb, "<");
      if (on instanceof ParameterizedType) {
        ParameterizedType pt = ((ParameterizedType) on);
        List<String> types =
            IterableExt.toList(
                ListExt.map(
                    pt.arguments,
                    (x) -> {
                      return x.name.toString();
                    }),
                false);
        StringBuilderExt.write(sb, ListExt.join(types, ", "));
      } else {
        StringBuilderExt.write(sb, ListExt.join(on.typeVars, ", "));
      }
      StringBuilderExt.write(sb, ">");
      return sb.toString();
    }
    return on.name;
  }

  public static String toStringTypeVariable(TypeVariable on) {
    return "~" + FormateUtil.toStringPropType(on);
  }

  public static String toStringTypeParam(TypeParam on) {
    if (on.extendType == null) {
      return on.name;
    }
    return on.name.toString() + " extends " + on.extendType.toString();
  }
}
