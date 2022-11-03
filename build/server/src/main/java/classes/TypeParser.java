package classes;

import d3e.core.D3ELogger;
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
  public Dart2NSContext context;
  public List<TokenFrame> savedFrames = ListExt.asList();
  public static List<String> primitives = ListExt.asList("void", "bool", "int", "double", "dynamic", "num");

  public TypeParser(Dart2NSContext context, TypeScanner scanner) {
    this.context = context;
    this.scanner = scanner;
  }

  public static List<TopDecl> parse(Dart2NSContext context, String path) {
    List<TopDecl> res = ListExt.asList();
    String content = context.path2Content(path);
    if (content.isEmpty()) {
      return res;
    }
    context.push(path);
    TypeParser p = new TypeParser(context, new TypeScanner(content));
    p.readFirstToken();
    while (p.readTopObject(res)) {}
    p.eatComments();
    p.check(TypeKind.Eof);
    context.pop();
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
      obj = readClassMember("");
    }
    if (obj != null) {
      list.add(obj);
      return true;
    }
    return false;
  }

  public List<TopDecl> readImportOrExport() {
    next();
    String path = this.tok.lit;
    List<TopDecl> objs = TypeParser.parse(this.context, path);
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

  public static String readFileFromPath(String basePath, String path) {
    if (StringExt.startsWith(path, "package:", 0l)) {
    } else if (StringExt.startsWith(path, "dart:", 0l)) {
    } else {
    }
    return "";
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
    checkKey("typedef");
    ValueType type = readValueType();
    check(TypeKind.Assign);
    FunctionType fnType = (FunctionType) readType();
    check(TypeKind.Semicolon);
    Typedef def = new Typedef(type, fnType);
    def.annotations = annotations;
    return def;
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

  public MethodParams readMethodParams(boolean constructor) {
    check(TypeKind.Lpar);
    MethodParams params = new MethodParams();
    while (true) {
      List<Comment> comments = eatComments();
      if (this.tok.kind == TypeKind.Lsbr) {
        params.optionalParams = readParams(TypeKind.Lsbr, TypeKind.Rsbr, constructor);
      } else if (this.tok.kind == TypeKind.Lcbr) {
        params.namedParams = readParams(TypeKind.Lcbr, TypeKind.Rcbr, constructor);
      }
      if (this.tok.kind == TypeKind.Eof || this.tok.kind == TypeKind.Rpar) {
        break;
      }
      params.positionalParams.add(readParam(constructor));
      if (this.tok.kind == TypeKind.Comma) {
        next();
      }
    }
    check(TypeKind.Rpar);
    return params;
  }

  public List<MethodParam> readParams(TypeKind start, TypeKind end, boolean constructor) {
    List<MethodParam> params = ListExt.asList();
    check(start);
    while (true) {
      List<Comment> comments = eatComments();
      if (this.tok.kind == end || this.tok.kind == TypeKind.Eof) {
        break;
      }
      MethodParam param = readParam(constructor);
      param.beforeComments = comments;
      params.add(param);
      if (this.tok.kind == TypeKind.Comma) {
        next();
      }
    }
    check(end);
    return params;
  }

  public MethodParam readParam(boolean constructor) {
    TypeToken start = this.tok;
    List<Annotation> annotations = ListExt.asList();
    String name = null;
    MethodParams fparams = null;
    Expression def = null;
    DataType type = null;
    boolean required = false;
    if (isKey(this.tok, "required")) {
    	next();
      required = true;
    }
    if(!constructor || !(isKey(this.tok, "this") || isKey(this.tok, "super"))) {
    	type = readType();
    }
    boolean hasThis = false;
    if (constructor && isKey(this.tok, "this")) {
      hasThis = true;
      next();
      check(TypeKind.Dot);
    }
    boolean hasSuper = false;
    if (constructor && isKey(this.tok, "super")) {
        hasSuper = true;
        next();
        check(TypeKind.Dot);
    }
    
    name = checkName();
    
    if (this.tok.kind == TypeKind.Assign) {
      next();
      def = expr(0l);
    }
    boolean deprecated =
        ListExt.any(
            annotations,
            (x) -> {
              return Objects.equals(x.name, "deprecated");
            });
    return new MethodParam(
        annotations, type, def, deprecated, name, required, hasThis ? "this" : hasSuper? "super" : null);
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
      ClassMember member = readClassMember(cls.name);
      member.comments = comments;
      cls.members.add(member);
    }
    check(TypeKind.Rcbr);
    cls.annotations = annotations;
    return cls;
  }

  public ClassMember readClassMember(String className) {
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
    DataType type = null;
    boolean isConstructor = this.tok.lit.equals(className);
    if (!isFactory && !isConstructor) {
      type = readType();
    }
    String name = checkName();
    if (this.tok.kind == TypeKind.Semicolon || this.tok.kind == TypeKind.Assign) {
      Expression init = null;
      if (this.tok.kind == TypeKind.Assign) {
    	next();
        init = expr(0l);
      }
      check(TypeKind.Semicolon);
      return new FieldDecl(annotations, comments, isConst, isFinal, name, isStatic, type, init);
    }
    /*
     Method Decal
    */
    boolean isSet = false;
    boolean isGet = false;
    Block body = null;
    Block init = null;
    Expression exp = null;
    if (isFactory || (isConstructor && tok.kind == TypeKind.Dot)) {
      if (!(Objects.equals(name, className))) {
        error("Factory method should have same class name");
      }
      check(TypeKind.Dot);
      name = checkName();
    } else {
      if (Objects.equals(name, "set") && this.tok.kind == TypeKind.Name) {
        isSet = true;
        name = checkName();
      } else if (Objects.equals(name, "get") && this.tok.kind == TypeKind.Name) {
        isGet = true;
        name = checkName();
      }
    }
    TypeParams typeParams = null;
    if (this.tok.kind == TypeKind.Lt) {
      typeParams = readTypeParams();
    }
    MethodParams params = null;
    if (!isGet) {
      params = readMethodParams(isConstructor);
    }
    if (this.tok.kind == TypeKind.Colon) {
      next();
      init = new Block();
      Statement stmt = readStatement(ListExt.List(), true);
      init.statements.add(stmt);
      while(this.tok.kind == TypeKind.Comma) {
    	next();
        stmt = readStatement(ListExt.List(), true);
        init.statements.add(stmt);
      } 
    } else {
      if (isKey(this.tok, "async")) {
        next();
        asyncType = ASyncType.ASYNC;
      }
    }
    if (this.tok.kind == TypeKind.Lcbr) {
      body = readBlock(true);
    } else if (this.tok.kind == TypeKind.Arrow) {
      next();
      exp = expr(0l);
      check(TypeKind.Semicolon);
    } else {
      check(TypeKind.Semicolon);
    }
    return new MethodDecl(
        annotations,
        asyncType,
        body,
        isConst,
        exp,
        isFactory,
        null,
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
    } else if (isKey(this.tok, "final") 
    		|| isKey(this.tok, "late")
    		|| isKey(this.tok, "const")) {
      smt = readDecl();
      if (!skipSemiColon) {
        check(TypeKind.Semicolon);
      }
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
      save();
      smt = readDecl();
      if(smt != null) {
    	  drop();
      } else {
    	restore();
        /*
         must be assignment or method call
        */
    	  boolean isPrefix = TypeToken.isPrefix(this.tok.kind);
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
        } else if (exp instanceof CascadeExp) {
            smt = ((CascadeExp) exp);
        } else {
          error("Unknown expression/statement");
        }
      }
      if (!skipSemiColon && !(smt instanceof InlineMethodStatement)) {
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
    return new Assignment(left, op, val);
  }

  public Statement readDecl() {
    TypeToken start = this.tok;
    boolean isFinal = false;
    boolean isLate = false;
    boolean isConst = false;
    if(isKey(tok, "final")) {
    	next();
    	isFinal = true;
    }
    if(isKey(tok, "late")) {
    	next();
    	isLate = true;
    }
    if(isKey(tok, "const")) {
    	next();
    	isConst = true;
    }
    DataType type = readType();
    if (type == null || this.tok.kind != TypeKind.Name) {
    	return null;
    }
    String name = checkName();
    if(isType() || this.tok.kind == TypeKind.Lpar) {
    	// May be inline function
    	TypeParams typeParams = null;
    	if (this.tok.kind == TypeKind.Lt) {
    	   typeParams = readTypeParams();
    	}
    	MethodParams params = readMethodParams(false);
    	Block block = null;
    	Expression exp = null;
    	if(this.tok.kind == TypeKind.Lcbr) {
    		block = readBlock(true);
    	} else {
    		check(TypeKind.Arrow);
    		exp = expr(0l);
    	}
    	MethodDecl method = new MethodDecl(null, ASyncType.NONE, 
    			block, false, exp, false, null, false, 
    			typeParams, false, exp, name, params, type, false, false);
    	return new InlineMethodStatement(method);
    }
    Expression init = null;
    if (this.tok.kind != TypeKind.Semicolon) {
      if (!TypeToken.isAssign(this.tok.kind)) {
        return null;
      } else {
        next();
        /*
         Skip over the assign op
        */
      }
      init = expr(0l);
    }
    return new Declaration(init, name, type, isFinal, isLate, isConst);
  }

  public boolean isDeclaration() {
    /*
    Check if it simple declation i.e Integer a; or Integer b = __;
    */
    boolean simpleD =
        (this.tok.kind == TypeKind.Name
            && this.peekTok.kind == TypeKind.Name
            && (this.peekTok2.kind == TypeKind.Semicolon || this.peekTok2.kind == TypeKind.Assign));
    boolean isType =
        this.tok.kind == TypeKind.Name
            && this.peekTok.kind == TypeKind.Lt
            && this.peekTok2.kind == TypeKind.Name
            && (this.peekTok3.kind == TypeKind.Gt
                || this.peekTok3.kind == TypeKind.Comma
                || this.peekTok3.kind == TypeKind.Lt);
    return simpleD || isType;
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
    boolean isFinal = false;
    if(isKey(tok, "final")) {
    	next();
    	isFinal = true;
    }
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
            if(tok.kind == TypeKind.Gt) {
	            check(TypeKind.Gt);
	            ArrayExpression arr = arrayInit(true);
	            arr.enforceType = type;
	            node = arr;
            } else {
            	check(TypeKind.Comma);
            	DataType valueType = readType();
            	check(TypeKind.Gt);
            	ArrayExpression arr = arrayInit(false);
	            arr.enforceType = type;
	            arr.valueType = valueType;
	            node = arr;
            }
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
      } else if (this.tok.kind == TypeKind.DotDot) {
        node = dotDotExpr(node, false, false);
      } else if (this.tok.kind == TypeKind.Lpar) {
        node = fnCallExp(node);
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
      } else if (this.tok.kind == TypeKind.Inc 
    		  || this.tok.kind == TypeKind.Dec 
    		  || this.tok.kind == TypeKind.Not) {
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
        && (primitives.contains(this.peekTok.lit) 
        		|| Character.isUpperCase(this.peekTok.lit.charAt(0)))
        && (this.peekTok2.kind == TypeKind.Gt
            || this.peekTok2.kind == TypeKind.Comma
            || this.peekTok2.kind == TypeKind.Lt);
  }

  public Expression parOrLambdaExpr() {
    TypeToken start = this.tok;
    if(tok.kind == TypeKind.Lpar && peekTok.kind == TypeKind.Rpar) {
    	return lambdaExp();
    }
    save();
    check(TypeKind.Lpar);
    Expression exp = null;
    if (this.tok.kind != TypeKind.Rpar) {
      exp = expr(0l);
    }
    boolean isLambda = this.tok.kind != TypeKind.Rpar;
    if (!isLambda) {
      drop();
      next();
      return new ParExpression(exp);
    } else {
      restore();
      return lambdaExp();
    }
  }

  public Expression lambdaExp() {
    TypeToken start = this.tok;
    check(TypeKind.Lpar);
    List<Param> params = ListExt.asList();
    while (this.tok.kind != TypeKind.Rpar) {
      DataType type = readType();
      String name = checkName();
      params.add(new Param(name, type));
      if (this.tok.kind == TypeKind.Comma) {
    	  next();
      } else {
    	  break;
      }
    }
    next();
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
    List<Param> params = ListExt.asList(new Param(name, null));
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

  public ValueType readValueType() {
	if(this.tok.kind != TypeKind.Name) {
		return null;
	}
    String name = checkName();
    String packageValue = null;
    if (this.tok.kind == TypeKind.Dot) {
      next();
      packageValue = name;
      name = checkName();
    }
    boolean nextType = isType();
    ValueType type = new ValueType(name, false);
    type.in = packageValue;
    if (nextType) {
      type.args = readTypeArgs();
    }
    if (this.tok.kind == TypeKind.Question) {
      type.optional = true;
      next();
    }
    return type;
  }

  public DataType readType() {
    DataType type = readValueType();
    if(type == null) {
    	return null;
    }
    if (isKey(this.tok, "Function")) {
      next();
      MethodParams params = readMethodParams(false);
      FunctionType fnType = new FunctionType(false, params, type);
      if (this.tok.kind == TypeKind.Question) {
        fnType.optional = true;
        next();
      }
      type = fnType;
    }
    return type;
  }

  public List<DataType> readTypeArgs() {
    TypeToken start = this.tok;
    check(TypeKind.Lt);
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
//    if (this.tok.kind == TypeKind.Gt) {
//      next();
//    }
    check(TypeKind.Gt);
    return args;
  }

  public void error(String msg) {
    D3ELogger.error(msg);
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
    TypeKind start = isList ? TypeKind.Lsbr : TypeKind.Lcbr;
    TypeKind end = isList ? TypeKind.Rsbr : TypeKind.Rcbr;
    check(start);
    while (true) {
      List<Comment> comments = eatComments();
      if (this.tok.kind == end || this.tok.kind == TypeKind.Eof) {
        break;
      }
      ArrayItem item = arrayItem(comments, isList);
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

  public ArrayItem arrayItem(List<Comment> comments, boolean isList) {
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
              ArrayItem exp = arrayItem(ListExt.List(), isList);
              ArrayItem elseS = null;
              if (isKey(this.tok, "else")) {
                next();
                elseS = arrayItem(ListExt.List(), isList);
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
              ArrayItem value = arrayItem(ListExt.List(), isList);
              CollectionFor cFor = new CollectionFor(exp, type, name, value);
              cFor.beforeComments = comments;
              return cFor;
            } else {
            	if(!isList) {
                	Expression key = expr(0l);
                	check(TypeKind.Colon);
                	Expression value = expr(0l);
                	return new MapItem(key, value, comments);
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
        (ttok.kind == TypeKind.Minus ? expr(TypeToken.preUnPrefix) : expr(TypeToken.preUnPrefix));
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
    while(this.peekTok.kind == TypeKind.String) {
    	next();
    	value = value + this.tok.lit;
    }
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
    if(name.equals("await")) {
    	return new AwaitExpression(expr(0l));
    }
    if(name.equals("const")) {
    	return new ConstExpression(expr(0l));
    }
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
    if(this.tok.kind == TypeKind.Dot) {
    	// Call on Type
    	next();
    	name = checkName();
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
  
  public Expression fnCallExp(Expression left) {
	 MethodParams params = readMethodParams(false);
	 return new FnCallExpression(left, params);
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
  
  public Expression dotDotExpr(Expression left, boolean checkNull, boolean notNull) {
	    TypeToken start = this.tok;
	    CascadeExp exp = new CascadeExp(left);
	    while (this.tok.kind == TypeKind.DotDot) {
	      next();
	      String name = checkName();
	      MethodCall call = callExpr(name);
	      call.on = left;
	      call.checkNull = checkNull;
	      call.notNull = notNull;
	      exp.calls.add(call);
	    }
	    return exp;
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

  public void save() {
    TokenFrame frame =
        new TokenFrame(
            this.peekTok,
            this.peekTok2,
            this.peekTok3,
            this.peekTok4,
            this.scanner.pos,
            this.prevTok,
            this.tok);
    this.savedFrames.add(frame);
  }

  public void restore() {
    if (this.savedFrames.isEmpty()) {
      error("No saved frames found");
    }
    TokenFrame frame = ListExt.removeLast(this.savedFrames);
    this.scanner.pos = frame.pos;
    this.tok = frame.tok;
    this.prevTok = frame.tok;
    this.peekTok = frame.peekTok;
    this.peekTok2 = frame.peekTok2;
    this.peekTok3 = frame.peekTok3;
    this.peekTok4 = frame.peekTok4;
  }

  public void drop() {
    if (this.savedFrames.isEmpty()) {
      error("No saved frames found");
    }
    ListExt.removeLast(this.savedFrames);
  }
}
