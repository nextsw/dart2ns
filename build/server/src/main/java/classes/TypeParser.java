package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.MathExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Objects;

public class TypeParser {
  public TypeScanner scanner;
  public TypeToken tok;
  public TypeToken prevTok;
  public TypeToken peekTok;
  public TypeToken peekTok2;
  public TypeToken peekTok3;
  public TypeToken peekTok4;
  public boolean expectingType = false;
  public boolean insideStrInterp = false;
  public List<GenError> errors = ListExt.asList();
private Dart2NSContext context;

  public TypeParser(Dart2NSContext context, TypeScanner scanner) {
	  this.context = context;
    this.scanner = scanner;
  }

  public static List<TopDecl> parse(Dart2NSContext context, String path) {
    List<TopDecl> res = ListExt.asList();
    String content = context.path2Content(path);
    if(content.isEmpty()) {
    	return res;
    }
    System.out.println("Parsing Started: " + path);
    context.push(path);
    TypeParser p = new TypeParser(context, new TypeScanner(content));
    p.readFirstToken();
    while (p.readTopObject(res)) {}
    p.eatComments();
    p.check(TypeKind.Eof);
    context.pop();
    System.out.println("Parsing Finished: " + path);
    return res;
  }

  public static void updateErrorsPath(String prop, List<GenError> errors) {
    errors.forEach(
        (e) -> {
          e.path = TypeParser.preparePath(prop, e.path);
        });
  }

  public static String preparePath(String prop, String path) {
    if (path == null) {
      return prop;
    } else if (StringExt.startsWith(path, "[", 0l)) {
      return prop + path;
    }
    return prop + "." + path;
  }

  public boolean readTopObject(List<TopDecl> list) {
    TypeToken start = this.tok;
    eatComments();
    TopDecl obj = null;
    List<Annotation> annotations = ListExt.asList();
    if (isKey(this.tok, "library")) {
      String lib = readLibrary();
      return true;
    } else if (isKey(this.tok, "export")) {
      List<TopDecl> objs = readImportOrExport();
      ListExt.addAll(list, objs);
      return true;
    } else if (isKey(this.tok, "import")) {
      List<TopDecl> objs = readImportOrExport();
      ListExt.addAll(list, objs);
      return true;
    }
    if (this.tok.kind == TypeKind.At) {
      annotations = readAnnotations();
    } else if (isKey(this.tok, "typedef")) {
      obj = readTypeDef(annotations);
    } else if (isKey(this.tok, "enum")) {
      obj = readEnum(annotations);
    } else if (this.tok.kind == TypeKind.Eof) {
      obj = null;
    } else if (isKey(this.tok, "class") || isKey(this.tok, "abstract")) {
      obj = readClass(annotations, start);
    } else {
      ClassMember mem = readClassMember();
    }
    if (obj != null) {
      list.add(obj);
      return true;
    } else {
    	return false;
    }
  }

  public List<TopDecl> readImportOrExport() {
    next();
    String path = tok.lit;
    List<TopDecl> objs = TypeParser.parse(context, path);
    next();
    while (this.tok.kind != TypeKind.Semicolon) {
      /*
       Ignoring the Show spec
      */
      next();
    }
    check(TypeKind.Semicolon);
    return objs;
  }


  public String readLibrary() {
    next();
    String lib = checkName();
    check(TypeKind.Semicolon);
    return lib;
  }

  public List<Annotation> readAnnotations() {
    List<Annotation> res = ListExt.asList();
    while (this.tok.kind == TypeKind.At) {
      Annotation at = readAnnotation();
      res.add(at);
    }
    return res;
  }

  public Annotation readAnnotation() {
    TypeToken start = this.tok;
    next();
    String name = checkName();
    String value = null;
    if (this.tok.kind == TypeKind.Lpar) {
      next();
      if (this.tok.kind == TypeKind.String) {
        value = this.tok.lit;
        next();
      }
      check(TypeKind.Rpar);
    }
    return new Annotation(name, value);
  }

  public Enum readEnum(List<Annotation> annotations) {
    TypeToken start = this.tok;
    checkKey("enum");
    String name = checkName();
    check(TypeKind.Lcbr);
    Enum data = new Enum(name);
    data.values.clear();
    while (this.tok.kind != TypeKind.Rcbr && this.tok.kind != TypeKind.Eof) {
      eatComments();
      String id = checkName();
      data.values.add(id);
      eatComments();
      if (this.tok.kind != TypeKind.Comma) {
        break;
      } else {
        next();
      }
    }
    check(TypeKind.Rcbr);
    data.annotations = annotations;
    return data;
  }

  public void checkKey(String key) {
    if (isKey(this.tok, key)) {
      next();
    } else {
      error("Expected: " + key + " found " + this.tok.lit);
    }
  }

  public Typedef readTypeDef(List<Annotation> annotations) {
    TypeToken start = this.tok;
    checkKey("typedef");
    String name = checkName();
    TypeParams typeParams = null;
    if (this.tok.kind == TypeKind.Lt) {
      typeParams = readTypeParams();
    }
    check(TypeKind.Assign);
    DataType retType = readType();
    String fn = checkName();
    MethodParams params = readMethodParams();
    check(TypeKind.Semicolon);
    Typedef data = new Typedef(name);
    data.generics = typeParams;
    data.method =
        new MethodDecl(
            ListExt.List(),
            null,
            null,
            false,
            false,
            null,
            false,
            null,
            false,
            null,
            "$",
            params,
            retType,
            false,
            false);
    data.annotations = annotations;
    return data;
  }

