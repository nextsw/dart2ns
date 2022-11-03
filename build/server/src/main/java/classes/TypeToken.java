package classes;

import d3e.core.ListExt;
import d3e.core.MapExt;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TypeToken {
  public TypeKind kind;
  public String lit;
  public long lineNo = 0l;
  public long pos = 0l;
  public long len = 0l;
  public long index = 0l;
  public static long preAssign = 0l;
  public static long preCascade = 1l;
  public static long preCond = 2l;
  public static long preIfNull = 3l;
  public static long preLogicOr = 4l;
  public static long preLogicAnd = 5l;
  public static long preEqual = 6l;
  public static long preRational = 7l;
  public static long preBitOr = 8l;
  public static long preBitXor = 9l;
  public static long preBitAnd = 10l;
  public static long preShift = 11l;
  public static long preAdd = 12l;
  public static long preMulti = 13l;
  public static long preUnPostfix = 14l;
  public static long preUnPrefix = 15l;
  public static Map<TypeKind, String> tokenStrs = TypeToken.buildTokenStrs();
  public static Map<String, TypeKind> keywords = TypeToken.buildKeys();
  public static Map<TypeKind, Long> precedenceMap = TypeToken.buildPrecedence();
  public static List<TypeKind> infixes = TypeToken.buildInfixes();
  public static List<TypeKind> assignTokens =
      ListExt.asList(
          TypeKind.Assign,
          TypeKind.PlusAssign,
          TypeKind.MinusAssign,
          TypeKind.MultAssign,
          TypeKind.DivAssign,
          TypeKind.DoubleQuestionAssign,
          TypeKind.XorAssign,
          TypeKind.ModAssign,
          TypeKind.OrAssign,
          TypeKind.AndAssign,
          TypeKind.RightShiftAssign,
          TypeKind.LeftShiftAssign);

  public TypeToken() {}

  public static Map<String, TypeKind> buildKeys() {
    Map<String, TypeKind> res = MapExt.Map();
    for (TypeKind k : TypeKind.values()) {
      /*
       TODO: Put this back. We need to fix this for OptionSets in general.
       if(k._name.startsWith('key')){
       if(ParserUtil.isKeyKind(k)) {
      */
      String key = TypeToken.tokenStrs.get(k);
      MapExt.set(res, key, k);
      /*
       }
      */
    }
    return res;
  }

  public static Map<TypeKind, String> buildTokenStrs() {
    Map<TypeKind, String> res = MapExt.Map();
    MapExt.set(res, TypeKind.Unknown, "unknown");
    MapExt.set(res, TypeKind.Comma, ",");
    MapExt.set(res, TypeKind.Semicolon, ";");
    MapExt.set(res, TypeKind.Colon, ":");
    MapExt.set(res, TypeKind.Arrow, "=>");
    MapExt.set(res, TypeKind.Hash, "#");
    MapExt.set(res, TypeKind.At, "@");
    MapExt.set(res, TypeKind.Lcbr, "{");
    MapExt.set(res, TypeKind.Rcbr, "}");
    MapExt.set(res, TypeKind.Lpar, "(");
    MapExt.set(res, TypeKind.Rpar, ")");
    MapExt.set(res, TypeKind.Lsbr, "[");
    MapExt.set(res, TypeKind.Rsbr, "]");
    MapExt.set(res, TypeKind.Gt, ">");
    MapExt.set(res, TypeKind.Lt, "<");
    MapExt.set(res, TypeKind.CommentSingle, "// comment");
    MapExt.set(res, TypeKind.CommentMulti, "/* comment");
    MapExt.set(res, TypeKind.Dot, ".");
    return res;
  }

  public static Map<TypeKind, Long> buildPrecedence() {
    Map<TypeKind, Long> res = MapExt.Map();
    MapExt.set(res, TypeKind.Plus, TypeToken.preAdd);
    MapExt.set(res, TypeKind.Minus, TypeToken.preAdd);
    MapExt.set(res, TypeKind.Mul, TypeToken.preMulti);
    MapExt.set(res, TypeKind.Div, TypeToken.preMulti);
    MapExt.set(res, TypeKind.Mod, TypeToken.preMulti);
    MapExt.set(res, TypeKind.Xor, TypeToken.preBitXor);
    MapExt.set(res, TypeKind.Pipe, TypeToken.preBitOr);
    MapExt.set(res, TypeKind.Inc, TypeToken.preUnPostfix);
    MapExt.set(res, TypeKind.Dec, TypeToken.preUnPostfix);
    MapExt.set(res, TypeKind.Not, TypeToken.preUnPrefix);
    MapExt.set(res, TypeKind.And, TypeToken.preLogicAnd);
    MapExt.set(res, TypeKind.Or, TypeToken.preLogicOr);
    MapExt.set(res, TypeKind.Question, TypeToken.preCond);
    MapExt.set(res, TypeKind.DoubleQuestion, TypeToken.preIfNull);
    MapExt.set(res, TypeKind.LeftShift, TypeToken.preShift);
    MapExt.set(res, TypeKind.Assign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.PlusAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.MinusAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.DivAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.DoubleQuestionAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.MultAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.XorAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.ModAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.OrAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.AndAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.RightShiftAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.LeftShiftAssign, TypeToken.preAssign);
    MapExt.set(res, TypeKind.Lsbr, TypeToken.preUnPostfix);
    MapExt.set(res, TypeKind.Eq, TypeToken.preEqual);
    MapExt.set(res, TypeKind.Ne, TypeToken.preEqual);
    MapExt.set(res, TypeKind.Gt, TypeToken.preRational);
    MapExt.set(res, TypeKind.Lt, TypeToken.preRational);
    MapExt.set(res, TypeKind.Ge, TypeToken.preRational);
    MapExt.set(res, TypeKind.Le, TypeToken.preRational);
    MapExt.set(res, TypeKind.Dot, TypeToken.preUnPostfix);
    MapExt.set(res, TypeKind.Lpar, TypeToken.preUnPostfix);
    MapExt.set(res, TypeKind.DotDot, TypeToken.preCascade);
    MapExt.set(res, TypeKind.Ellipses, TypeToken.preUnPrefix);
    return res;
  }

  public static List<TypeKind> buildInfixes() {
    return ListExt.asList(
        TypeKind.Plus,
        TypeKind.Minus,
        TypeKind.Mod,
        TypeKind.Mul,
        TypeKind.Div,
        TypeKind.Eq,
        TypeKind.Ne,
        TypeKind.Gt,
        TypeKind.Lt,
        TypeKind.Ge,
        TypeKind.Le,
        TypeKind.Or,
        TypeKind.Xor,
        TypeKind.And,
        TypeKind.Pipe,
        TypeKind.Amp,
        TypeKind.LeftShift,
        TypeKind.DoubleQuestion);
  }

  public static boolean isAssign(TypeKind kind) {
    return TypeToken.assignTokens.contains(kind);
  }

  public long getPrecedence() {
    if (TypeToken.precedenceMap.containsKey(this.kind)) {
      return TypeToken.precedenceMap.get(this.kind);
    } else {
      if (this.kind == TypeKind.Name) {
        if (Objects.equals(this.lit, "is") || Objects.equals(this.lit, "as")) {
          return TypeToken.preRational;
        }
      }
      return 0l;
    }
  }

  public static TypeKind keyToToken(String key) {
    return TypeToken.keywords.get(key);
  }

  public static boolean isKey(String key) {
    return TypeToken.keyToToken(key) != null;
  }

  public static String keyToStr(TypeKind kind) {
    return TypeToken.tokenStrs.get(kind);
  }

  public String toString() {
    return TypeToken.keyToStr(this.kind) + " \"" + this.lit + "\"";
  }

  public boolean isScalar() {
    return this.kind == TypeKind.Number || this.kind == TypeKind.String;
  }

  public boolean isUnary() {
    return this.kind == TypeKind.Plus
        || this.kind == TypeKind.Minus
        || this.kind == TypeKind.Not
        || this.kind == TypeKind.BitNot
        || this.kind == TypeKind.Mul
        || this.kind == TypeKind.Amp;
  }

  public boolean isRelational() {
    return this.kind == TypeKind.Lt
        || this.kind == TypeKind.Le
        || this.kind == TypeKind.Gt
        || this.kind == TypeKind.Ge
        || this.kind == TypeKind.Eq
        || this.kind == TypeKind.Ne;
  }

  public static boolean isInfix(TypeKind kind) {
    return TypeToken.infixes.contains(kind);
  }

  public static boolean isPrefix(TypeKind kind) {
    return kind == TypeKind.Inc
        || kind == TypeKind.Dec
        || kind == TypeKind.Minus
        || kind == TypeKind.Plus;
  }

  public static boolean isPostfix(TypeKind kind) {
    return kind == TypeKind.Inc || kind == TypeKind.Dec;
  }
}
