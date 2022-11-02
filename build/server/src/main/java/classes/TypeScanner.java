package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.StringExt;
import java.util.Objects;
import java.util.function.Function;

public class TypeScanner {
  public static String singleQuote = "'";
  public static String doubleQuote = "\"";
  public static String numSep = "_";
  public long pos = 0l;
  public long lineNo = 0l;
  public long lastNlPos = 0l;
  public boolean isInsideString = false;
  public boolean isInterStart = false;
  public boolean isInterEnd = false;
  public String lineComment;
  public boolean isStarted = false;
  public long tidex = 0l;
  public String text;
  public String quote;
  public long noLines = 0l;
  public long eofs = 0l;
  public long savedPos = 0l;

  public TypeScanner(String text) {
    this.text = text;
  }

  public TypeToken scan() {
    while (true) {
      if (this.isStarted) {
        this.pos++;
      }
      this.isStarted = true;
      if (this.pos >= StringExt.length(this.text)) {
        return endOfFile();
      }
      if (!this.isInsideString) {
        skipWhiteSpace();
      }
      if (this.isInterEnd) {
        /*
         TODO
        */
      }
      skipWhiteSpace();
      if (this.pos >= StringExt.length(this.text)) {
        return endOfFile();
      }
      String c = StringExt.get(this.text, this.pos);
      String nextc = lookAhead(1l);
      if (ParserUtil.isNameChar(c)) {
        String name = identName();
        String nextChar = lookAhead(1l);
        TypeKind kind = TypeToken.keywords.get(name);
        if (kind != TypeKind.Unknown && kind != null) {
          return newToken(kind, name, StringExt.length(name));
        }
        if (this.isInsideString) {
          if (Objects.equals(nextChar, this.quote)) {
            this.isInterEnd = true;
            this.isInterStart = false;
            this.isInsideString = false;
          }
        }
        return newToken(TypeKind.Name, name, StringExt.length(name));
      } else if (ParserUtil.isDigit(c)
          || ((Objects.equals(c, ".") && ParserUtil.isDigit(nextc))
              || Objects.equals(c, "-") && ParserUtil.isDigit(nextc))) {
        if (!this.isInsideString) {
          long startPos = this.pos;
          while (startPos < StringExt.length(this.text)
              && Objects.equals(StringExt.get(this.text, startPos), "0")) {
            startPos++;
          }
          long prefixZeroNum = startPos - this.pos;
          if (startPos == StringExt.length(this.text)
              || (Objects.equals(c, "0")
                  && !ParserUtil.isDigit(StringExt.get(this.text, startPos)))) {
            prefixZeroNum--;
          }
          this.pos += prefixZeroNum;
        }
        String num = identNumber();
        return newToken(TypeKind.Number, num, StringExt.length(num));
      }
      switch (c) {
        case "+":
          {
            {
              if (Objects.equals(nextc, "+")) {
                this.pos++;
                return newToken(TypeKind.Inc, "++", 2l);
              } else if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.PlusAssign, "+=", 2l);
              }
              return newToken(TypeKind.Plus, "+", 1l);
            }
          }
        case "-":
          {
            {
              if (Objects.equals(nextc, "-")) {
                this.pos++;
                return newToken(TypeKind.Dec, "--", 2l);
              } else if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.MinusAssign, "-=", 2l);
              } else if (Objects.equals(nextc, ">")) {
                this.pos++;
                return newToken(TypeKind.Map, "->", 2l);
              }
              return newToken(TypeKind.Minus, "-", 1l);
            }
          }
        case "*":
          {
            {
              if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.MultAssign, "*=", 2l);
              }
              return newToken(TypeKind.Mul, "*", 1l);
            }
          }
        case "^":
          {
            {
              if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.XorAssign, "^=", 2l);
              }
              return newToken(TypeKind.Xor, "^", 1l);
            }
          }
        case "%":
          {
            {
              if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.ModAssign, "%=", 2l);
              }
              return newToken(TypeKind.Mod, "%", 1l);
            }
          }
        case "?":
          {
            {
              if (Objects.equals(nextc, "?")) {
                this.pos++;
                return newToken(TypeKind.DoubleQuestion, "??", 2l);
              }
              return newToken(TypeKind.Question, "?", 1l);
            }
          }
        case "(":
          {
            {
              return newToken(TypeKind.Lpar, "(", 1l);
            }
          }
        case ")":
          {
            {
              return newToken(TypeKind.Rpar, ")", 1l);
            }
          }
        case "[":
          {
            {
              return newToken(TypeKind.Lsbr, "[", 1l);
            }
          }
        case "]":
          {
            {
              return newToken(TypeKind.Rsbr, "]", 1l);
            }
          }
        case "{":
          {
            {
              if (this.isInsideString) {
                continue;
              }
              return newToken(TypeKind.Lcbr, "{", 1l);
            }
          }
        case "$":
          {
            {
              if (this.isInsideString) {
                return newToken(TypeKind.StrDollar, "$", 1l);
              }
              return newToken(TypeKind.Dollar, "", 1l);
            }
          }
        case "}":
          {
            {
              if (this.isInsideString) {
                this.pos++;
                if (Objects.equals(StringExt.get(this.text, this.pos), this.quote)) {
                  this.isInsideString = false;
                  return newToken(
                      TypeKind.String,
                      Objects.equals(this.quote, TypeScanner.singleQuote) ? "'" : "\"",
                      1l);
                }
                String identStr = identString();
                return newToken(TypeKind.String, identStr, StringExt.length(identStr) + 2l);
              } else {
                return newToken(TypeKind.Rcbr, "}", 1l);
              }
            }
          }
        case "'":
          {
            {
              String identStr = identString();
              return newToken(TypeKind.String, identStr, StringExt.length(identStr) + 2l);
            }
          }
        case "&":
          {
            {
              if (Objects.equals(nextc, "&")) {
                this.pos++;
                return newToken(TypeKind.And, "&&", 2l);
              }
              if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.AndAssign, "&=", 2l);
              }
              return newToken(TypeKind.Amp, "&", 1l);
            }
          }
        case "|":
          {
            {
              if (Objects.equals(nextc, "|")) {
                this.pos++;
                return newToken(TypeKind.Or, "||", 2l);
              }
              if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.OrAssign, "|=", 2l);
              }
              return newToken(TypeKind.Pipe, "|", 1l);
            }
          }
        case ",":
          {
            {
              return newToken(TypeKind.Comma, ",", 1l);
            }
          }
        case "@":
          {
            {
              return newToken(TypeKind.At, "@", 1l);
            }
          }
        case ".":
          {
            {
              if (Objects.equals(nextc, ".")) {
                this.pos++;
                if (Objects.equals(StringExt.get(this.text, this.pos), ".")) {
                  this.pos++;
                  return newToken(TypeKind.Ellipses, "...", 3l);
                }
                return newToken(TypeKind.DotDot, "..", 2l);
              }
              return newToken(TypeKind.Dot, ".", 1l);
            }
          }
        case "#":
          {
            {
              return newToken(TypeKind.Hash, "#", 1l);
            }
          }
        case ">":
          {
            {
              if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.Ge, ">=", 2l);
              } else if (Objects.equals(nextc, ">")) {
                if (this.pos + 2l < StringExt.length(this.text)
                    && Objects.equals(StringExt.get(this.text, this.pos + 2l), "=")) {
                  this.pos += 2l;
                  return newToken(TypeKind.RightShiftAssign, ">>=", 3l);
                }
                this.pos++;
                return newToken(TypeKind.RightShift, ">>", 2l);
              } else {
                return newToken(TypeKind.Gt, ">", 1l);
              }
            }
          }
        case "<":
          {
            {
              if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.Le, "<=", 2l);
              } else if (Objects.equals(nextc, "<")) {
                if (this.pos + 2l < StringExt.length(this.text)
                    && Objects.equals(StringExt.get(this.text, this.pos + 2l), "=")) {
                  this.pos += 2l;
                  return newToken(TypeKind.LeftShiftAssign, "<<=", 3l);
                }
                this.pos++;
                return newToken(TypeKind.LeftShift, "<<", 2l);
              } else {
                return newToken(TypeKind.Lt, "<", 1l);
              }
            }
          }
        case "=":
          {
            {
              if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.Eq, "==", 2l);
              } else if (Objects.equals(nextc, ">")) {
                this.pos++;
                return newToken(TypeKind.Arrow, "=>", 2l);
              } else {
                return newToken(TypeKind.Assign, "=", 1l);
              }
            }
          }
        case ":":
          {
            {
              return newToken(TypeKind.Colon, ":", 1l);
            }
          }
        case ";":
          {
            {
              return newToken(TypeKind.Semicolon, ";", 1l);
            }
          }
        case "!":
          {
            {
              if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.Ne, "!=", 2l);
              }
              return newToken(TypeKind.Not, "!", 1l);
            }
          }
        case "~":
          {
            {
              return newToken(TypeKind.BitNot, "~", 1l);
            }
          }
        case "/":
          {
            {
              if (Objects.equals(nextc, "=")) {
                this.pos++;
                return newToken(TypeKind.DivAssign, "/=", 2l);
              }
              if (Objects.equals(nextc, "/")) {
                long start = this.pos + 1l;
                ignoreLine();
                long commentLineEnd = this.pos;
                this.lineNo--;
                String comment = StringExt.substring(this.text, start, commentLineEnd);
                return newToken(TypeKind.CommentSingle, comment, StringExt.length(comment) + 2l);
              }
              if (Objects.equals(nextc, "*")) {
                long start = this.pos + 1l;
                while (true) {
                  this.pos++;
                  if (this.pos >= StringExt.length(this.text)) {
                    this.lineNo--;
                    error("Comment not terminated");
                  }
                  if (Objects.equals(StringExt.get(this.text, this.pos), "\n")) {
                    incLineNumber();
                    continue;
                  }
                  if (expect("*/", this.pos)) {
                    break;
                  }
                }
                String comment = StringExt.substring(this.text, start, this.pos);
                this.pos += 2l;
                return newToken(TypeKind.CommentMulti, comment, StringExt.length(comment) + 4l);
              }
              return newToken(TypeKind.Div, "/", 1l);
            }
          }
        default:
          {
          }
      }
      break;
    }
    return newToken(TypeKind.Eof, "", 1l);
  }

  public void ignoreLine() {
    eatToEndOfLine();
    incLineNumber();
  }

  public void eatToEndOfLine() {
    while (this.pos < StringExt.length(this.text)
        && !(Objects.equals(StringExt.get(this.text, this.pos), "\n"))) {
      this.pos++;
    }
  }

  public void error(String msg) {}

  public void incLineNumber() {
    this.lastNlPos = this.pos;
    this.lineNo++;
    if (this.lineNo > this.noLines) {
      this.noLines = this.lineNo;
    }
  }

  public boolean expect(String want, long startPos) {
    long endPos = startPos + StringExt.length(want);
    if (startPos < 0l || startPos >= StringExt.length(this.text)) {
      return false;
    }
    if (endPos < 0l || endPos > StringExt.length(this.text)) {
      return false;
    }
    for (long x = startPos; x < endPos; x++) {
      if (!(Objects.equals(StringExt.get(this.text, x), StringExt.get(want, x - startPos)))) {
        return false;
      }
    }
    return true;
  }

  public String identString() {
    String q = StringExt.get(this.text, this.pos);
    boolean isQuote =
        Objects.equals(q, TypeScanner.singleQuote) || Objects.equals(q, TypeScanner.doubleQuote);
    boolean isRaw =
        isQuote
            && (this.pos > 0l)
            && (Objects.equals(StringExt.get(this.text, this.pos - 1l), "r"));
    if (isQuote && !this.isInsideString) {
      this.quote = q;
    }
    long nCrChars = 0l;
    long start = this.pos;
    this.isInsideString = false;
    String slash = "\\";
    boolean escape = false;
    while (true) {
      this.pos++;
      if (this.pos >= StringExt.length(this.text)) {
        break;
      }
      String c = StringExt.get(this.text, this.pos);
      String prevc = StringExt.get(this.text, this.pos - 1l);
      if (!escape && Objects.equals(c, this.quote)) {
        break;
      }
      if (Objects.equals(c, slash)) {
        escape = !escape;
      } else {
        escape = false;
      }
      if (Objects.equals(c, "\r")) {
        nCrChars++;
      }
      if (Objects.equals(c, "\n")) {
        break;
      }
    }
    String lit = "";
    if (Objects.equals(StringExt.get(this.text, start), this.quote)) {
      start++;
    }
    long end = this.pos;
    if (this.isInsideString) {
      end++;
    }
    if (start <= this.pos) {
      String stringSoFar = StringExt.substring(this.text, start, end);
      if (nCrChars > 0l) {
        stringSoFar = StringExt.replaceAll(stringSoFar, "\r", "");
      }
      if (StringExt.contains(stringSoFar, "\\\n", 0l)) {
        lit = trimSlashLineBreak(stringSoFar);
      } else {
        lit = stringSoFar;
      }
    }
    return lit;
  }

  public String identDecNumber() {
    boolean hasWrongDigit = false;
    long firstWrongDigitPos = 0l;
    String firstWrongDigit = "";
    long startPos = this.pos;
    while (this.pos < StringExt.length(this.text)) {
      String c = StringExt.get(this.text, this.pos);
      if (!ParserUtil.isDigit(c)
          && !(Objects.equals(c, TypeScanner.numSep))
          && !(Objects.equals(c, "-"))) {
        if (!ParserUtil.isLetter(c)
            || (Objects.equals(c, "e") || Objects.equals(c, "E") || Objects.equals(c, "-"))) {
          break;
        } else if (!hasWrongDigit) {
          hasWrongDigit = true;
          firstWrongDigitPos = this.pos;
          firstWrongDigit = c;
        }
      }
      this.pos++;
    }
    boolean callMethod = false;
    boolean isRange = false;
    boolean isFloatWithoutFraction = false;
    if (this.pos < StringExt.length(this.text)
        && Objects.equals(StringExt.get(this.text, this.pos), ".")) {
      this.pos++;
      if (this.pos < StringExt.length(this.text)) {
        if (ParserUtil.isDigit(StringExt.get(this.text, this.pos))) {
          while (this.pos < StringExt.length(this.text)) {
            String c = StringExt.get(this.text, this.pos);
            if (!ParserUtil.isDigit(c)) {
              if (!ParserUtil.isLetter(c) || (Objects.equals(c, "e") || Objects.equals(c, "E"))) {
                if ((Objects.equals(c, "."))
                    && (this.pos + 1l < StringExt.length(this.text))
                    && ParserUtil.isLetter(StringExt.get(this.text, this.pos + 1l))) {
                  callMethod = true;
                }
                break;
              } else if (!hasWrongDigit) {
                hasWrongDigit = true;
                firstWrongDigitPos = this.pos;
                firstWrongDigit = c;
              }
            }
            this.pos++;
          }
        } else if (Objects.equals(StringExt.get(this.text, this.pos), ".")) {
          isRange = true;
          this.pos--;
        } else if (ListExt.asList("e", "E").contains(StringExt.get(this.text, this.pos))) {
          /*
          s.e5
          */
        } else if (ParserUtil.isLetter(StringExt.get(this.text, this.pos))) {
          /*
          5.str()
          */
          callMethod = true;
          this.pos--;
        } else if (!(Objects.equals(StringExt.get(this.text, this.pos), ")"))) {
          /*
          5.
          */
          isFloatWithoutFraction = true;
          this.pos--;
        }
      }
    }
    boolean hasExp = false;
    if (this.pos < StringExt.length(this.text)
        && (Objects.equals(StringExt.get(this.text, this.pos), "e")
            || Objects.equals(StringExt.get(this.text, this.pos), "E"))) {
      hasExp = true;
      this.pos++;
      if (this.pos < StringExt.length(this.text)
          && (Objects.equals(StringExt.get(this.text, this.pos), "-")
              || Objects.equals(StringExt.get(this.text, this.pos), "+"))) {
        this.pos++;
      }
      while (this.pos < StringExt.length(this.text)) {
        String c = StringExt.get(this.text, this.pos);
        if (!ParserUtil.isDigit(c)) {
          if (!ParserUtil.isLetter(c)) {
            /*
            5e5.str()
            */
            if ((Objects.equals(c, "."))
                && (this.pos + 1l < StringExt.length(this.text))
                && ParserUtil.isLetter(StringExt.get(this.text, this.pos + 1l))) {
              callMethod = true;
            }
            break;
          } else if (!hasWrongDigit) {
            hasWrongDigit = true;
            firstWrongDigitPos = this.pos;
            firstWrongDigit = c;
          }
        }
        this.pos++;
      }
    }
    if (hasWrongDigit) {
      this.pos = firstWrongDigitPos;
      error("this number has unsuitable digit");
    } else if ((Objects.equals(StringExt.get(this.text, this.pos - 1l), "e")
        || Objects.equals(StringExt.get(this.text, this.pos - 1l), "E"))) {
      this.pos--;
      error("exponent has no digits");
    } else if ((this.pos < StringExt.length(this.text))
        && (Objects.equals(StringExt.get(this.text, this.pos), "."))
        && !isRange
        && !isFloatWithoutFraction
        && !callMethod) {
      if (hasExp) {
        error("exponential part should be integer");
      } else {
        error("too many decimal points in number");
      }
    }
    String number =
        StringExt.replaceAll(
            StringExt.substring(this.text, startPos, this.pos), TypeScanner.numSep, "");
    this.pos--;
    return number;
  }

  public String identBinNumber() {
    /*
     TODO: Why does a binary number contain letters?
    */
    return _identNonDecNumber(
        (c) -> {
          return !ParserUtil.isBinDigit(c);
        },
        (c) -> {
          return (!ParserUtil.isDigit(c) && !ParserUtil.isLetter(c));
        });
  }

  public String identHexNumber() {
    return _identNonDecNumber(
        (c) -> {
          return !ParserUtil.isHexDigit(c);
        },
        (c) -> {
          return !ParserUtil.isLetter(c);
        });
  }

  public String identOctNumber() {
    return _identNonDecNumber(
        (c) -> {
          return !ParserUtil.isOctDigit(c);
        },
        (c) -> {
          return (!ParserUtil.isDigit(c) && !ParserUtil.isLetter(c));
        });
  }

  public String _identNonDecNumber(
      Function<String, Boolean> digitCheck, Function<String, Boolean> breakCheck) {
    boolean hasWrongDigit = false;
    long firstWrongDigitPos = 0l;
    String firstWrongDigit = "\\0";
    long startPos = this.pos;
    this.pos += 2l;
    /*
     skip '0x'
    */
    String c = StringExt.get(this.text, this.pos);
    if (Objects.equals(c, TypeScanner.numSep)) {
      this.error("separator `_` is only valid between digits in a numeric literal");
    }
    while (this.pos < StringExt.length(this.text)) {
      c = StringExt.get(this.text, this.pos);
      if (digitCheck.apply(c) && (!(Objects.equals(c, TypeScanner.numSep)))) {
        if (breakCheck.apply(c)) {
          break;
        } else if (!hasWrongDigit) {
          hasWrongDigit = true;
          firstWrongDigitPos = this.pos;
          firstWrongDigit = c;
        }
      }
      this.pos++;
    }
    if (startPos + 2l == this.pos) {
      this.pos--;
      /*
       adjust error position
      */
      this.error("number part of this number is not provided");
    } else if (hasWrongDigit) {
      this.pos = firstWrongDigitPos;
      /*
       adjust error position
      */
      this.error("this number has unsuitable digit `" + firstWrongDigit + "`");
    }
    String number =
        StringExt.replaceAll(
            StringExt.substring(this.text, startPos, this.pos), TypeScanner.numSep, "");
    this.pos--;
    return number;
  }

  public String trimSlashLineBreak(String s) {
    /*
     Integer start = 0;
     String retString = s;
     while(true) {
         Integer idx = retString.indexOf('\\\n', start);
         if(idx != -1) {
             retString =retString.substring(0, idx) + retString.substring(idx + 2).trim();
             start = idx;
         } else {
             break;
         }
     }
     return retString;
    */
    return IterableExt.join(
        ListExt.map(
            StringExt.split(s, "\n"),
            (one) -> {
              if (one.endsWith("\\")) {
                return StringExt.substring(one, 0l, StringExt.length(one) - 1l);
              }
              return one;
            }),
        "");
  }

  public long countSymbolBefore(long p, String c) {
    long count = 0l;
    for (long i = p; i >= 0l; i--) {
      if (!(Objects.equals(StringExt.get(this.text, i), c))) {
        break;
      }
      count++;
    }
    return count;
  }

  public String identNumber() {
    if (expect("0x", this.pos)) {
      return identHexNumber();
    }
    if (expect("0b", this.pos)) {
      return identBinNumber();
    }
    if (expect("0o", this.pos)) {
      return identOctNumber();
    }
    return identDecNumber();
  }

  public TypeToken newToken(TypeKind kind, String name, long len) {
    TypeToken token = new TypeToken();
    token.kind = kind;
    token.lit = name;
    token.len = len;
    token.lineNo = this.lineNo;
    long newLineStart = this.lastNlPos > 0l ? this.lastNlPos + 1l : 0l;
    token.index = this.pos - newLineStart - len + 1l;
    if (token.index < 0l) {
      token.index = 0l;
    }
    token.pos = this.lastNlPos + token.index;
    return token;
  }

  public TypeToken newToken2(TypeKind kind, String name, long index, long len) {
    TypeToken token = new TypeToken();
    token.kind = kind;
    token.lit = name;
    token.len = len;
    token.lineNo = this.lineNo;
    token.index = index;
    return token;
  }

  public static boolean isNl(String c) {
    return Objects.equals(c, "\r") || Objects.equals(c, "\n");
  }

  public TypeToken endOfFile() {
    this.eofs++;
    return newToken(TypeKind.Eof, "", 1l);
  }

  public void skipWhiteSpace() {
    while (this.pos < StringExt.length(this.text)
        && ParserUtil.isSpace(StringExt.get(this.text, this.pos))) {
      if (expect("\n", this.pos)) {
        incLineNumber();
      }
      this.pos++;
    }
  }

  public String lookAhead(long n) {
    if (this.pos + n < StringExt.length(this.text)) {
      return StringExt.get(this.text, this.pos + n);
    } else {
      return "";
    }
  }

  public String identName() {
    long start = this.pos;
    this.pos++;
    while ((this.pos < StringExt.length(this.text))
        && (ParserUtil.isNameChar(StringExt.get(this.text, this.pos))
            || ParserUtil.isDigit(StringExt.get(this.text, this.pos)))) {
      this.pos++;
    }
    String name = StringExt.substring(this.text, start, this.pos);
    this.pos--;
    return name;
  }

  public void save() {
    this.savedPos = this.pos;
  }

  public void restore() {
    this.pos = this.savedPos;
  }
}