  public TypeParams readTypeParams() {
    TypeToken start = this.tok;
    /*
     Need to call next here to move over the "<"
    */
    check(TypeKind.Lt);
    TypeParams params = new TypeParams();
    while (true) {
      if (this.tok.kind == TypeKind.Gt) {
        break;
      }
      String name = checkName();
      DataType type = null;
      if (isKey(this.tok, "extends")) {
        next();
        type = readType();
      }
      params.params.add(new TypeParam(type, name));
      if (this.tok.kind != TypeKind.Comma) {
        break;
      } else {
        next();
      }
    }
    /*
     Need to call next here to move over the ">"
    */
    check(TypeKind.Gt);
    return params;
  }

  public MethodParams readMethodParams() {
    check(TypeKind.Lpar);
    MethodParams params = new MethodParams();
    while (true) {
      List<Comment> comments = eatComments();
      if (this.tok.kind == TypeKind.Lsbr) {
        params.optionalParams = readParams(TypeKind.Lsbr, TypeKind.Rsbr);
      } else if (this.tok.kind == TypeKind.Lcbr) {
        params.namedParams = readParams(TypeKind.Lcbr, TypeKind.Rcbr);
      }
      if (this.tok.kind == TypeKind.Eof || this.tok.kind == TypeKind.Rpar) {
        break;
      }
      params.positionalParams.add(readParam());
      if (this.tok.kind == TypeKind.Comma) {
        next();
      }
    }
    check(TypeKind.Rpar);
    return params;
  }

  public List<MethodParam> readParams(TypeKind start, TypeKind end) {
    List<MethodParam> params = ListExt.asList();
    check(start);
    while (true) {
      List<Comment> comments = eatComments();
      if (this.tok.kind == end || this.tok.kind == TypeKind.Eof) {
        break;
      }
      MethodParam param = readParam();
      param.beforeComments = comments;
      params.add(param);
      if (this.tok.kind == TypeKind.Comma) {
        next();
      }
    }
    check(end);
    return params;
  }

  public MethodParam readParam() {
    TypeToken start = this.tok;
    List<Annotation> annotations = ListExt.asList();
    String name = null;
    MethodParams fparams = null;
    Expression def = null;
    DataType type = null;
    if (this.tok.kind == TypeKind.At) {
      annotations = readAnnotations();
    }
    boolean hasThis = false;
    if (isKey(this.tok, "this")) {
      hasThis = true;
      next();
      check(TypeKind.Dot);
      name = checkName();
    } else {
      type = readType();
      name = checkName();
      if (this.tok.kind == TypeKind.Lpar) {
        fparams = readMethodParams();
      }
    }
    if (this.tok.kind == TypeKind.Assign) {
      def = expr(0l);
      next();
    }
    boolean deprecated =
        ListExt.any(
            annotations,
            (x) -> {
              return Objects.equals(x.name, "deprecated");
            });
    boolean required =
        ListExt.any(
            annotations,
            (x) -> {
              return Objects.equals(x.name, "required");
            });
    return new MethodParam(
        annotations, type, def, deprecated, name, required, hasThis ? "this" : null);
  }

  public ClassDecl readClass(List<Annotation> annotations, TypeToken start) {
    boolean isAbstract = false;
    boolean isClient = false;
    boolean isServer = false;
    if (isKey(this.tok, "abstract")) {
      isAbstract = true;
      next();
    }
    if (isKey(this.tok, "client")) {
      isClient = true;
      next();
    } else if (isKey(this.tok, "server")) {
      isServer = true;
      next();
    }
    checkKey("class");
    String name = checkName();
    ClassDecl cls = new ClassDecl(name);
    cls.isAbstract = isAbstract;
    if (this.tok.kind == TypeKind.Lt) {
      cls.generics = readTypeParams();
    }
    if (isKey(this.tok, "extends")) {
      next();
      cls.extendType = readType();
    }
    cls.impls.clear();
    if (isKey(this.tok, "implements")) {
      next();
      while (true) {
        cls.impls.add(readType());
        if (this.tok.kind != TypeKind.Comma) {
          break;
        }
        next();
      }
    }
    if (isKey(this.tok, "with")) {
      next();
      while (true) {
        cls.mixins.add(readType());
        if (this.tok.kind != TypeKind.Comma) {
          break;
        }
        next();
      }
    }
    check(TypeKind.Lcbr);
    cls.members.clear();
    while (true) {
      List<Comment> comments = eatComments();
      if (this.tok.kind == TypeKind.Rcbr || this.tok.kind == TypeKind.Eof) {
        break;
      }
      ClassMember member = readClassMember();
      member.comments = comments;
      cls.members.add(member);
    }
    check(TypeKind.Rcbr);
    cls.annotations = annotations;
    return cls;
  }

