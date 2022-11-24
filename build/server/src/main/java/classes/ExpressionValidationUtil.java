package classes;

import d3e.core.D3ELogger;
import d3e.core.IntegerExt;
import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.SetExt;
import d3e.core.StringBuilderExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class ExpressionValidationUtil {
  public ExpressionValidationUtil() {}

  public static void validate(Expression on, ValidationContext ctx) {
    if (on instanceof ArrayAccess) {
      ExpressionValidationUtil.validateArrayAccess(((ArrayAccess) on), ctx);
    } else if (on instanceof ArrayExpression) {
      ArrayExpression exp = ((ArrayExpression) on);
      ExpressionValidationUtil.validateArrayExpression(((ArrayExpression) on), ctx);
    } else if (on instanceof BinaryExpression) {
      ExpressionValidationUtil.validateBinaryExpression(((BinaryExpression) on), ctx);
    } else if (on instanceof FieldOrEnumExpression) {
      ExpressionValidationUtil.validateFieldOrEnumExpression(((FieldOrEnumExpression) on), ctx);
    } else if (on instanceof LambdaExpression) {
      LambdaExpression exp = ((LambdaExpression) on);
      ExpressionValidationUtil.validateLambdaExpression(((LambdaExpression) on), ctx);
    } else if (on instanceof LiteralExpression) {
      ExpressionValidationUtil.validateLiteralExpression(((LiteralExpression) on), ctx);
    } else if (on instanceof NullExpression) {
      ExpressionValidationUtil.validateNullExpression(((NullExpression) on), ctx);
    } else if (on instanceof ParExpression) {
      ExpressionValidationUtil.validateParExpression(((ParExpression) on), ctx);
    } else if (on instanceof SwitchExpression) {
      ExpressionValidationUtil.validateSwitchExpression(((SwitchExpression) on), ctx);
    } else if (on instanceof TerinaryExpression) {
      ExpressionValidationUtil.validateTerinaryExpression(((TerinaryExpression) on), ctx);
    } else if (on instanceof TypeCastOrCheckExpression) {
      ExpressionValidationUtil.validateTypeCastOrCheckExpression(
          ((TypeCastOrCheckExpression) on), ctx);
    } else if (on instanceof CollectionIf) {
      ExpressionValidationUtil.validateCollectionIf(((CollectionIf) on), ctx);
    } else if (on instanceof CollectionFor) {
      ExpressionValidationUtil.validateCollectionFor(((CollectionFor) on), ctx);
    } else if (on instanceof CollectionSpread) {
      ExpressionValidationUtil.validateCollectionSpread(((CollectionSpread) on), ctx);
    } else if (on instanceof ExpressionArrayItem) {
      ExpressionValidationUtil.validateExpressionArrayItem(((ExpressionArrayItem) on), ctx);
    } else if (on instanceof Assignment) {
      ExpressionValidationUtil.validateAssignment(((Assignment) on), ctx);
    } else if (on instanceof Block) {
      ExpressionValidationUtil.validateBlock(((Block) on), ctx);
    } else if (on instanceof Break) {
      ExpressionValidationUtil.validateBreak(((Break) on), ctx);
    } else if (on instanceof Continue) {
      ExpressionValidationUtil.validateContinue(((Continue) on), ctx);
    } else if (on instanceof Declaration) {
      ExpressionValidationUtil.validateDeclaration(((Declaration) on), ctx);
    } else if (on instanceof DoWhileLoop) {
      ExpressionValidationUtil.validateDoWhileLoop(((DoWhileLoop) on), ctx);
    } else if (on instanceof ForLoop) {
      ExpressionValidationUtil.validateForLoop(((ForLoop) on), ctx);
    } else if (on instanceof ForEachLoop) {
      ExpressionValidationUtil.validateForEachLoop(((ForEachLoop) on), ctx);
    } else if (on instanceof IfStatement) {
      ExpressionValidationUtil.validateIfStatement(((IfStatement) on), ctx);
    } else if (on instanceof MethodCall) {
      MethodCall exp = ((MethodCall) on);
      ExpressionValidationUtil.validateMethodCall(exp, ctx);
    } else if (on instanceof PostfixExpression) {
      ExpressionValidationUtil.validatePostfixExpression(((PostfixExpression) on), ctx);
    } else if (on instanceof PrefixExpression) {
      ExpressionValidationUtil.validatePrefixExpression(((PrefixExpression) on), ctx);
    } else if (on instanceof Return) {
      ExpressionValidationUtil.validateReturn(((Return) on), ctx);
    } else if (on instanceof SwitchStatement) {
      ExpressionValidationUtil.validateSwitchStatement(((SwitchStatement) on), ctx);
    } else if (on instanceof ThrowStatement) {
      ExpressionValidationUtil.validateThrowStatement(((ThrowStatement) on), ctx);
    } else if (on instanceof TryCatcheStatment) {
      ExpressionValidationUtil.validateTryCatcheStatment(((TryCatcheStatment) on), ctx);
    } else if (on instanceof WhileLoop) {
      ExpressionValidationUtil.validateWhileLoop(((WhileLoop) on), ctx);
    }
  }

  public static void validateNullExpression(NullExpression on, ValidationContext ctx) {
    on.expType = ctx.nullType();
  }

  public static void validateAbstractForEachLoop(ForEachLoop on, ValidationContext ctx) {
    PropType bool = ctx.bool();
    ctx.addAttribute("#c", true);
    ctx.addAttribute("#b", true);
    if (on.dataType != null) {
      ExpressionValidationUtil.validateDataType(on.dataType, ctx);
    } else {
      ctx.addError(null, "Type is required");
    }
    PropType varType = ctx.typeOrObjectData(on.dataType);
    if (on.name != null) {
      if (ctx.hasLocalVar(on.name)) {
        ctx.addError(null, "Duplicate variable name \"" + on.name + "\" found.");
      }
      ctx.addLocalVar(varType, on.name, false);
    } else {
      ctx.addError(null, "variable name is required");
    }
    if (on.collection == null) {
      ctx.addError(null, "collection expression is required");
    } else {
      ExpressionValidationUtil.validate(on.collection, ctx);
      PropType collType = ctx.typeOrObject(on.collection);
      PropType iterable = ctx.getType("Iterable");
      if (iterable.isAssignableFrom(collType)) {
        PropType elementType = collType.getElemenetType();
        if (elementType != null && !varType.isAssignableFrom(elementType)) {
          ctx.addError(
              on.collection.range,
              "Can not convert \""
                  + elementType.toString()
                  + "\" to \""
                  + varType.toString()
                  + "\"");
        }
      } else {
        ctx.addError(
            on.collection.range,
            "Expression type(" + collType.toString() + ") should be collection");
      }
    }
  }

  public static void validateArgument(Argument on, ValidationContext ctx) {
    if (on.arg == null) {
      ctx.addError(null, "Invalid argument");
    } else {
      ExpressionValidationUtil.validate(on.arg, ctx);
    }
  }

  public static void validateArrayAccess(ArrayAccess a, ValidationContext ctx) {
    ExpressionValidationUtil.validate(a.on, ctx);
    ExpressionValidationUtil.validate(a.index, ctx);
    a.method =
        ExpressionValidationUtil.validateWithOp(
            ctx,
            a.range,
            null,
            a.index.range,
            ctx.typeOrObject(a.on),
            ctx.typeOrObject(a.index),
            "[]");
    a.expType = ctx.typeOrObject(a.on).elementType();
  }

  public static void validateArrayExpression(ArrayExpression on, ValidationContext ctx) {
    ValidationContext sub;
    PropType pt;
    if (on.enforceType != null) {
      ExpressionValidationUtil.validateDataType(on.enforceType, ctx);
      pt = on.enforceType.resolvedType;
      sub = ctx.subWithType(pt);
    } else {
      pt = null;
      sub = ctx;
    }
    ValidationContext sub$final = sub;
    on.values.forEach(
        (e) -> {
          ExpressionValidationUtil.validate(e, sub$final);
        });
    PropType pt$final = pt;
    ValidationContext sub$final1 = sub;
    if (pt$final != null) {
      ListExt.where(
              on.values,
              (v) -> {
                return !pt$final.isAssignableFrom(sub$final1.typeOrObject(v));
              })
          .forEach(
              (a) -> {
                sub$final1.addError(
                    a.range,
                    "Typemismatch: can not convert from "
                        + sub$final1.typeOrObject(a).toString()
                        + " to "
                        + pt$final.toString());
              });
    }
    PropType elementType = ExpressionValidationUtil.computeElementType(on, ctx);
    on.elementType = elementType;
    if (on.getList()) {
      on.expType = ctx.list(elementType);
    } else {
      on.expType = ctx.set(elementType);
    }
  }

  public static PropType computeElementType(ArrayExpression on, ValidationContext ctx) {
    if (on.enforceType != null) {
      return ctx.typeOrObjectData(on.enforceType);
    } else {
      if (on.values.isEmpty()) {
        PropType type = ctx.getExpectedType();
        if (type == null) {
          type = ctx.object();
        } else {
          PropType listType = ctx.getType("Iterable");
          if (listType.isAssignableFrom(type)) {
            return type.elementTypeWithIndex(0l);
          }
        }
        return type;
      }
      return ctx.findSuperType(
          IterableExt.toList(
              ListExt.map(
                  on.values,
                  (e) -> {
                    return ctx.typeOrObject(e);
                  }),
              false));
    }
  }

  public static boolean shouldNotAssignToField(Expression exp) {
    if (exp == null) {
      return false;
    }
    if (!(exp instanceof FieldOrEnumExpression)) {
      return false;
    }
    FieldOrEnumExpression fe = ((FieldOrEnumExpression) exp);
    ClassDecl type = fe.fieldClass;
    if (type == null) {
      return false;
    }
    FieldDecl curField =
        IterableExt.firstWhere(
            type.getFields(),
            (f) -> {
              return Objects.equals(f.name, fe.name);
            },
            null);
    if (curField == null) {
      return false;
    }
    return curField.finalValue || curField.constValue;
  }

  public static void validateAssignment(Assignment on, ValidationContext ctx) {
    if (on.left instanceof FieldOrEnumExpression) {
      FieldOrEnumExpression fe = ((FieldOrEnumExpression) on.left);
      fe.setter = true;
      ExpressionValidationUtil.validate(on.left, ctx);
      if (fe.evalType == EvalType.LOCAL_VAR) {
        LocalVar local = ctx.findLocalVar(fe.name);
        local.markNotFinal();
      }
    } else {
      /*
       This case is wrong because on.left cannot be anything other than FieldOrEnumExpression
      */
      ctx.addError(null, "Invalid assignment operator");
    }
    PropType lt = ctx.typeOrObject(on.left);
    ctx = ctx.subWithType(lt);
    if (on.right == null) {
      ctx.addError(null, "Assignment value is requried");
    } else {
      ExpressionValidationUtil.validate(on.right, ctx);
    }
    if (ExpressionValidationUtil.shouldNotAssignToField(on.left)) {
      ctx.addError(on.left.range, "Cannot assign to this field: " + on.left.toString());
      return;
    }
    PropType rt = ctx.typeOrObject(on.right);
    switch (on.op) {
      case "=":
        {
          if (!lt.isAssignableFrom(rt)) {
            ctx.addError(
                null, "Can not assign \"" + rt.toString() + "\" to \"" + lt.toString() + "\"");
          }
          break;
        }
      default:
        {
          ExpressionValidationUtil.validateWithOp(
              ctx, null, on.left.range, on.right.range, lt, rt, on.op);
        }
    }
    on.expType = ExpressionValidationUtil.computeReturnType(on, ctx);
  }

  public static FieldDecl getProp(Assignment on, ValidationContext ctx) {
    if (on.left instanceof FieldOrEnumExpression) {
      FieldOrEnumExpression fe = ((FieldOrEnumExpression) on.left);
      if (fe.on == null) {
        return null;
      }
      PropType type = ctx.typeOrObject(fe.on);
      return type.getField(fe.name);
    } else {
      return null;
    }
  }

  public static void validateBinaryExpression(BinaryExpression on, ValidationContext ctx) {
    on.expType = null;
    if (on.left != null) {
      ExpressionValidationUtil.validate(on.left, ctx);
    }
    PropType lt = ctx.typeOrObject(on.left);
    ctx = ctx.subWithType(lt);
    if (on.right != null) {
      ExpressionValidationUtil.validate(on.right, ctx);
    }
    if (on.left == null || on.right == null) {
      ctx.addError(null, "Left and Right operand should be requried");
      return;
    }
    PropType rt = ctx.typeOrObject(on.right);
    on.method =
        ExpressionValidationUtil.validateWithOp(
            ctx, null, on.left.range, on.right.range, lt, rt, on.op);
    on.expType = ExpressionValidationUtil.typeBinaryExpression(on, ctx);
  }

  public static MethodDecl validateWithOp(
      ValidationContext ctx,
      Range _this,
      Range left,
      Range right,
      PropType lt,
      PropType rt,
      String op) {
    if (op == null) {
      ctx.addError(left, "Invalid operator");
      return null;
    }
    if (Objects.equals("==", op) || Objects.equals("!=", op)) {
      if (!lt.canCompare(rt)) {
        ctx.addError(
            left,
            "Can not compare two different Types \""
                + lt.toString()
                + "\" and \""
                + rt.toString()
                + "\"");
      }
      return null;
    }
    if (lt == null) {
      ctx.addError(_this, "left side is null");
      return null;
    }
    MethodDecl method = lt.findOperatorMethod(ctx, op, rt);
    if (method == null) {
      ctx.addError(
          _this,
          "The operator "
              + op.toString()
              + " is undefined for the argument type(s) "
              + lt.toString()
              + ", "
              + rt.toString());
    }
    return method;
  }

  public static PropType typeBinaryExpression(BinaryExpression on, ValidationContext ctx) {
    if (on.expType != null) {
      return on.expType;
    }
    if (Objects.equals("==", on.op) || Objects.equals("!=", on.op)) {
      on.expType = ctx.bool();
      return on.expType;
    }
    PropType lt = ctx.typeOrObject(on.left);
    ctx = ctx.subWithType(lt);
    PropType rt = ctx.typeOrObject(on.right);
    if (lt == null) {
      on.expType = ctx.object();
      return on.expType;
    }
    MethodDecl method = lt.findOperatorMethod(ctx, on.op, rt);
    if (method != null) {
      on.expType =
          lt.resolveType(
              ctx,
              method.returnType.resolvedType,
              ExpressionValidationUtil.createTypeArguments(method, ctx, ListExt.List(0l)));
      return on.expType;
    }
    on.expType = ctx.object();
    return on.expType;
  }

  public static Map<String, PropType> createTypeArguments(
      MethodDecl on, ValidationContext ctx, List<PropType> typeArgs) {
    if (ctx == null || typeArgs == null || !on.getHaveTypeParams()) {
      return MapExt.Map();
    }
    Map<String, PropType> result = MapExt.Map();
    for (TypeParam t : on.generics.params) {
      /*
       FIXME
       List<TypeResolutionPosition> positions = t.positions;
      */
      List<PropType> all = ListExt.List(0l);
      /*
       for (TypeResolutionPosition p in positions) {
           PropType res = getTypeAtPosition(on, ctx, p);
           if (res != null) {
               all.add(res);
           }
       }
      */
      MapExt.set(result, t.name, ctx.findSuperType(all));
    }
    /*
    for (Integer x = 0; x < typeArgs.length && x < typeVars.length; x++) {
    	result.set(typeVars.get(x).name, typeArgs.get(x));
    }
    */
    return result;
  }

  public static void validateBlock(Block on, ValidationContext context) {
    ValidationContext ctx = context.createSharedSub();
    boolean hasReturnType = false;
    for (Statement s : on.statements) {
      if (s == null) {
        break;
      }
      if (hasReturnType) {
        ctx.addError(s.range, "Unreachable code");
        break;
      }
      ctx.statement = s;
      s.finalVars.clear();
      ExpressionValidationUtil.validate(s, ctx);
      ctx.statement = null;
      hasReturnType = ExpressionValidationUtil.computeReturnType(s, ctx) != null;
    }
    on.returnType = ExpressionValidationUtil.computeReturnType(on, ctx);
    for (Statement s : on.statements) {
      ListExt.removeWhere(
          s.finalVars,
          (v) -> {
            return v.isFinal;
          });
    }
  }

  public static boolean hasBoolAttribute(Object attr) {
    if (attr == null) {
      return false;
    }
    try {
      return (((Boolean) attr));
    } catch (RuntimeException e) {
      return false;
    }
  }

  public static void validateBreak(Break on, ValidationContext ctx) {
    Object localVar = ctx.getAttribute("#b");
    if (!ExpressionValidationUtil.hasBoolAttribute(localVar)) {
      ctx.addError(null, "break is not valid out side loops");
    }
  }

  public static void validateCatchPart(CatchPart on, ValidationContext ctx) {
    if (on.onType != null) {
      ExpressionValidationUtil.validateDataType(on.onType, ctx);
    }
    if (on.body != null) {
      PropType expType = on.onType == null ? ctx.getType("Exception") : on.onType.resolvedType;
      ValidationContext sub = ctx.createSharedSub();
      if (on.exp != null) {
        sub.addLocalVar(expType, on.exp, false);
      }
      if (on.stackTrace != null) {
        sub.addLocalVar(ctx.getType("StackTrace"), on.stackTrace, false);
      }
      ExpressionValidationUtil.validate(on.body, sub);
    } else {
      ctx.addError(null, "Catch block is required");
    }
  }

  public static void validateCollectionFor(CollectionFor on, ValidationContext context) {
    ValidationContext ctx = context.createSharedSub();
    if (on.stmt instanceof ForEachLoop) {
      ExpressionValidationUtil.validateForEachLoop(((ForEachLoop) on.stmt), context);
    } else if (on.stmt instanceof ForLoop) {
      ExpressionValidationUtil.validateForLoop(((ForLoop) on.stmt), context);
    }
    if (on.value != null) {
      ExpressionValidationUtil.validate(on.value, ctx);
      if (on.value != null) {
        on.expType = ctx.typeOrObject(on.value);
      }
    } else {
      ctx.addError(null, "For body is required");
      on.expType = ctx.object();
    }
  }

  public static void validateCollectionIf(CollectionIf on, ValidationContext ctx) {
    if (on.test != null) {
      ExpressionValidationUtil.validate(on.test, ctx);
      PropType type = ctx.typeOrObject(on.test);
      if (!type.isBool()) {
        ctx.addError(on.test.range, "Can not convert Boolean from \"" + type.toString() + "\"");
      }
    }
    if (on.thenItem != null) {
      ExpressionValidationUtil.validate(on.thenItem, ctx);
    }
    if (on.elseItem != null) {
      ExpressionValidationUtil.validate(on.elseItem, ctx);
    }
    if (on.elseItem == null) {
      on.expType = ctx.typeOrObject(on.thenItem);
    } else {
      on.expType = ctx.getCommonType(ctx.typeOrObject(on.thenItem), ctx.typeOrObject(on.elseItem));
    }
  }

  public static void validateCollectionSpread(CollectionSpread on, ValidationContext ctx) {
    ExpressionValidationUtil.validate(on.values, ctx);
    PropType type = ctx.typeOrObject(on.values);
    if (!type.isCollection()) {
      ctx.addError(on.values.range, "Expression type should be collection");
    }
    on.expType = type.elementType();
  }

  public static void validateContinue(Continue on, ValidationContext ctx) {
    Object localVar = ctx.getAttribute("#c");
    if (!ExpressionValidationUtil.hasBoolAttribute(localVar)) {
      ctx.addError(null, "'continue' is not valid out side loops");
    }
  }

  public static void validateDataType(DataType on, ValidationContext ctx) {
    if (on instanceof ValueType) {
      ValueType von = ((ValueType) on);
      PropType propType = ctx.getType(von.name);
      if (propType == null) {
        ctx.addError(null, "Invalid type \"" + on.name + "\"");
        propType = ctx.object();
      } else {
        ctx.addUsedType(propType);
        ctx.addTypeUsage(propType);
      }
      if (ListExt.isNotEmpty(von.args)) {
        ExpressionValidationUtil.validateTypeArguments(von.args, ctx, propType.typeVars);
      }
      on.resolvedType = von.type(ctx);
    } else if (on instanceof FunctionType) {
      FunctionType fnType = ((FunctionType) on);
      ExpressionValidationUtil.validateDataType(fnType.returnType, ctx);
      for (MethodParam p : fnType.params) {
        ExpressionValidationUtil.validateMethodParam(p, ctx, ctx.getDataType());
      }
      if (ListExt.isNotEmpty(fnType.typeArgs)) {
        D3ELogger.error("Type Args for Function type are not supported");
        /*
         validateTypeArguments(fnType.typeArgs, ctx, propType.typeVars);
        */
      }
      on.resolvedType = fnType.type(ctx);
    }
  }

  public static void validateDeclaration(Declaration on, ValidationContext ctx) {
    if (on.type != null) {
      ExpressionValidationUtil.validateDataType(on.type, ctx);
      on.expType = on.type.resolvedType;
    } else {
      ctx.addError(null, "Type is required");
    }
    PropType propType = ctx.typeOrObjectData(on.type);
    if (propType == null) {
      propType = ctx.object();
    }
    for (NameAndValue nv : on.names) {
      /*
       ReservedWords.checkReserved(nv.name, ctx, null);
      */
      if (ctx.hasLocalVar(nv.name)) {
        ctx.addError(null, "Found duplicate field \"" + nv.name + "\"");
      } else {
        ctx.addLocalVar(propType, nv.name, false);
      }
      if (nv.value != null) {
        ValidationContext sub = ctx.subWithType(propType);
        ExpressionValidationUtil.validate(nv.value, sub);
        PropType assignType = sub.typeOrObject(nv.value);
        if (assignType != null && !propType.isAssignableFrom(assignType)) {
          sub.addError(
              nv.value.range,
              "Can not assign \""
                  + assignType.toString()
                  + "\" to type \""
                  + propType.toString()
                  + "\"");
        }
      }
    }
  }

  public static void validateDoWhileLoop(DoWhileLoop on, ValidationContext ctx) {
    if (on.test == null) {
      ctx.addError(null, "Test expression is required");
    } else {
      ExpressionValidationUtil.validate(on.test, ctx);
      PropType testType = ctx.typeOrObject(on.test);
      if (!testType.isBool()) {
        ctx.addError(on.test.range, "Can not convert Boolean from \"" + testType.toString() + "\"");
      }
    }
    ValidationContext sub = ctx.createSharedSub();
    PropType bool = sub.bool();
    sub.addAttribute("#c", true);
    sub.addAttribute("#b", true);
    if (on.body == null) {
      /*
       sub.addWarn(null, 'Found empty body');
      */
    } else {
      ExpressionValidationUtil.validate(on.body, sub);
    }
  }

  public static void validateExpressionArrayItem(ExpressionArrayItem on, ValidationContext ctx) {
    if (on.exp != null) {
      ExpressionValidationUtil.validate(on.exp, ctx);
    }
    on.expType = ctx.typeOrObject(on.exp);
  }

  public static void validateFieldOrEnumExpression(
      FieldOrEnumExpression fe, ValidationContext ctx) {
    ExpressionValidationUtil.validateInternal(fe, ctx);
  }

  public static void validateInternal(FieldOrEnumExpression fe, ValidationContext ctx) {
    fe.evalType = EvalType.ERROR;
    fe.expType = null;
    if (fe.on != null) {
      if (fe.on instanceof FieldOrEnumExpression) {
        PropType eval =
            ExpressionValidationUtil.evalStaticType((((FieldOrEnumExpression) fe.on)), ctx);
        if (eval != null) {
          ctx.addUsedType(eval);
          ctx.addTypeUsage(eval);
          if (!ExpressionValidationUtil.checkOnField(fe, ctx, eval, true)) {
            ctx.addError(
                fe.range,
                "Invalid static field '"
                    + fe.name.toString()
                    + "' on type '"
                    + eval.toString()
                    + "'");
          }
          return;
        }
      }
      ExpressionValidationUtil.validate(fe.on, ctx);
      PropType type = ctx.typeOrObject(fe.on);
      ctx.addUsedType(type);
      if (!ExpressionValidationUtil.checkOnField(fe, ctx, type, false)) {
        ctx.addError(
            fe.range,
            "Invalid field \"" + fe.name.toString() + "\" on type \"" + type.toString() + "\"");
        return;
      }
    } else {
      if (Objects.equals(fe.name, "super")) {
        LocalVar _this = ctx.findLocalVar("this");
        if (_this != null) {
          PropType _extends = _this.type.extendsValue;
          fe.expType = _extends;
          fe.evalType = EvalType.LOCAL_VAR;
          return;
        }
      }
      PropType et = ctx.getExpectedType();
      if (et != null && et.enm != null) {
        FieldDecl findField = et.findField(fe.name, true);
        if (findField != null) {
          if (!ExpressionValidationUtil.checkPrivateFieldAccess(findField.name, true, et, ctx)) {
            ctx.addError(
                fe.range, "FieldDecl " + findField.name + " in " + et.name + " is not visible.");
            return;
          }
          fe.resolvedMember = findField;
          fe.evalType = EvalType.STATIC_FIELD;
          fe.fieldClass = et.cls;
          fe.expType = et;
          ctx.addTypeUsage(et);
          return;
        }
      }
      LocalVar findLocalVar = ctx.findLocalVar(fe.name);
      if (findLocalVar == null) {
        LocalVar _this = ctx.findLocalVar("this");
        if (_this != null) {
          if (ExpressionValidationUtil.checkOnField(fe, ctx, _this.type, false)) {
            return;
          }
        }
        if (ExpressionValidationUtil.checkOnField(fe, ctx, ctx.getDataType(), true)) {
          return;
        }
        if (ExpressionValidationUtil.checkOnLibraryFunction(fe, ctx)) {
          return;
        }
        ctx.addError(
            fe.range, "Invalid field or type or enum value \"" + fe.name.toString() + "\"");
      } else {
        if (!findLocalVar.initialized) {
          LocalVar _this = ctx.findLocalVar("this");
          if (_this != null) {
            if (ExpressionValidationUtil.checkOnField(fe, ctx, _this.type, false)) {
              return;
            }
          }
          if (ExpressionValidationUtil.checkOnField(fe, ctx, ctx.getDataType(), true)) {
            return;
          }
          ctx.addError(
              fe.range,
              "The local variable \"" + fe.name.toString() + "\" may not have been initialized");
        }
        fe.evalType = EvalType.LOCAL_VAR;
        fe.expType = findLocalVar.type;
        ctx.addLambdaUsed(findLocalVar, false);
      }
    }
  }

  public static PropType evalStaticType(FieldOrEnumExpression fe, ValidationContext ctx) {
    PropType type = null;
    if (fe.on == null) {
      PropType et = ctx.getExpectedType();
      if (et instanceof EnumType) {
        if (et.hasField(ctx, fe.name, true)) {
          fe.expType = et;
          return null;
        }
      }
      type = ctx.getType(fe.name);
    } else {
      if (fe.on instanceof FieldOrEnumExpression) {
        String pkg = ExpressionValidationUtil.pkgName((((FieldOrEnumExpression) fe.on)));
        if (pkg != null) {
          type = ctx.getType(pkg + "." + fe.name);
        }
      }
    }
    if (type != null) {
      fe.fieldClass = type.cls;
      fe.evalType = EvalType.TYPE;
    }
    return type;
  }

  public static String pkgName(FieldOrEnumExpression fe) {
    if (fe.on == null) {
      return fe.name;
    }
    if (fe.on instanceof FieldOrEnumExpression) {
      String pkg = ExpressionValidationUtil.pkgName((((FieldOrEnumExpression) fe.on)));
      if (pkg != null) {
        return pkg + "." + fe.name;
      } else {
        return null;
      }
    }
    return null;
  }

  public static boolean checkPrivateFieldAccess(
      String fieldName, boolean isStatic, PropType type, ValidationContext ctx) {
    if (StringExt.startsWith(fieldName, "_", 0l)) {
      PropType typeWithField = null;
      if (!isStatic) {
        LocalVar _this = ctx.findLocalVar("this");
        if (_this != null && _this.type != null) {
          typeWithField = _this.type;
        }
      } else {
        typeWithField = ctx.getDataType();
      }
      if (typeWithField != null && !(Objects.equals(type, typeWithField))) {
        return false;
      }
    }
    return true;
  }

  public static boolean checkOnField(
      FieldOrEnumExpression fe, ValidationContext ctx, PropType type, boolean isStatic) {
    if (type == null) {
      return false;
    }
    String fieldName = fe.name;
    FieldDecl field = type.getField(fieldName);
    if (field != null && (field.type != null)) {
      if (!ExpressionValidationUtil.checkPrivateFieldAccess(fieldName, isStatic, type, ctx)) {
        ctx.addError(fe.range, "FieldDecl " + fieldName + " in " + type.name + " is not visible.");
        return false;
      }
      fe.expType =
          type.resolveType(
              ctx,
              field.type.resolvedType,
              MapExt.fromIterable(
                  field.type.resolvedType.typeVars,
                  (i) -> {
                    return i.name;
                  },
                  (k) -> {
                    return k;
                  }));
      fe.resolvedMember = field;
      fe.evalType = (isStatic ? EvalType.STATIC_FIELD : EvalType.FIELD);
      fe.fieldClass = type.cls;
      ctx.addFieldUsege(type, field);
      return true;
    }
    MethodDecl method = type.findMethodByName(ctx, fieldName, fe.setter, !fe.setter);
    if (method == null) {
      method = type.findMethodByName(ctx, fieldName, false, false);
      if (method == null) {
        return false;
      }
      if (method.staticValue != isStatic) {
        String error;
        if (method.staticValue) {
          error =
              "Static method '"
                  + fe.name
                  + "' in type '"
                  + type.name
                  + " must be accessed in a static way.";
        } else {
          error =
              "Cannot make a static reference to non-static method '"
                  + fe.name
                  + "' in type '"
                  + type.name
                  + "'.";
        }
        ctx.addError(fe.range, error);
        return false;
      }
      fe.resolvedMember = method;
      fe.evalType = EvalType.METHOD_REFERENCE;
      fe.fieldClass = type.cls;
      fe.expType = type.resolveType(ctx, method.asLambdaType(), MapExt.Map());
    } else {
      fe.evalType = EvalType.GETTER;
      fe.resolvedMember = method;
      fe.fieldClass = type.cls;
      fe.expType =
          type.resolveType(
              ctx,
              method.getter
                  ? method.returnType.resolvedType
                  : ListExt.first(method.allParams).dataType.resolvedType,
              MapExt.Map());
    }
    ctx.addMethodUsege(type, method);
    return true;
  }

  public static boolean checkOnLibraryFunction(FieldOrEnumExpression fe, ValidationContext ctx) {
    String fieldName = fe.name;
    FieldDecl field = ctx.getLibraryField(fieldName);
    if (field != null && (field.type != null)) {
      fe.expType = field.type.resolvedType;
      fe.resolvedMember = field;
      fe.evalType = EvalType.GLOBAL_FIELD;
      return true;
    }
    MethodDecl method = ctx.getLibraryMethod(fieldName);
    if (method == null) {
      return false;
    }
    if (method != null && !method.getter) {
      fe.resolvedMember = method;
      fe.evalType = EvalType.GLOBAL_METHOD_REFERENCE;
      fe.expType = method.asLambdaType();
    } else {
      fe.evalType = EvalType.GETTER;
      fe.resolvedMember = method;
      fe.expType = method.returnType.resolvedType;
    }
    return true;
  }

  public static void validateForEachLoop(ForEachLoop on, ValidationContext context) {
    ValidationContext ctx = context.createSharedSub();
    ExpressionValidationUtil.validateAbstractForEachLoop(on, ctx);
    if (on.body != null) {
      ExpressionValidationUtil.validate(on.body, ctx);
    } else {
      ctx.addWarn(null, "Empty for loop found");
    }
  }

  public static void validateForLoop(ForLoop on, ValidationContext context) {
    ValidationContext ctx = context.createSharedSub();
    PropType bool = ctx.bool();
    ctx.addAttribute("#c", true);
    ctx.addAttribute("#b", true);
    if (on.decl != null) {
      ExpressionValidationUtil.validate(on.decl, ctx);
    }
    on.inits.forEach(
        (e) -> {
          ExpressionValidationUtil.validate(e, ctx);
        });
    if (on.test == null) {
      ctx.addError(null, "Test expression is required");
    } else {
      ExpressionValidationUtil.validate(on.test, ctx);
      PropType testType = ctx.typeOrObject(on.test);
      if (!testType.isBool()) {
        ctx.addError(on.test.range, "Can not convert Boolean from \"" + testType.toString() + "\"");
      }
    }
    on.resets.forEach(
        (e) -> {
          ExpressionValidationUtil.validate(e, ctx);
        });
    if (on.body == null) {
      ctx.addWarn(null, "Found empty body");
    } else {
      ExpressionValidationUtil.validate(on.body, ctx);
    }
  }

  public static void validateIfStatement(IfStatement on, ValidationContext ctx) {
    if (on.test == null) {
      ctx.addError(null, "Condition is required");
    } else {
      ExpressionValidationUtil.validate(on.test, ctx);
      PropType type = ctx.typeOrObject(on.test);
      if (!type.isBool()) {
        ctx.addError(on.test.range, "Can not convert Boolean from \"" + type.toString() + "\"");
      }
    }
    if (on.thenStatement != null) {
      ExpressionValidationUtil.validate(on.thenStatement, ctx);
    } else {
      ctx.addError(null, "If body is required");
    }
    if (on.elseStatement != null) {
      ExpressionValidationUtil.validate(on.elseStatement, ctx);
    }
    on.returnType = ExpressionValidationUtil.computeReturnType(on, ctx);
  }

  public static void validateLambdaExpression(LambdaExpression on, ValidationContext ctx) {
    on.expType = null;
    PropType ct = ctx.getExpectedType();
    LambdaType expType = ct.findLambdaFunction();
    if (!(expType instanceof LambdaType)) {
      ctx.addError(null, "Function not found.");
      return;
    }
    if (ListExt.length(expType.params) != ListExt.length(on.params)) {
      ctx.addError(
          null,
          "Arguments size is not matched with function parameters. "
              + IntegerExt.toString(ListExt.length(expType.params)));
    }
    PropType rt = ct.resolveType(ctx, expType.returnType, MapExt.Map());
    ValidationContext subCtx = ctx.subWithType(rt);
    subCtx.insideLambda = true;
    subCtx.addAttribute("#b", null);
    subCtx.addAttribute("#c", null);
    /*
    subCtx.addLocalVar(ct, "#a", false);
    */
    CollectionsUtil.forEach(
        expType.params,
        on.params,
        (p, a) -> {
          PropType paramType = ct.resolveType(subCtx, p, MapExt.Map());
          ExpressionValidationUtil.validateParam(a, subCtx, paramType);
        });
    if (on.expression != null) {
      ExpressionValidationUtil.validate(on.expression, subCtx);
      PropType returnType = subCtx.typeOrObject(on.expression);
      if (!(Objects.equals(rt, PropType.VOID)) && !rt.isAssignableFrom(returnType)) {
        subCtx.addError(
            on.expression.range,
            "Function return type \""
                + returnType.toString()
                + "\" is not matched with \""
                + rt.toString()
                + "\"");
      }
    } else if (on.body != null) {
      subCtx.addLocalVar(ctx.bool(), "#r", false);
      ExpressionValidationUtil.validate(on.body, subCtx);
      PropType returnType = ExpressionValidationUtil.computeReturnType(on.body, subCtx);
      if (returnType == null) {
        if (!(Objects.equals(rt.name, PropType.VOID_TYPE))) {
          subCtx.addError(null, "Function must return a result of type \"" + rt.toString() + "\"");
        }
      }
    }
    on.expectedType = expType;
    on.expType = expType;
  }

  public static PropType createLambdaExpType(
      ValidationContext subCtx, MethodDecl resultMethod, PropType ct) {
    LambdaType res = LambdaType.withMethod(resultMethod);
    ct.typeVars.forEach(
        (i) -> {
          res.typeVars.add(i);
        });
    if (ct instanceof ParameterizedType) {
      subCtx.setExpectedType(resultMethod.returnType.resolvedType);
      Map<String, PropType> arguments = resultMethod.createTypeArguments(subCtx, ListExt.List(0l));
      ParameterizedType dpt = (((ParameterizedType) ct));
      ParameterizedType pt = ParameterizedType.from(res);
      dpt.arguments.forEach(
          (t) -> {
            pt.addArgumentWithName(t.name, t.type);
          });
      ListExt.where(
              resultMethod.generics.params,
              (t) -> {
                return t.typeVar.isDependsOnReturn();
              })
          .forEach(
              (t) -> {
                pt.addArgumentWithName(t.name, res.resolveType(subCtx, t.typeVar, arguments));
              });
      return pt;
    } else {
      return res;
    }
  }

  public static void validateLiteralExpression(LiteralExpression on, ValidationContext ctx) {
    if (on.type == LiteralType.TypeString) {
      on.expType = ctx.getType("String");
    } else if (on.type == LiteralType.TypeBoolean) {
      on.expType = ctx.getType("Boolean");
    } else if (on.type == LiteralType.TypeInteger) {
      on.expType = ctx.getType("Integer");
    } else if (on.type == LiteralType.TypeDouble) {
      on.expType = ctx.getType("Double");
    }
  }

  public static void validateMethodCall(MethodCall mc, ValidationContext ctx) {
    ExpressionValidationUtil.validateMethodCallInternal(mc, ctx);
  }

  public static void validateMethodCallInternal(MethodCall mc, ValidationContext ctx) {
    mc.expType = null;
    mc.callType = MethodCallType.ERROR;
    if (mc.name == null) {
      ctx.addError(mc.range, "MethodDecl name is required");
      return;
    }
    if (mc.on == null) {
      PropType type;
      if (Objects.equals(mc.name, "super")) {
        LocalVar _super = ctx.findLocalVar("#super");
        if (_super != null) {
          type = _super.type.extendsValue;
        } else {
          type = null;
        }
      } else {
        type = ctx.getType(mc.name);
      }
      if (type != null) {
        MethodDecl constructor = ExpressionValidationUtil.findMethod(ctx, mc, type);
        if (constructor != null) {
          mc.expType = ExpressionValidationUtil.validateArgs(mc, ctx, type, constructor);
          mc.callType = MethodCallType.Constructor;
          ctx.addUsedType(mc.expType);
          mc.resolvedMethod = constructor;
          ctx.addMethodUsege(type, constructor);
          return;
        } else {
          ctx.addError(
              mc.range,
              "The constructor in the type \""
                  + type.toString()
                  + "\" is not applicable for the arguments "
                  + ExpressionValidationUtil.getParamsError(mc, ctx));
        }
      } else {
        LocalVar var = ctx.findLocalVar(mc.name);
        if (var != null && var.initialized) {
          PropType fieldType = ctx.getLocalVarType(mc.name);
          LambdaType function = fieldType == null ? null : fieldType.findLambdaFunction();
          if (function != null) {
            if (false) {
              /*
               FIXME
              */
              mc.callType = MethodCallType.FunctionMethod;
              mc.expType = ExpressionValidationUtil.validateArgs(mc, ctx, fieldType, null);
              ctx.addUsedType(mc.expType);
              mc.staticClass = function;
            } else {
              ctx.addError(
                  mc.range,
                  "The method \""
                      + fieldType.name
                      + "\" is not applicable for the arguments "
                      + ExpressionValidationUtil.getParamsError(mc, ctx));
            }
            return;
          }
        } else {
          LocalVar _this = ctx.findLocalVar("this");
          if (_this != null) {
            if (ExpressionValidationUtil.validateOnMethod(mc, ctx, _this.type)) {
              return;
            }
          }
          if (ExpressionValidationUtil.findStaticMethod(mc, ctx, ctx.getDataType())) {
            return;
          }
        }
        ctx.addError(mc.range, "Invalid method/type \"" + mc.name.toString() + "\"");
      }
    } else {
      if (mc.on instanceof FieldOrEnumExpression) {
        FieldOrEnumExpression typeExp = ((FieldOrEnumExpression) mc.on);
        PropType propType = ExpressionValidationUtil.evalStaticType(typeExp, ctx);
        if (propType != null) {
          ctx.addTypeUsage(propType);
          if (!ExpressionValidationUtil.findStaticMethod(mc, ctx, propType)) {
            MethodDecl constructor = ExpressionValidationUtil.findConstructor(mc, ctx, propType);
            if (constructor != null) {
              mc.expType = ExpressionValidationUtil.validateArgs(mc, ctx, propType, constructor);
              mc.callType = MethodCallType.FactoryConstructor;
              mc.resolvedMethod = constructor;
              mc.staticClass =
                  mc.expType instanceof ParameterizedType
                      ? (((ParameterizedType) mc.expType)).baseType
                      : mc.expType;
              ctx.addMethodUsege(propType, constructor);
              ctx.addUsedType(mc.expType);
              return;
            }
            ctx.addError(
                mc.range,
                "The static method \""
                    + mc.name
                    + "\" in the type \""
                    + propType.toString()
                    + "\" is not applicable for the arguments \" "
                    + ExpressionValidationUtil.getParamsError(mc, ctx));
          }
          return;
        }
      }
      ExpressionValidationUtil.validate(mc.on, ctx);
      PropType type = ctx.typeOrObject(mc.on);
      if (!ExpressionValidationUtil.validateOnMethod(mc, ctx, type)) {
        ctx.addError(
            mc.range,
            "The method \""
                + mc.name
                + "\" in the type \""
                + type.toString()
                + "\" is not applicable for the arguments "
                + ExpressionValidationUtil.getParamsError(mc, ctx));
      }
    }
  }

  public static String getParamsError(MethodCall mc, ValidationContext ctx) {
    List<PropType> positioanlTypes =
        IterableExt.toList(
            ListExt.map(
                mc.positionArgs,
                (p) -> {
                  return ctx.typeOrObject(p.arg);
                }),
            false);
    StringBuilder b = StringBuilderExt.StringBuffer("");
    StringBuilderExt.write(b, "(");
    if (!positioanlTypes.isEmpty()) {
      StringBuilderExt.write(b, ListExt.get(positioanlTypes, 0l));
      for (long i = 1l; i < ListExt.length(positioanlTypes); i++) {
        StringBuilderExt.write(b, ", ");
        StringBuilderExt.write(b, ListExt.get(positioanlTypes, i));
      }
    }
    if (!mc.namedArgs.isEmpty()) {
      if (!positioanlTypes.isEmpty()) {
        StringBuilderExt.write(b, ", ");
      }
      StringBuilderExt.write(b, ListExt.get(mc.namedArgs, 0l));
      for (long i = 1l; i < ListExt.length(mc.namedArgs); i++) {
        StringBuilderExt.write(b, ", ");
        StringBuilderExt.write(b, ListExt.get(mc.namedArgs, i));
      }
    }
    StringBuilderExt.write(b, ")");
    return b.toString();
  }

  public static MethodDecl findConstructor(
      MethodCall mc, ValidationContext ctx, PropType propType) {
    String value = mc.name;
    if (Objects.equals(value, "super")) {
      value = propType.name;
    }
    return propType.findMethod(ctx, value);
  }

  public static MethodDecl findMethod(ValidationContext ctx, MethodCall mc, PropType propType) {
    return propType.findMethod(ctx, mc.name);
  }

  public static boolean findStaticMethod(MethodCall mc, ValidationContext ctx, PropType type) {
    MethodDecl method = ExpressionValidationUtil.findMethod(ctx, mc, type);
    if (method != null && method.staticValue) {
      mc.expType = ExpressionValidationUtil.validateArgs(mc, ctx, type, method);
      mc.callType = MethodCallType.StaticMethod;
      mc.resolvedMethod = method;
      ctx.addMethodUsege(type, method);
      return true;
    } else {
      return false;
    }
  }

  public static boolean validateOnMethod(MethodCall mc, ValidationContext ctx, PropType type) {
    MethodDecl method = ExpressionValidationUtil.findMethod(ctx, mc, type);
    if (method != null && !method.staticValue) {
      if (method.getter) {
        return false;
      }
      mc.expType = ExpressionValidationUtil.validateArgs(mc, ctx, type, method);
      mc.callType = MethodCallType.InstanceMethod;
      mc.resolvedMethod = method;
      ctx.addMethodUsege(type, method);
      return true;
    } else {
      FieldDecl field = type.getField(mc.name);
      if (field != null) {
        PropType fieldType = field.type.resolvedType;
        LambdaType function = fieldType == null ? null : fieldType.findLambdaFunction();
        if (function != null) {
          if (false) {
            /*
            FIXME
            */
            mc.expType = ExpressionValidationUtil.validateArgs(mc, ctx, fieldType, null);
            mc.callType = MethodCallType.FunctionMethod;
            /*
             mc.isInstanceFunction = true;
             mc.resolvedMethod = lambdaFunction;
             mc.declaringClass = function;
             mc.staticClass = function;
            */
            return true;
          } else {
            ctx.addError(
                mc.range,
                "' The MethodDecl '"
                    + fieldType.name
                    + "' is not applicable for the arguments '"
                    + ExpressionValidationUtil.getParamsError(mc, ctx));
          }
        }
      }
      return false;
    }
  }

  public static PropType validateArgs(
      MethodCall mc, ValidationContext ctx, PropType propType, MethodDecl exe) {
    if (ListExt.isNotEmpty(mc.typeArgs)) {
      ExpressionValidationUtil.validateTypeArguments(
          mc.typeArgs, ctx, ExpressionValidationUtil.getTypeVars(mc, propType, exe));
    }
    if (ListExt.length(mc.positionArgs)
        > ListExt.length(exe.params.positionalParams) + ListExt.length(exe.params.optionalParams)) {
      ctx.addError(mc.range, "Too many positional paramters");
      return null;
    }
    if (ListExt.length(mc.positionArgs) < ListExt.length(exe.params.positionalParams)) {
      ctx.addError(mc.range, "Too few positional paramters");
      return null;
    }
    boolean hasRequiredErrors = false;
    for (MethodParam np : exe.params.namedParams) {
      if (np.required) {
        boolean notFound = true;
        for (NamedArgument na : mc.namedArgs) {
          if (na.name != null && np.name.equals(na.name)) {
            notFound = false;
            break;
          }
        }
        if (notFound) {
          hasRequiredErrors = true;
          ctx.addError(mc.range, "Parameter " + np.name + " is required");
        }
      }
    }
    if (hasRequiredErrors) {
      return null;
    }
    List<PropType> typesArray = exe.createTypesArray(ctx, propType);
    MethodDecl dummy =
        new MethodDecl(
            ListExt.List(),
            null,
            null,
            false,
            null,
            false,
            false,
            null,
            false,
            null,
            false,
            null,
            "",
            false,
            null,
            null,
            false,
            false);
    /*
     dummy.generics.params = List.from(exe.generics.typeVars);
    */
    dummy.returnType = exe.returnType;
    List<MethodParam> positionalParams = exe.params.positionalParams;
    long i = 0l;
    for (long j = 0l; i < ListExt.length(positionalParams); i++) {
      MethodParam param = ListExt.get(positionalParams, i);
      Argument arg = ListExt.get(mc.positionArgs, i);
      PropType resolveType =
          propType.resolveType(
              ctx, param.dataType.resolvedType, dummy.createTypeArguments(ctx, typesArray));
      ValidationContext sub = ctx.subWithType(resolveType);
      ExpressionValidationUtil.validateArgument(arg, sub);
      PropType type = sub.typeOrObject(arg.arg);
      dummy.params.positionalParams.add(
          new MethodParam(ListExt.List(), type.toDataType(), null, false, param.name, false, null));
      if (!resolveType.isAssignableFrom(type)) {
        ctx.addError(null, "Can not assign " + type.toString() + " to " + resolveType.toString());
      }
    }
    /*
     Only one argument on Criteria method
    */
    boolean onCriteria =
        (exe != null && Objects.equals(exe.name, "on"))
            && (propType != null && Objects.equals(propType.name, "Criteria"));
    if (onCriteria) {
      if (ListExt.isNotEmpty(mc.positionArgs)) {
        Argument arg = ListExt.first(mc.positionArgs);
        Expression exp = arg.arg;
        boolean invalid = false;
        if (!(exp instanceof FieldOrEnumExpression)) {
          invalid = true;
        } else {
          FieldOrEnumExpression fe = ((FieldOrEnumExpression) exp);
          if (!(Objects.equals(fe.name, "typeIdx"))) {
            invalid = true;
          }
        }
        if (invalid) {
          ctx.addError(null, "Wrong expression. Expected '<Model>.typeIdx'");
        }
      }
    }
    List<MethodParam> optionalParams = exe.params.optionalParams;
    for (long j = 0l; i < ListExt.length(mc.positionArgs); j++, i++) {
      MethodParam param = ListExt.get(optionalParams, j);
      Argument arg = ListExt.get(mc.positionArgs, i);
      PropType resolveType =
          propType.resolveType(
              ctx, param.dataType.resolvedType, dummy.createTypeArguments(ctx, typesArray));
      ValidationContext sub = ctx.subWithType(resolveType);
      ExpressionValidationUtil.validateArgument(arg, sub);
      PropType type = sub.typeOrObject(arg.arg);
      dummy.params.optionalParams.add(
          new MethodParam(ListExt.List(), type.toDataType(), null, false, param.name, false, null));
      if (!resolveType.isAssignableFrom(type)) {
        ctx.addError(null, "Can not assign " + type.toString() + " to " + resolveType.toString());
      }
    }
    List<MethodParam> namedParams = exe.params.namedParams;
    for (NamedArgument arg : mc.namedArgs) {
      MethodParam param =
          ListExt.firstWhere(
              namedParams,
              (f) -> {
                return Objects.equals(f.name, arg.name);
              },
              null);
      if (param != null) {
        PropType resolveType =
            propType.resolveType(
                ctx, param.dataType.resolvedType, dummy.createTypeArguments(ctx, typesArray));
        ValidationContext sub = ctx.subWithType(resolveType);
        ExpressionValidationUtil.validateNamedArgument(arg, sub);
        PropType type = sub.typeOrObject(arg.value);
        dummy.params.namedParams.add(
            new MethodParam(
                ListExt.List(), type.toDataType(), null, false, param.name, false, null));
        if (!resolveType.isAssignableFrom(type)) {
          ctx.addError(null, "Can not assign " + type.toString() + " to " + resolveType.toString());
        }
      } else {
        ctx.addError(null, "Unknown named param " + arg.name);
      }
    }
    if (dummy.returnType != null) {
      if (mc.callType == MethodCallType.FunctionMethod) {
        dummy.generics.params.clear();
      }
      PropType resolveType =
          propType.resolveType(
              ctx, exe.returnType.resolvedType, dummy.createTypeArguments(ctx, typesArray));
      return resolveType;
    }
    return propType.applyArgs(typesArray);
  }

  public static List<TypeVariable> getTypeVars(MethodCall mc, PropType propType, MethodDecl exe) {
    List<TypeVariable> list =
        ListExt.from(
            ListExt.map(
                exe.generics.params,
                (t) -> {
                  return t.typeVar;
                }),
            false);
    return list;
  }

  public static void validateMethodParams(
      MethodParams on, ValidationContext ctx, PropType declType) {
    on.positionalParams.forEach(
        (p) -> {
          ExpressionValidationUtil.validateMethodParam(p, ctx, declType);
        });
    on.namedParams.forEach(
        (p) -> {
          ExpressionValidationUtil.validateMethodParam(p, ctx, declType);
        });
    on.optionalParams.forEach(
        (p) -> {
          ExpressionValidationUtil.validateMethodParam(p, ctx, declType);
        });
    Set<String> names = SetExt.Set();
    List<MethodParam> list$ = ListExt.<MethodParam>List();
    {
      list$.addAll(on.positionalParams);
      list$.addAll(on.namedParams);
      list$.addAll(on.optionalParams);
    }
    ListExt.map(
            list$,
            (a) -> {
              return a;
            })
        .forEach(
            (p) -> {
              if (!names.add(p.name)) {
                ctx.addError(null, "Duplicate parameter name found \"" + p.name + "\"");
              }
            });
  }

  public static void validateMethodParam(MethodParam on, ValidationContext ctx, PropType declType) {
    if (on.dataType != null) {
      ExpressionValidationUtil.validateDataType(on.dataType, ctx);
    }
    if (on.name == null) {
      ctx.addError(null, "Parameter name is required");
    }
    /*
     ReservedWords.checkReserved(on.name, ctx, null);
     if (on.params != null) {
         ctx.addError(null, 'FunctionParamer is not supporting');
         validateMethodParams(on.params, ctx, declType);
     }
    */
    if (on.defaultValue != null) {
      ExpressionValidationUtil.validate(on.defaultValue, ctx);
    }
    PropType type = ExpressionValidationUtil.addField(on, ctx, declType);
    if (on.dataType == null) {
      on.dataType = type.toDataType();
    }
  }

  public static PropType addField(MethodParam on, ValidationContext ctx, PropType declType) {
    PropType propType = MethodParamUtil.type(on, ctx, declType);
    if (on.dataType == null
        && declType != null
        && (on.thisToken != null && Objects.equals(on.thisToken, "this"))) {
      /*
       We assign Object in this case
      */
      FieldDecl existing =
          ListExt.firstWhere(
              declType.getAllFields(),
              (f) -> {
                return Objects.equals(f.name, on.name);
              },
              null);
      if (existing == null) {
        ctx.addError(
            null, "FieldDecl \"" + on.name + "\" not found in type \"" + declType.name + "\"");
        return null;
      }
      if (existing != null && existing.type != null) {
        propType = existing.type.resolvedType;
      }
    }
    ctx.addLocalVar(propType, on.name, false);
    return propType;
  }

  public static void validateNamedArgument(NamedArgument on, ValidationContext ctx) {
    if (on.value != null) {
      ExpressionValidationUtil.validate(on.value, ctx);
    }
  }

  public static void validateWithType(NamedArgument on, ValidationContext ctx, PropType _this) {
    if (ExpressionValidationUtil.getName(on) == null) {
      ctx.addError(null, "ModelProperty name is required");
      return;
    }
    FieldDecl prop = _this.getField(ExpressionValidationUtil.getName(on));
    if (prop == null) {
      ctx.addError(null, "Invalid property \"" + on.name.toString() + "\"");
      return;
    }
    ValidationContext sub = ctx.subWithField(prop);
    ExpressionValidationUtil.validate(on.value, sub);
    PropType type = sub.typeOrObject(on.value);
    PropType fieldType = prop.type.resolvedType;
    if (!fieldType.isAssignableFrom(type)) {
      ctx.addError(
          on.value.range,
          "Type mismatch: cannot convert from " + type.toString() + " to " + fieldType.toString());
    }
  }

  public static String getName(NamedArgument on) {
    return on.name == null ? null : on.name;
  }

  public static void validateParam(Param on, ValidationContext ctx, PropType type) {
    if (on.name != null && ctx.hasLocalVar(on.name)) {
      ctx.addError(null, "Duplicate argument name \"" + on.name.toString() + "\"");
    }
    PropType varType = type;
    if (on.type != null) {
      PropType argType = ctx.typeOrObjectData(on.type);
      if (argType == null) {
        ctx.addError(null, "Invalid data type \"" + on.type.toString() + "\"");
      } else {
        if (!argType.isAssignableFrom(varType)) {
          ctx.addError(
              null,
              "Can not convert \"" + argType.toString() + "\" to \"" + varType.toString() + "\"");
        }
        varType = argType;
      }
    }
    if (on.name != null) {
      ctx.addLocalVar(varType, on.name, null);
    }
  }

  public static void validateParExpression(ParExpression on, ValidationContext ctx) {
    if (on.exp != null) {
      ExpressionValidationUtil.validate(on.exp, ctx);
    }
    on.expType = ctx.typeOrObject(on.exp);
  }

  public static void validatePostfixExpression(PostfixExpression pe, ValidationContext ctx) {
    pe.expType = null;
    if (pe.on == null) {
      ctx.addError(pe.range, "Expression is required");
      return;
    }
    if (pe.postfix == null) {
      ctx.addError(pe.range, "Postfix is required");
      return;
    }
    if (!(pe.on instanceof FieldOrEnumExpression)) {
      ctx.addError(pe.range, "Invalid operator");
      return;
    }
    ExpressionValidationUtil.validate(pe.on, ctx);
    PropType propType = ctx.typeOrObject(pe.on);
    if (!propType.isNumber()) {
      ctx.addError(null, "Expression type should be number type.");
    }
    pe.expType = ctx.typeOrObject(pe.on);
    switch (pe.postfix) {
      case "++":
      case "--":
        {
          return;
        }
      default:
        {
          ctx.addError(pe.range, "Invalid operator \"" + pe.postfix + "\"");
        }
    }
  }

  public static void validatePrefixExpression(PrefixExpression pe, ValidationContext ctx) {
    pe.expType = null;
    if (pe.on == null) {
      ctx.addError(pe.range, "Expression is required");
      return;
    }
    if (pe.prefix == null) {
      ctx.addError(pe.range, "Prefix is required");
      return;
    }
    if (Objects.equals(pe.prefix, "++") || Objects.equals(pe.prefix, "--")) {
      if (!(pe.on instanceof FieldOrEnumExpression)) {
        ctx.addError(pe.range, "Invalid operator");
        return;
      }
    }
    ExpressionValidationUtil.validate(pe.on, ctx);
    PropType propType = ctx.typeOrObject(pe.on);
    if (Objects.equals(pe.prefix, "!")) {
      if (!propType.isBool()) {
        ctx.addError(null, "Expression type should be bool type.");
      }
    } else if (!propType.isNumber()) {
      ctx.addError(null, "Expression type should be number type.");
    }
    pe.expType = ctx.typeOrObject(pe.on);
    switch (pe.prefix) {
      case "+":
      case "-":
      case "++":
      case "--":
      case "~":
      case "!":
        {
          return;
        }
      default:
        {
          ctx.addError(pe.range, "Invalid operator \"" + pe.prefix + "\"");
        }
    }
  }

  public static void validateReturn(Return on, ValidationContext ctx) {
    LocalVar localVar = ctx.findLocalVar("#r");
    if (localVar == null) {
      ctx.addError(null, "Invalid return statment");
      return;
    }
    if (on.expression != null) {
      ExpressionValidationUtil.validate(on.expression, ctx);
    }
    PropType type = ExpressionValidationUtil.computeReturnType(on, ctx);
    if (on.expression != null && Objects.equals(type, PropType.VOID)) {
      ctx.addError(null, "Cannot return a void result");
      return;
    }
    PropType expectedType = ctx.getExpectedType();
    boolean success = expectedType != null;
    if (success) {
      success = expectedType.isAssignableFrom(type);
      if (!success) {
        LocalVar asyncVar = ctx.findLocalVar("#a");
        if (asyncVar != null && (((Boolean) asyncVar.value))) {
          if (ctx.getType("Future").isAssignableFrom(type)) {
            success = expectedType.isAssignableFrom(type.elementTypeWithIndex(0l));
          } else {
            success = expectedType.isAssignableFrom(type);
          }
        }
      }
    }
    if (type != null && !success) {
      ctx.addError(
          null,
          "Can not convert from " + type.toString() + " to " + ctx.getExpectedType().toString());
    }
    on.returnType = ExpressionValidationUtil.computeReturnType(on, ctx);
  }

  public static void validateSwitchCaseBlock(
      SwitchCaseBlock on, ValidationContext ctx, PropType testType) {
    boolean isEnum = testType.enm != null;
    ValidationContext testCtx = ctx.subWithType(testType);
    on.tests.forEach(
        (t) -> {
          ExpressionValidationUtil.validate(t, testCtx);
        });
    for (Expression t : on.tests) {
      PropType tt = testCtx.typeOrObject(t);
      if (testType.canCompare(tt)) {
        continue;
      }
      if (isEnum) {
        ctx.addError(t.range, "Enum value not found in " + testType.toString());
        continue;
      }
      ctx.addError(t.range, "Can not compare " + tt.toString() + " with " + testType.toString());
    }
    ValidationContext sub = ctx.createSharedSub();
    on.statements.forEach(
        (s) -> {
          ExpressionValidationUtil.validate(s, sub);
        });
  }

  public static void validateSwitchExpression(SwitchExpression se, ValidationContext ctx) {
    if (se.on == null) {
      ctx.addError(se.range, "Switch test is required");
    } else {
      ExpressionValidationUtil.validate(se.on, ctx);
    }
    ValidationContext sub = ctx.subWithType(ctx.typeOrObject(se.on));
    /*
     se.cases.forEach((c) => validateCaseExpression(c, sub, se));
    */
    if (se.onElse != null) {
      ExpressionValidationUtil.validate(se.onElse, sub);
    } else {
      if (se.cases.isEmpty()) {
        ctx.addError(se.range, "Switch case should not be empty");
      }
    }
    List<PropType> allTypes =
        IterableExt.toList(
            ListExt.map(
                se.cases,
                (c) -> {
                  return sub.typeOrObject(c.result);
                }),
            false);
    if (se.onElse != null) {
      allTypes.add(sub.typeOrObject(se.onElse));
    }
    se.expType = sub.findSuperType(allTypes);
  }

  public static void validateSwitchStatement(SwitchStatement on, ValidationContext ctx) {
    PropType type;
    if (on.test != null) {
      ExpressionValidationUtil.validate(on.test, ctx);
      type = ctx.typeOrObject(on.test);
    } else {
      type = ctx.voidType();
    }
    ValidationContext sub = ctx.createSharedSub();
    PropType type$final = type;
    on.cases.forEach(
        (c) -> {
          ExpressionValidationUtil.validateSwitchCaseBlock(c, sub, type$final);
        });
    on.defaults.forEach(
        (d) -> {
          ExpressionValidationUtil.validate(d, sub);
        });
    on.returnType = ExpressionValidationUtil.computeReturnType(on, ctx);
  }

  public static void validateTerinaryExpression(TerinaryExpression on, ValidationContext ctx) {
    if (on.condition != null) {
      ExpressionValidationUtil.validate(on.condition, ctx);
      if (!ctx.typeOrObject(on.condition).isBool()) {
        ctx.addError(on.condition.range, "Condition should be of Boolean type");
      }
    } else {
      ctx.addError(null, "Condition is required");
    }
    if (on.ifTrue != null) {
      ExpressionValidationUtil.validate(on.ifTrue, ctx);
    } else {
      ctx.addError(null, "Then is required");
    }
    if (on.ifFalse != null) {
      ExpressionValidationUtil.validate(on.ifFalse, ctx);
    } else {
      ctx.addError(null, "Else is required");
    }
    on.expType = ctx.getCommonType(ctx.typeOrObject(on.ifTrue), ctx.typeOrObject(on.ifFalse));
  }

  public static void validateThrowStatement(ThrowStatement on, ValidationContext ctx) {
    ExpressionValidationUtil.validate(on.exp, ctx);
    on.returnType = ExpressionValidationUtil.computeReturnType(on, ctx);
  }

  public static void validateTryCatcheStatment(TryCatcheStatment on, ValidationContext ctx) {
    if (on.body != null) {
      ExpressionValidationUtil.validate(on.body, ctx.createSharedSub());
    } else {
      ctx.addError(null, "Try block is required");
    }
    on.catchParts.forEach(
        (c) -> {
          ExpressionValidationUtil.validateCatchPart(c, ctx);
        });
    if (on.finallyBody != null) {
      ExpressionValidationUtil.validate(on.finallyBody, ctx);
    } else if (on.catchParts.isEmpty()) {
      ctx.addError(null, "Catch/Finally block is required");
    }
    on.returnType = ExpressionValidationUtil.computeReturnType(on, ctx);
  }

  public static void validateTypeArguments(
      List<DataType> args, ValidationContext ctx, List<TypeVariable> typeVars) {
    args.forEach(
        (a) -> {
          ExpressionValidationUtil.validateDataType(a, ctx);
        });
    if (typeVars == null || ListExt.length(typeVars) == 0l) {
      ctx.addError(null, "The type arguments are not applicable here " + args.toString());
      return;
    }
    if (ListExt.length(typeVars) != ListExt.length(args)) {
      ctx.addError(
          null,
          "Incorrect number of arguments; it cannot be parameterized with arguments "
              + args.toString());
    } else {
      CollectionsUtil.forEach(
          args,
          typeVars,
          (a, p) -> {
            PropType val = a.resolvedType;
            if (!p.isAssignableFrom(val)) {
              ctx.addError(
                  null,
                  "Bound mismatch: The type "
                      + val.toString()
                      + " is not a valid substitute for the bounded parameter \""
                      + p.toString()
                      + "\"");
            }
          });
    }
  }

  public static void validateTypeCastOrCheckExpression(
      TypeCastOrCheckExpression on, ValidationContext ctx) {
    if (on.exp != null) {
      ExpressionValidationUtil.validate(on.exp, ctx);
    }
    if (on.dataType != null) {
      ExpressionValidationUtil.validateDataType(on.dataType, ctx);
      if (on.exp.expType.isAssignableFrom(on.dataType.resolvedType)) {
      } else if (!on.dataType.resolvedType.isAssignableFrom(on.exp.expType)) {
        ctx.addError(
            null,
            "Can not convert from "
                + on.dataType.resolvedType.toString()
                + " to "
                + on.exp.expType.toString());
      }
    } else {
      ctx.addError(null, "Type is required");
    }
    if (on.check) {
      on.expType = ctx.bool();
      return;
    }
    PropType tp = null;
    if (on.dataType != null) {
      tp = on.dataType.resolvedType;
    }
    if (tp == null) {
      on.expType = ctx.object();
      return;
    }
    if (!tp.typeVars.isEmpty()) {
      tp = ParameterizedType.from(tp);
    }
    on.expType = tp;
  }

  public static void validateTypeParams(TypeParams on, ValidationContext ctx) {
    if (on.params != null) {
      on.params.forEach(
          (p) -> {
            ExpressionValidationUtil.validateTypeParam(p, ctx);
          });
    }
  }

  public static void validateTypeParam(TypeParam on, ValidationContext ctx) {
    if (on.name != null) {
      PropType type = ctx.getType(on.name);
      if (type != null) {
        ctx.addWarn(
            null,
            "The type parameter '"
                + on.name.toString()
                + "' is hiding the type '"
                + type.toString()
                + "'");
      }
    } else {
      ctx.addError(null, "Type argument is required");
    }
    if (on.extendType != null) {
      ExpressionValidationUtil.validateDataType(on.extendType, ctx);
    }
  }

  public static void validateWhileLoop(WhileLoop on, ValidationContext ctx) {
    if (on.test == null) {
      ctx.addError(null, "Test expression is required");
    } else {
      ExpressionValidationUtil.validate(on.test, ctx);
      PropType testType = on.test.expType;
      if (!testType.isBool()) {
        ctx.addError(on.test.range, "Can not convert Boolean from '" + testType.toString() + "'");
      }
    }
    ValidationContext sub = ctx.createSharedSub();
    PropType bool = sub.bool();
    sub.addAttribute("#c", true);
    sub.addAttribute("#b", true);
    if (on.body == null) {
      sub.addWarn(null, "Found empty body");
    } else {
      ExpressionValidationUtil.validate(on.body, sub);
    }
    on.returnType = ExpressionValidationUtil.computeReturnType(on, ctx);
  }

  public static PropType computeReturnType(Statement on, ValidationContext ctx) {
    if (on instanceof Block) {
      return ExpressionValidationUtil.computeBlockReturnType(((Block) on), ctx);
    } else if (on instanceof Declaration) {
      return ExpressionValidationUtil.computeDeclarationReturnType(((Declaration) on), ctx);
    } else if (on instanceof IfStatement) {
      return ExpressionValidationUtil.computeIfStatementReturnType(((IfStatement) on), ctx);
    } else if (on instanceof MethodCall) {
      return ExpressionValidationUtil.computeMethodCallReturnType(((MethodCall) on), ctx);
    } else if (on instanceof PostfixExpression) {
      return ExpressionValidationUtil.computePostfixExpressionReturnType(
          ((PostfixExpression) on), ctx);
    } else if (on instanceof Return) {
      return ExpressionValidationUtil.computeReturnReturnType(((Return) on), ctx);
    } else if (on instanceof SwitchStatement) {
      return ExpressionValidationUtil.computeSwitchStatementReturnType(((SwitchStatement) on), ctx);
    } else if (on instanceof ThrowStatement) {
      return ExpressionValidationUtil.computeThrowStatementReturnType(((ThrowStatement) on), ctx);
    } else if (on instanceof TryCatcheStatment) {
      return ExpressionValidationUtil.computeTryCatcheStatmentReturnType(
          ((TryCatcheStatment) on), ctx);
    } else if (on instanceof WhileLoop) {
      return ExpressionValidationUtil.computeWhileLoopReturnType(((WhileLoop) on), ctx);
    }
    return null;
  }

  public static PropType computeWhileLoopReturnType(WhileLoop on, ValidationContext ctx) {
    return null;
  }

  public static PropType computeTryCatcheStatmentReturnType(
      TryCatcheStatment on, ValidationContext ctx) {
    if (on.body != null) {
      return ExpressionValidationUtil.computeReturnType(on.body, ctx);
    }
    return null;
  }

  public static PropType computeBlockReturnType(Block on, ValidationContext ctx) {
    for (long i = ListExt.length(on.statements) - 1l; i >= 0l; i--) {
      Statement last = ListExt.get(on.statements, i);
      return ExpressionValidationUtil.computeReturnType(last, ctx);
    }
    return null;
  }

  public static PropType computeDeclarationReturnType(Declaration on, ValidationContext ctx) {
    return null;
  }

  public static PropType computeIfStatementReturnType(IfStatement on, ValidationContext ctx) {
    if (on.elseStatement == null || on.thenStatement == null) {
      return null;
    }
    PropType trt = ExpressionValidationUtil.computeReturnType(on.thenStatement, ctx);
    PropType ert = ExpressionValidationUtil.computeReturnType(on.elseStatement, ctx);
    return trt == null || ert == null ? null : trt;
  }

  public static PropType computeMethodCallReturnType(MethodCall mc, ValidationContext ctx) {
    return null;
  }

  public static PropType computePostfixExpressionReturnType(
      PostfixExpression pe, ValidationContext ctx) {
    return null;
  }

  public static PropType computeReturnReturnType(Return on, ValidationContext ctx) {
    if (on.expression == null) {
      return ctx.voidType();
    }
    return ctx.typeOrObject(on.expression);
  }

  public static PropType computeSwitchCaseBlockReturnType(
      SwitchCaseBlock on, ValidationContext ctx) {
    if (on.statements.isEmpty()) {
      return null;
    }
    Statement last = ListExt.last(on.statements);
    PropType ret = ExpressionValidationUtil.computeReturnType(last, ctx);
    if (ret == null) {
      return null;
    }
    return ret;
  }

  public static PropType computeSwitchStatementReturnType(
      SwitchStatement on, ValidationContext ctx) {
    List<PropType> returns = ListExt.List(0l);
    for (SwitchCaseBlock c : on.cases) {
      PropType ret = ExpressionValidationUtil.computeSwitchCaseBlockReturnType(c, ctx);
      if (ret == null) {
        return null;
      }
      returns.add(ret);
    }
    if (on.defaults.isEmpty()) {
      return null;
    }
    Statement last = ListExt.last(on.defaults);
    PropType ret = ExpressionValidationUtil.computeReturnType(last, ctx);
    if (ret == null) {
      return null;
    }
    returns.add(ret);
    return ctx.findSuperType(returns);
  }

  public static PropType computeThrowStatementReturnType(ThrowStatement on, ValidationContext ctx) {
    return ctx.nullType();
  }

  public static void forEachInvolvedType(PropType type, Consumer<PropType> consumer) {
    if (type instanceof ParameterizedType) {
      ParameterizedType pt = ((ParameterizedType) type);
      PropType baseType = pt.baseType;
      consumer.accept(baseType);
      pt.arguments.forEach(
          (t) -> {
            ExpressionValidationUtil.forEachInvolvedType(t.type, consumer);
          });
    } else {
      consumer.accept(type);
    }
  }

  public static String listToString(List<PropType> paramTypes) {
    StringBuilder sb = StringBuilderExt.StringBuffer("");
    StringBuilderExt.write(sb, "(");
    if (paramTypes != null && !paramTypes.isEmpty()) {
      StringBuilderExt.write(sb, ListExt.get(paramTypes, 0l));
      for (long i = 1l; i < ListExt.length(paramTypes); i++) {
        StringBuilderExt.write(sb, " ");
        StringBuilderExt.write(sb, ListExt.get(paramTypes, i));
      }
    }
    StringBuilderExt.write(sb, ")");
    return sb.toString();
  }
}