  public ClassMember readClassMember() {
    List<Comment> comments = eatComments();
    TypeToken start = this.tok;
    List<Annotation> annotations = readAnnotations();
    boolean isStatic = false;
    boolean isFinal = false;
    boolean isConst = false;
    boolean isFactory = false;
    boolean isAbstract = false;
    ASyncType asyncType = ASyncType.NONE;
    if (isKey(this.tok, "static")) {
      isStatic = true;
      next();
    }
    if (isKey(this.tok, "final")) {
      isFinal = true;
      next();
    }
    if (isKey(this.tok, "const")) {
      isConst = true;
      next();
    }
    if (isKey(this.tok, "factory")) {
      isFactory = true;
      next();
    }
    if (isKey(this.tok, "abstract")) {
      isAbstract = true;
      next();
    }
    String fac = null;
    String name = null;
    Expression init = null;
    if (this.peekTok.kind == TypeKind.Dot) {
      fac = checkName();
      next();
    }
    DataType type = null;
    if (fac == null && isDeclaration()) {
    	save();
      /*
       Field Decl
      */
      type = readType();
      name = checkName();
        Expression exp = null;
        if (this.tok.kind == TypeKind.Assign) {
          next();
          exp = expr(0l);
          check(TypeKind.Semicolon);
          return new FieldDecl(comments, isConst, isFinal, name, isStatic, type, exp);
        } else if (this.tok.kind == TypeKind.Semicolon){
          next();
          return new FieldDecl(comments, isConst, isFinal, name, isStatic, type, exp);
        } else {
        	restore();
        }
        
    }
    /*
     Method Decal
    */
    Block body = null;
    TypeParams typeParams = null;
    boolean isSet = false;
    boolean isGet = false;
    if (!isFactoryDecl()) {
      if (type == null) {
        type = readType();
      }
      if (this.peekTok.kind == TypeKind.Name) {
        if (isKey(this.tok, "set")) {
          isSet = true;
          next();
        } else if (isKey(this.tok, "get")) {
          isGet = true;
          next();
        }
      }
    }
    if (name == null) {
      name = checkName();
    }
    if (this.tok.kind == TypeKind.Lt) {
      typeParams = readTypeParams();
    }
    MethodParams params = null;
    if (!isGet) {
      params = readMethodParams();
    }
    if (this.tok.kind == TypeKind.Colon) {
      /*
      init call
      */
    	next();
      init = expr(0l);
    }
    if (isKey(this.tok, "async")) {
      next();
      asyncType = ASyncType.ASYNC;
    }
    if (this.tok.kind == TypeKind.Lcbr) {
      body = readBlock(true);
      next();
    } else {
      check(TypeKind.Semicolon);
    }
    return new MethodDecl(
        annotations,
        asyncType,
        body,
        isConst,
        isFactory,
        fac,
        isFinal,
        typeParams,
        isGet,
        init,
        name,
        params,
        type,
        isSet,
        isStatic);
  }

  private void restore() {
	scanner.save();
}

private void save() {
	scanner.restore();
}

public boolean isFactoryDecl() {
    return (this.tok.kind == TypeKind.Name && this.peekTok.kind == TypeKind.Dot)
        || (this.tok.kind == TypeKind.Name
            && (this.peekTok.kind == TypeKind.Lt || this.peekTok.kind == TypeKind.Lpar));
  }

  public List<String> readPath() {
    check(TypeKind.At);
    List<String> list = ListExt.asList();
    while (true) {
      String name = checkName();
      list.add(name);
      if (this.tok.kind != TypeKind.Dot) {
        break;
      }
      next();
    }
    return list;
  }

  public Block readBlock(boolean withBraces) {
    Block block = new Block();
    if (withBraces) {
      check(TypeKind.Lcbr);
      /*
       check() already calls next once.
       next();
      */
    }
    while (true) {
      List<Comment> comments = eatComments();
      if (this.tok.kind == TypeKind.Rcbr || this.tok.kind == TypeKind.Eof) {
        block.afterComments = comments;
        break;
      }
      Statement smt = readStatement(comments, false);
      if (smt == null) {
        block.afterComments = comments;
        break;
      }
      block.statements.add(smt);
    }
    if (withBraces) {
      check(TypeKind.Rcbr);
      /*
       check() already calls next once.
       next();
      */
    }
    return block;
  }

  public Statement readStatement(List<Comment> comments, boolean skipSemiColon) {
    comments = comments != null ? comments : eatComments();
    Statement smt = null;
    TypeToken start = this.tok;
    if (isKey(this.tok, "return")) {
      smt = readReturn();
    } else if (isKey(this.tok, "throw")) {
      smt = readThrow();
    } else if (isKey(this.tok, "for")) {
      smt = readFor();
    } else if (isKey(this.tok, "while")) {
      smt = readWhile();
    } else if (isKey(this.tok, "do")) {
      smt = readDoWhile();
    } else if (isKey(this.tok, "switch")) {
      smt = readSwitch();
    } else if (isKey(this.tok, "if")) {
      smt = readIf();
    } else if (isKey(this.tok, "try")) {
      smt = readTry();
    } else if (isKey(this.tok, "break")) {
      next();
      check(TypeKind.Semicolon);
      smt = new Break();
    } else if (isKey(this.tok, "continue")) {
      next();
      check(TypeKind.Semicolon);
      smt = new Continue();
    } else if (this.tok.kind == TypeKind.Lcbr) {
      smt = readBlock(true);
    } else {
      /*
       Declaraton, Assignment, Method Call
      */
      boolean isPrefix = TypeToken.isPrefix(this.tok.kind);
      if (isDeclaration()) {
        smt = readDecl();
      } else {
        /*
         must be assignment or method call
        */
        Expression exp = expr(0l);
        if (TypeToken.isAssign(this.tok.kind)) {
          /*
           Assignment
          */
          smt = readAssignment(exp, start);
        } else if (isPrefix) {
          smt = ((PrefixExpression) exp);
        } else if (TypeToken.isPostfix(this.prevTok.kind)) {
          smt = ((PostfixExpression) exp);
        } else if (exp instanceof MethodCall) {
          smt = ((MethodCall) exp);
        } else {
          error("Unknown expression/statement");
        }
      }
      if (!skipSemiColon) {
        /*
         We'll need to ignore the semi-colon after the "reset" part of a traditional for loop. No one writes a semi-colon there.
        */
        check(TypeKind.Semicolon);
      }
    }
    smt.comments = comments;
    return smt;
  }

  public Assignment readAssignment(Expression left, TypeToken start) {
    TypeToken opt = this.tok;
    String op = this.tok.lit;
    next();
    Expression val = expr(0l);
    return new Assignment(((FieldOrEnumExpression) left), op, val);
  }

  public Statement readDecl() {
    TypeToken start = this.tok;
    DataType type = readType();
    String name = checkName();
    Expression init = null;
    if (this.tok.kind != TypeKind.Semicolon) {
      if (!TypeToken.isAssign(this.tok.kind)) {
        error("Expecting an assign operator");
      } else {
        next();
        /*
         Skip over the assign op
        */
      }
      init = expr(0l);
    }
    return new Declaration(init, name, type);
  }

  public boolean isDeclaration() {
    /*
    Check if it simple declation i.e Integer a; or Integer b = __;
    */
    boolean simpleD =
        (this.tok.kind == TypeKind.Name
            && this.peekTok.kind == TypeKind.Name
            && (this.peekTok2.kind == TypeKind.Semicolon || this.peekTok2.kind == TypeKind.Assign));
    boolean simplePackageD =
            (this.tok.kind == TypeKind.Name
            	&& this.peekTok.kind == TypeKind.Dot
                && this.peekTok2.kind == TypeKind.Name
                && this.peekTok3.kind == TypeKind.Question
                && this.peekTok4.kind == TypeKind.Name);
    boolean isType =
        this.tok.kind == TypeKind.Name
            && this.peekTok.kind == TypeKind.Lt
            && this.peekTok2.kind == TypeKind.Name
            && (this.peekTok3.kind == TypeKind.Gt
                || this.peekTok3.kind == TypeKind.Comma
                || this.peekTok3.kind == TypeKind.Lt);
    return simpleD || isType || simplePackageD;
  }

  public Statement readDoWhile() {
    TypeToken start = this.tok;
    next();
    /*
     Skip over "do"
    */
    Block block = readBlock(true);
    next();
    /*
     Skip over "while"
    */
    check(TypeKind.Lpar);
    Expression test = expr(0l);
    check(TypeKind.Rpar);
    check(TypeKind.Semicolon);
    return new DoWhileLoop(block, test);
  }

  public Statement readTry() {
    TypeToken start = this.tok;
    next();
    TryCatcheStatment tcs = new TryCatcheStatment(null, ListExt.List(), null);
    tcs.body = readBlock(true);
    Block finallyBlock = null;
    while (isKey(this.tok, "on") || isKey(this.tok, "catch")) {
      /*
      read catche
      */
      TypeToken startC = this.tok;
      CatchPart catchPart = new CatchPart();
      if (isKey(this.tok, "on")) {
        next();
        catchPart.onType = readType();
      }
      checkKey("catch");
      check(TypeKind.Lpar);
      String name = checkName();
      String asName = null;
      if (this.tok.kind == TypeKind.Comma) {
        next();
        asName = checkName();
      }
      check(TypeKind.Rpar);
      catchPart.exp = name;
      catchPart.stackTrace = asName;
      catchPart.body = readBlock(true);
      tcs.catchParts.add(catchPart);
    }
    if (isKey(this.tok, "finally")) {
      next();
      tcs.finallyBody = readBlock(true);
    }
    return tcs;
  }

  public Statement readIf() {
    TypeToken start = this.tok;
    next();
    check(TypeKind.Lpar);
    Expression test = expr(0l);
    check(TypeKind.Rpar);
    Block block = readBlock(true);
    Statement elseS = null;
    if (isKey(this.tok, "else")) {
      next();
      elseS = readStatement(ListExt.List(), false);
    }
    return new IfStatement(elseS, test, block);
  }

  public Statement readSwitch() {
    TypeToken start = this.tok;
    next();
    check(TypeKind.Lpar);
    Expression test = expr(0l);
    SwitchStatement ss = new SwitchStatement(test);
    check(TypeKind.Rpar);
    check(TypeKind.Lcbr);
    while (this.tok.kind != TypeKind.Rcbr) {
      List<Comment> comments = eatComments();
      if (isKey(this.tok, "case")) {
        next();
        SwitchCaseBlock caseBlock = new SwitchCaseBlock();
        caseBlock.tests.add(expr(0l));
        /*
         while(true) {
             if(tok.kind != TypeKind.Comma) {
                 break;
             }
         }
        */
        check(TypeKind.Colon);
        while (isKey(this.tok, "case")) {
          next();
          caseBlock.tests.add(expr(0l));
          check(TypeKind.Colon);
        }
        while (true) {
          List<Comment> comments2 = eatComments();
          if (this.tok.kind == TypeKind.Rcbr
              || isKey(this.tok, "case")
              || isKey(this.tok, "default")) {
            break;
          }
          Statement smt = readStatement(comments2, false);
          if (smt == null) {
            break;
          }
          caseBlock.statements.add(smt);
        }
        ss.cases.add(caseBlock);
      } else if (isKey(this.tok, "default")) {
        next();
        check(TypeKind.Colon);
        while (this.tok.kind != TypeKind.Rcbr) {
          ss.defaults.add(readStatement(ListExt.List(), false));
        }
        break;
      } else {
        break;
      }
    }
    check(TypeKind.Rcbr);
    return ss;
  }

  public boolean isKey(TypeToken token, String key) {
    return token.kind == TypeKind.Name && Objects.equals(token.lit, key);
  }

  public Statement readWhile() {
    TypeToken start = this.tok;
    next();
    check(TypeKind.Lpar);
    Expression test = expr(0l);
    check(TypeKind.Rpar);
    Block block = readBlock(true);
    return new WhileLoop(block, test);
  }

  public Statement readFor() {
    TypeToken start = this.tok;
    next();
    check(TypeKind.Lpar);
    boolean forEachLoop = false;
    forEachLoop =
        (this.tok.kind == TypeKind.Name
            && this.peekTok.kind == TypeKind.Name
            && isKey(this.peekTok2, "in"));
    boolean nextIsType =
        this.peekTok.kind == TypeKind.Lt
            && this.peekTok2.kind == TypeKind.Name
            && (this.peekTok3.kind == TypeKind.Gt
                || this.peekTok3.kind == TypeKind.Comma
                || this.peekTok3.kind == TypeKind.Lt);
    forEachLoop = forEachLoop || (this.tok.kind == TypeKind.Name && nextIsType);
    if (forEachLoop) {
      DataType type = readType();
      String name = checkName();
      checkKey("in");
      Expression exp = expr(0l);
      check(TypeKind.Rpar);
      Block block = readBlock(true);
      return new ForEachLoop(block, exp, type, name);
    } else {
      /*
       Normal for loop
      */
      List<Statement> inits = ListExt.asList();
      /*
       Need to check prevTok also here since semi-colon is check()-ed while reading statement.
       TODO: Check if this also happens for EOF
      */
      while (true) {
        if (this.tok.kind == TypeKind.Semicolon || this.tok.kind == TypeKind.Eof) {
          break;
        }
        inits.add(readStatement(ListExt.List(), false));
        if (this.tok.kind != TypeKind.Comma) {
          break;
        }
        next();
      }
      Declaration decl = null;
      if ((ListExt.length(inits) > 0l) && (ListExt.get(inits, 0l) instanceof Declaration)) {
        decl = ((Declaration) ListExt.removeAt(inits, 0l));
      }
      Expression exp = expr(0l);
      next();
      /*
       Skip over the semi-colon
      */
      List<Statement> resets = ListExt.asList();
      while (true) {
        if (this.tok.kind == TypeKind.Rpar || this.tok.kind == TypeKind.Eof) {
          break;
        }
        resets.add(readStatement(ListExt.List(), true));
        if (this.tok.kind != TypeKind.Comma) {
          break;
        }
        next();
      }
      check(TypeKind.Rpar);
      Block block = readBlock(true);
      return new ForLoop(block, decl, inits, resets, exp);
    }
  }

  public ThrowStatement readThrow() {
    TypeToken start = this.tok;
    next();
    ThrowStatement ret = new ThrowStatement();
    ret.exp = expr(0l);
    check(TypeKind.Semicolon);
    return ret;
  }

  public Return readReturn() {
    TypeToken start = this.tok;
    next();
    Return ret = new Return();
    if (this.tok.kind != TypeKind.Semicolon) {
      ret.expression = expr(0l);
    }
    check(TypeKind.Semicolon);
    return ret;
  }

  public Expression expr(long precedence) {
    List<Comment> comments = eatComments();
    Expression node = null;
    TypeToken start = this.tok;
    switch (this.tok.kind) {
      case Name:
        {
          {
            if (isKey(this.tok, "true") || isKey(this.tok, "false")) {
              node = new LiteralExpression(false, LiteralType.TypeBoolean, this.tok.lit);
              next();
            } else if (isKey(this.tok, "null")) {
              node = new NullExpression();
              next();
            } else if (this.peekTok.kind == TypeKind.Arrow) {
              return singleParamLambda();
            } else if (isKey(this.tok, "switch")) {
              return switchExpr();
            } else {
              node = nameExpr();
            }
          }
          break;
        }
      case String:
        {
          {
            node = stringExpr();
          }
          break;
        }
      case Minus:
        {
          {
            node = prefixExpr();
          }
          break;
        }
      case Mul:
        {
          {
            node = prefixExpr();
          }
          break;
        }
      case Not:
        {
          {
            node = prefixExpr();
          }
          break;
        }
      case BitNot:
        {
          {
            node = prefixExpr();
          }
          break;
        }
      case Number:
        {
          {
            node = parseNumberLiteral();
          }
          break;
        }
      case Lpar:
        {
          {
            node = parOrLambdaExpr();
          }
          break;
        }
      case Lsbr:
        {
          {
            ArrayExpression arr = arrayInit(true);
            node = arr;
          }
          break;
        }
      case Lcbr:
        {
          {
            ArrayExpression arr = arrayInit(false);
            node = arr;
          }
          break;
        }
      case Lt:
        {
          {
            check(TypeKind.Lt);
            DataType type = readType();
            check(TypeKind.Gt);
            ArrayExpression arr = arrayInit(true);
            arr.enforceType = type;
            node = arr;
          }
          break;
        }
      case Inc:
      case Dec:
        {
          {
            String prefix = this.tok.lit;
            next();
            Expression exp = expr(precedence);
            node = new PrefixExpression(exp, prefix);
          }
          break;
        }
      default:
        {
          {
            error("bad token: " + this.tok.kind.toString());
          }
        }
    }
    while (precedence < this.tok.getPrecedence()) {
      if (this.tok.kind == TypeKind.Dot) {
        node = dotExpr(node, false, false);
      } else if (this.tok.kind == TypeKind.Lsbr) {
        node = indexExpr(node);
      } else if (isKey(this.tok, "as")) {
        next();
        DataType type = readType();
        node = new TypeCastOrCheckExpression(false, type, node);
      } else if (isKey(this.tok, "is")) {
        next();
        DataType type = readType();
        node = new TypeCastOrCheckExpression(true, type, node);
      } else if (this.tok.kind == TypeKind.LeftShift) {
        TypeToken ttok = this.tok;
        next();
        Expression right = expr(precedence - 1l);
        node = new BinaryExpression(node, ttok.lit, right);
      } else if (TypeToken.isInfix(this.tok.kind)) {
        node = infixExpr(node);
      } else if (this.tok.kind == TypeKind.Inc || this.tok.kind == TypeKind.Dec) {
        node = new PostfixExpression(node, this.tok.lit);
        next();
      } else if (this.tok.kind == TypeKind.Question) {
        next();
        if (this.tok.kind == TypeKind.Dot) {
          node = dotExpr(node, true, false);
        } else {
          return ternaryExpr(node);
        }
      } else if (this.tok.kind == TypeKind.Not) {
        next();
        if (this.tok.kind == TypeKind.Dot) {
          node = dotExpr(node, false, true);
        } else {
          error("Not sure what should happen here");
        }
      } else {
        return node;
      }
    }
    if (node != null) {
      node.comments = comments;
    }
    return node;
  }

  public boolean isType() {
    return this.tok.kind == TypeKind.Lt
        && this.peekTok.kind == TypeKind.Name
        && (this.peekTok2.kind == TypeKind.Gt
            || this.peekTok2.kind == TypeKind.Comma
            || this.peekTok2.kind == TypeKind.Lt);
  }

  public Expression parOrLambdaExpr() {
    TypeToken start = this.tok;
    check(TypeKind.Lpar);
    Expression exp = null;
    if (this.tok.kind != TypeKind.Rpar) {
      exp = expr(0l);
    }
    boolean isLambda = this.peekTok.kind == TypeKind.Arrow || this.peekTok.kind == TypeKind.Lcbr;
    if (this.tok.kind == TypeKind.Rpar && !isLambda) {
      next();
      return new ParExpression(exp);
    } else {
      if (exp == null) {
        return lambdaExp(ListExt.asList());
      }
      return lambdaExp(ListExt.asList(((FieldOrEnumExpression) exp)));
    }
  }

  public Expression lambdaExp(List<FieldOrEnumExpression> exprs) {
    TypeToken start = this.tok;
    while (this.tok.kind != TypeKind.Rpar) {
      if (this.tok.kind == TypeKind.Comma) {
        next();
      } else {
        break;
      }
      exprs.add(((FieldOrEnumExpression) expr(0l)));
    }
    next();
    /*
     Remove the rpar
    */
    List<Param> params =
        IterableExt.toList(
            ListExt.map(
                exprs,
                (one) -> {
                  return new Param(one.name);
                }),
            false);
    LambdaExpression exp = new LambdaExpression(params);
    if (this.tok.kind == TypeKind.Arrow) {
      next();
      Expression val = expr(0l);
      if (TypeToken.isAssign(this.tok.kind)) {
        /*
         Added because Assignment is only being read as part of a statement.
        */
        val = readAssignment(val, start);
      }
      exp.expression = val;
    } else if (this.tok.kind == TypeKind.Lcbr) {
      exp.body = readBlock(true);
    }
    return exp;
  }

  public Expression singleParamLambda() {
    TypeToken start = this.tok;
    String name = checkName();
    check(TypeKind.Arrow);
    List<Param> params = ListExt.asList(new Param(name));
    LambdaExpression exp = new LambdaExpression(params);
    exp.expression = expr(0l);
    return exp;
  }

  public Expression ternaryExpr(Expression condition) {
    Expression yes = expr(0l);
    check(TypeKind.Colon);
    Expression no = expr(0l);
    TerinaryExpression exp = new TerinaryExpression(condition, no, yes);
    return exp;
  }

  public Expression switchExpr() {
    TypeToken start = this.tok;
    checkKey("switch");
    check(TypeKind.Lpar);
    Expression test = expr(0l);
    SwitchExpression exp = new SwitchExpression(ListExt.List(), test);
    check(TypeKind.Rpar);
    check(TypeKind.Lcbr);
    while (this.tok.kind != TypeKind.Rcbr) {
      if (isKey(this.tok, "case")) {
        next();
        CaseExpression caseExp = new CaseExpression();
        while (true) {
          caseExp.tests.add(expr(0l));
          if (this.tok.kind != TypeKind.Comma) {
            break;
          }
        }
        check(TypeKind.Colon);
        caseExp.result = expr(0l);
        exp.cases.add(caseExp);
      } else if (isKey(this.tok, "default")) {
        next();
        check(TypeKind.Colon);
        exp.onElse = expr(0l);
        break;
      } else {
        break;
      }
    }
    check(TypeKind.Rcbr);
    return exp;
  }

  public Expression infixExpr(Expression left) {
    TypeToken ttok = this.tok;
    TypeKind op = this.tok.kind;
    long precedence = this.tok.getPrecedence();
    next();
    if (isKey(ttok, "as") || isKey(ttok, "is")) {
      this.expectingType = true;
    }
    Expression right = expr(precedence);
    return new BinaryExpression(left, ttok.lit, right);
  }

  public DataType readType() {
    TypeToken start = this.tok;
    String name = checkName();
    boolean nextType = isType();
    DataType type = new DataType(name, false);
    if (nextType) {
      type.args = readTypeArgs();
    }
    if (this.tok.kind == TypeKind.Question) {
      type.optional = true;
      next();
    }
    return type;
  }

  public List<DataType> readTypeArgs() {
    TypeToken start = this.tok;
    /*
     Need to call next here to move over the "<"
    */
    if (this.tok.kind == TypeKind.Lt) {
      next();
    }
    List<DataType> args = ListExt.asList();
    while (true) {
      DataType sub = readType();
      args.add(sub);
      if (this.tok.kind != TypeKind.Comma) {
        break;
      } else {
        next();
      }
    }
    /*
     Need to call next here to move over the ">"
    */
    if (this.tok.kind == TypeKind.Gt) {
      next();
    }
    return args;
  }

  public void error(String msg) {
    System.err.println(msg);
  }

  public void readFirstToken() {
    next();
    next();
    next();
    next();
    next();
  }

  public void next() {
    this.prevTok = this.tok;
    this.tok = this.peekTok;
    this.peekTok = this.peekTok2;
    this.peekTok2 = this.peekTok3;
    this.peekTok3 = this.peekTok4;
    this.peekTok4 = this.scanner.scan();
  }

  public ArrayExpression arrayInit(boolean isList) {
    ArrayExpression arrExp = new ArrayExpression();
    DataType type = null;
    if (isType()) {
      arrExp.enforceType = readType();
    }
    TypeKind start = isList ? TypeKind.Lsbr : TypeKind.Lcbr;
    TypeKind end = isList ? TypeKind.Rsbr : TypeKind.Rcbr;
    check(start);
    while (true) {
      List<Comment> comments = eatComments();
      if (this.tok.kind == end || this.tok.kind == TypeKind.Eof) {
        break;
      }
      ArrayItem item = arrayItem(comments);
      arrExp.values.add(item);
      List<Comment> comments2 = eatComments();
      item.afterComments = comments2;
      if (this.tok.kind != TypeKind.Comma) {
        break;
      } else {
        next();
      }
    }
    check(end);
    arrExp.list = isList;
    return arrExp;
  }

  public ArrayItem arrayItem(List<Comment> comments) {
    comments = comments != null ? comments : eatComments();
    TypeToken start = this.tok;
    switch (this.tok.kind) {
      case Ellipses:
        {
          {
            next();
            boolean checkNull = this.tok.kind == TypeKind.Question;
            if (checkNull) {
              next();
            }
            Expression exp = expr(0l);
            return new CollectionSpread(comments, checkNull, exp);
          }
        }
      default:
        {
          {
            if (isKey(this.tok, "if")) {
              next();
              check(TypeKind.Lpar);
              Expression test = expr(0l);
              check(TypeKind.Rpar);
              ArrayItem exp = arrayItem(ListExt.List());
              ArrayItem elseS = null;
              if (isKey(this.tok, "else")) {
                next();
                elseS = arrayItem(ListExt.List());
              }
              CollectionIf cif = new CollectionIf(elseS, test, exp);
              cif.beforeComments = comments;
              return cif;
            } else if (isKey(this.tok, "for")) {
              next();
              check(TypeKind.Lpar);
              DataType type = readType();
              String name = checkName();
              checkKey("in");
              Expression exp = expr(0l);
              check(TypeKind.Rpar);
              ArrayItem value = arrayItem(ListExt.List());
              CollectionFor cFor = new CollectionFor(exp, type, name, value);
              cFor.beforeComments = comments;
              return cFor;
            } else {
              Expression exp = expr(0l);
              ExpressionArrayItem item = new ExpressionArrayItem(exp);
              item.beforeComments = comments;
              return item;
            }
          }
        }
    }
  }

  public void check(TypeKind expected) {
    if (this.tok.kind != expected) {
      if (this.tok.kind == TypeKind.Name) {
        error("unexpected name `" + this.tok.lit + "`, expecting " + expected.toString());
      } else {
        error(
            "unexpected name `" + this.tok.kind.toString() + "`, expecting " + expected.toString());
      }
    }
    next();
  }

  public Expression parseNumberLiteral() {
    String lit = this.tok.lit;
    Expression node;
    if (ParserUtil.isDouble(lit)) {
      node = new LiteralExpression(false, LiteralType.TypeDouble, lit);
    } else {
      node = new LiteralExpression(false, LiteralType.TypeInteger, lit);
    }
    next();
    return node;
  }

  public Expression prefixExpr() {
    TypeToken ttok = this.tok;
    next();
    Expression right =
        (ttok.kind == TypeKind.Minus ? expr(TypeToken.preCall) : expr(TypeToken.prePrefix));
    return new PrefixExpression(right, ttok.lit);
  }

  public Expression vweb() {
    return null;
  }

  public Expression stringExpr() {
    TypeToken start = this.tok;
    boolean isRaw = this.tok.kind == TypeKind.Name && Objects.equals(this.tok.lit, "r");
    if (isRaw) {
      next();
    }
    String value = this.tok.lit;
    Expression node;
    if (this.peekTok.kind != TypeKind.Dollar) {
      next();
      node = new LiteralExpression(isRaw, LiteralType.TypeString, isRaw ? value : value);
      return node;
    }
    /*
     Handle string interpolation
    */
    return null;
  }

  public Expression nameExpr() {
    if (this.expectingType) {
      this.expectingType = false;
      DataType type = readType();
      /*
      TODO
      */
    }
    /*
    Raw Strings
    */
    if (Objects.equals(this.tok.lit, "r")
        && this.peekTok.kind == TypeKind.String
        && !this.insideStrInterp) {
      return stringExpr();
    }
    String name = checkName();
    if (isType() || this.tok.kind == TypeKind.Lpar) {
      return callExpr(name);
    }
    return new FieldOrEnumExpression(false, name, false, null);
  }

  public MethodCall callExpr(String name) {
    TypeToken start = this.tok;
    List<DataType> typeArgs = null;
    if (isType()) {
      typeArgs = readTypeArgs();
    }
    check(TypeKind.Lpar);
    MethodCall mCall = new MethodCall(name, ListExt.List(), ListExt.List(), typeArgs);
    while (true) {
      List<Comment> comments = eatComments();
      if (this.tok.kind == TypeKind.Rpar
          || this.tok.kind == TypeKind.Eof
          || this.peekTok.kind == TypeKind.Colon) {
        break;
      }
      Argument arg = readArg();
      arg.arg.comments = comments;
      mCall.positionArgs.add(arg);
      if (this.tok.kind != TypeKind.Comma) {
        break;
      }
      check(TypeKind.Comma);
    }
    while (true) {
      List<Comment> comments2 = eatComments();
      if (this.tok.kind == TypeKind.Rpar
          || this.tok.kind == TypeKind.Eof
          || this.peekTok.kind != TypeKind.Colon) {
        break;
      }
      NamedArgument arg = readNamedArg();
      arg.beforeComments = comments2;
      mCall.namedArgs.add(arg);
      if (this.tok.kind != TypeKind.Comma) {
        break;
      }
      check(TypeKind.Comma);
    }
    check(TypeKind.Rpar);
    return mCall;
  }

  public List<Comment> eatComments() {
    List<Comment> comments = ListExt.asList();
    while (this.tok.kind == TypeKind.CommentSingle || this.tok.kind == TypeKind.CommentMulti) {
      comments.add(comment());
    }
    return comments;
  }

  public Comment comment() {
    TypeToken start = this.tok;
    String text = this.tok.lit;
    next();
    if (start.kind == TypeKind.CommentMulti) {
      text = trimLines(text, start.index - 1l);
    }
    return new Comment(
        text, start.kind == TypeKind.CommentMulti ? CommentType.MULTI : CommentType.SINGLE);
  }

  public String trimLines(String text, long index) {
    List<String> lines = StringExt.split(text, "\n");
    lines =
        IterableExt.toList(
            ListExt.map(
                lines,
                (l) -> {
                  String line = StringExt.trimLeft(l);
                  long removed = StringExt.length(l) - StringExt.length(line);
                  long from = removed;
                  if (index > 0l) {
                    removed = MathExt.minInt(index, from);
                  }
                  String res = StringExt.substring(l, from, 0l);
                  return res;
                }),
            false);
    return ListExt.join(lines, "\n");
  }

  public Expression indexExpr(Expression left) {
    TypeToken start = this.tok;
    next();
    Expression idx = expr(0l);
    check(TypeKind.Rsbr);
    return new ArrayAccess(idx, left);
  }

  public Expression dotExpr(Expression left, boolean checkNull, boolean notNull) {
    TypeToken start = this.tok;
    next();
    String name = checkName();
    if (isType() || this.tok.kind == TypeKind.Lpar) {
      MethodCall call = callExpr(name);
      call.on = left;
      call.checkNull = checkNull;
      call.notNull = notNull;
      return call;
    } else {
      return new FieldOrEnumExpression(checkNull, name, notNull, left);
    }
  }

  public Argument readArg() {
    Expression exp = expr(0l);
    List<Comment> comments2 = eatComments();
    return new Argument(comments2, exp);
  }

  public NamedArgument readNamedArg() {
    TypeToken start = this.tok;
    String name = checkName();
    check(TypeKind.Colon);
    Expression value = expr(0l);
    List<Comment> comments2 = eatComments();
    return new NamedArgument(comments2, name, value);
  }

  public String checkName() {
    String lit = this.tok.lit;
    check(TypeKind.Name);
    return lit;
  }
}
