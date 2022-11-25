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
  public static List<String> primitives =
      ListExt.asList("void", "bool", "int", "double", "dynamic", "num", "var");

  public TypeParser(Dart2NSContext context, TypeScanner scanner) {
    this.context = context;
    this.scanner = scanner;
  }

  public static List<TopDecl> parse(Dart2NSContext context, String content) {
    List<TopDecl> res = ListExt.asList();
    if (content.isEmpty()) {
      return res;
    }
    TypeParser p = new TypeParser(context, new TypeScanner(content));
    p.readFirstToken();
    try {
      while (p.readTopObject(res)) {}
      p.eatComments();
      p.check(TypeKind.Eof);
    } catch (RuntimeException e) {
      D3ELogger.error(e.getMessage());
    }
    res.forEach(
        (i) -> {
          context.add(i);
        });
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
    List<Annotation> annotations = readAnnotations();
    if (isKey(this.tok, "library")) {
      String lib = readLibrary();
      return true;
    } else if (isKey(this.tok, "export")) {
      next();
      String path = this.tok.lit;
      next();
      Export export = this.context.loadExport(path);
      readExport(export);
      return true;
    } else if (isKey(this.tok, "import")) {
      next();
      String path = this.tok.lit;
      next();
      Import importValue = this.context.loadImport(path);
      readImport(importValue);
      return true;
    } else if (isKey(this.tok, "part")) {
      if (isKey(this.peekTok, "of")) {
        next();
        next();
        List<String> str = ListExt.asList();
        while (this.tok.kind != TypeKind.Semicolon) {
          str.add(this.tok.lit);
          next();
        }
        this.context.addPartOf(ListExt.join(str, ""));
        next();
        return true;
      }
      next();
      this.context.loadPart(this.tok.lit);
      next();
      check(TypeKind.Semicolon);
      return true;
    }
    if (isKey(this.tok, "typedef")) {
      obj = readTypeDef(annotations);
    } else if (isKey(this.tok, "enum")) {
      obj = readEnum(annotations);
    } else if (this.tok.kind == TypeKind.Eof) {
      obj = null;
    } else if (isKey(this.tok, "class")
        || isKey(this.tok, "abstract")
        || isKey(this.tok, "mixin")
        || isKey(this.tok, "extension")) {
      obj = readClass(annotations, start);
    } else {
      List<ClassMember> listOfMembers = ListExt.asList();
      obj = readClassMember("", listOfMembers);
      for (ClassMember cm : listOfMembers) {
        list.add(cm);
      }
    }
    if (obj != null) {
      list.add(obj);
      return true;
    }
    return false;
  }

  public void readExport(Export ex) {
    if (isKey(this.tok, "show")) {
      next();
      while (this.tok.kind != TypeKind.Semicolon) {
        ex.show.add(this.tok.lit);
        next();
      }
    }
    if (isKey(this.tok, "hide")) {
      next();
      while (this.tok.kind != TypeKind.Semicolon) {
        ex.hide.add(this.tok.lit);
        next();
      }
    }
    check(TypeKind.Semicolon);
  }

  public void readImport(Import imp) {
    if (isKey(this.tok, "if")) {
      while (this.tok.kind != TypeKind.String) {
        next();
      }
      imp.conditioned = this.tok.lit;
      next();
    }
    if (isKey(this.tok, "differred")) {
      next();
      imp.differed = true;
    }
    if (isKey(this.tok, "as")) {
      next();
      imp.name = checkName();
    }
    if (isKey(this.tok, "show")) {
      next();
      while (this.tok.kind != TypeKind.Semicolon) {
        imp.show.add(this.tok.lit);
        next();
      }
    }
    if (isKey(this.tok, "hide")) {
      next();
      while (this.tok.kind != TypeKind.Semicolon) {
        imp.hide.add(this.tok.lit);
        next();
      }
    }
    check(TypeKind.Semicolon);
  }

  public String readLibrary() {
    next();
    String lib = checkName();
    while (this.tok.kind == TypeKind.Dot) {
      next();
      lib += "." + checkName();
    }
    check(TypeKind.Semicolon);
    return lib;
  }

  public List<Annotation> readAnnotations() {
    List<Annotation> res = ListExt.asList();
    while (this.tok.kind == TypeKind.At) {
      Annotation at = readAnnotation();
      res.add(at);
      ListExt.addAll(at.comments, eatComments());
    }
    return res;
  }

  public Annotation readAnnotation() {
    TypeToken start = this.tok;
    next();
    String name = checkName();
    MethodCall call = callExpr(name, ListExt.List());
    return new Annotation(call);
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
      List<Annotation> subAnn = readAnnotations();
      String id = checkName();
      data.values.add(id);
      eatComments();
      if (this.tok.kind != TypeKind.Comma) {
        break;
      } else {
        next();
        eatComments();
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
    DefType type = readDefType();
    FunctionType fnType = null;
    if (this.tok.kind != TypeKind.Assign) {
      String name = null;
      if (this.tok.kind == TypeKind.Lpar) {
        name = type.name;
      } else {
        name = checkName();
      }
      fnType = readFunctionType(type);
      fnType.name = name;
    } else {
      check(TypeKind.Assign);
      fnType = ((FunctionType) readType(false));
    }
    check(TypeKind.Semicolon);
    String value$ = fnType.name;
    if (value$ == null) {
      value$ = type.name;
    }
    Typedef def = new Typedef(value$, type, fnType);
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
      if (this.tok.kind == TypeKind.At) {
        readAnnotation();
      }
      if (this.tok.kind == TypeKind.Gt) {
        break;
      }
      if (this.tok.kind != TypeKind.Name) {
        return null;
      }
      String name = checkName();
      DataType type = null;
      if (isKey(this.tok, "extends")) {
        next();
        type = readType(false);
        if (type == null) {
          return null;
        }
      }
      params.params.add(new TypeParam(type, name));
      if (this.tok.kind != TypeKind.Comma) {
        break;
      } else {
        next();
      }
    }
    if (this.tok.kind != TypeKind.Gt) {
      return null;
    }
    next();
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
      } else {
        break;
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
      if (param.name == null) {
        param.name = param.dataType.name;
        param.dataType = null;
      }
      param.beforeComments = comments;
      params.add(param);
      if (this.tok.kind == TypeKind.Comma) {
        next();
      } else {
        break;
      }
    }
    check(end);
    return params;
  }

  public MethodParam readParam(boolean constructor) {
    TypeToken start = this.tok;
    List<Annotation> annotations = readAnnotations();
    String name = null;
    MethodParams fparams = null;
    Expression def = null;
    DataType type = null;
    boolean required = false;
    boolean covariant = false;
    boolean isFinal = false;
    if (isKey(this.tok, "required")) {
      next();
      required = true;
    }
    if (isKey(this.tok, "covariant")) {
      next();
      covariant = true;
    }
    if (isKey(this.tok, "final")) {
      next();
      isFinal = true;
    }
    if (!constructor || !(isKey(this.tok, "this") || isKey(this.tok, "super"))) {
      type = readType(true);
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
    eatComments();
    if (this.tok.kind == TypeKind.Name) {
      name = checkName();
      if (this.tok.kind == TypeKind.Lpar || isType()) {
        type = readFunctionType(type);
      }
    }
    if (this.tok.kind == TypeKind.Assign || this.tok.kind == TypeKind.Colon) {
      next();
      def = expr(0l);
    }
    return new MethodParam(
        annotations,
        type,
        def,
        false,
        name,
        required,
        hasThis ? "this" : hasSuper ? "super" : null);
  }

  public FunctionType readFunctionType(DataType type) {
    List<DataType> args = ListExt.asList();
    if (isType()) {
      args = readTypeArgs();
    }
    MethodParams params = null;
    if (this.tok.kind == TypeKind.Lpar) {
      params = readMethodParams(false);
    }
    var value$1 = params == null ? null : params.toFixedParams();
    List<MethodParam> value$ = value$1;
    if (value$ == null) {
      value$ = ListExt.asList();
    }
    FunctionType fnType = new FunctionType(false, value$, type, args);
    if (this.tok.kind == TypeKind.Question) {
      fnType.optional = true;
      next();
    }
    return fnType;
  }

  public ClassDecl readClass(List<Annotation> annotations, TypeToken start) {
    boolean isAbstract = false;
    if (isKey(this.tok, "abstract")) {
      isAbstract = true;
      next();
    }
    boolean isMixin = false;
    boolean isExtension = false;
    if (isKey(this.tok, "mixin")) {
      isMixin = true;
      next();
    } else if (isKey(this.tok, "extension")) {
      isExtension = true;
      next();
    } else {
      checkKey("class");
    }
    String name = checkName();
    ClassDecl cls = new ClassDecl(isMixin, name);
    cls.isExtension = isExtension;
    cls.isAbstract = isAbstract;
    if (this.tok.kind == TypeKind.Lt) {
      TypeParams params = readTypeParams();
      cls.generics = params;
    }
    boolean isMixinApplication = false;
    if (!isMixin && this.tok.kind == TypeKind.Assign) {
      isMixinApplication = true;
      next();
      cls.mixinApplicationType = readType(false);
    } else {
      if (!isExtension && !isMixin && isKey(this.tok, "extends")) {
        next();
        cls.extendType = readType(false);
      }
      if ((isExtension || isMixin) && isKey(this.tok, "on")) {
        next();
        while (true) {
          cls.ons.add(readType(false));
          if (this.tok.kind != TypeKind.Comma) {
            break;
          }
          next();
        }
      }
    }
    if (!isExtension && isKey(this.tok, "with")) {
      next();
      while (true) {
        cls.mixins.add(readType(false));
        if (this.tok.kind != TypeKind.Comma) {
          break;
        }
        next();
      }
    }
    if (!isExtension && isKey(this.tok, "implements")) {
      next();
      while (true) {
        cls.impls.add(readType(false));
        if (this.tok.kind != TypeKind.Comma) {
          break;
        }
        next();
      }
    }
    if (!isMixinApplication && this.tok.kind == TypeKind.Lcbr) {
      check(TypeKind.Lcbr);
      List<ClassMember> members = ListExt.asList();
      while (true) {
        List<Comment> comments = eatComments();
        if (this.tok.kind == TypeKind.Rcbr || this.tok.kind == TypeKind.Eof) {
          break;
        }
        ClassMember member = readClassMember(cls.name, members);
        member.comments = comments;
        members.add(member);
      }
      members.forEach(
          (i) -> {
            cls.add(i);
          });
      check(TypeKind.Rcbr);
    } else {
      check(TypeKind.Semicolon);
    }
    cls.annotations = annotations;
    return cls;
  }

  public ClassMember readClassMember(String className, List<ClassMember> listOfMembers) {
    List<Comment> comments = eatComments();
    TypeToken start = this.tok;
    List<Annotation> annotations = readAnnotations();
    ListExt.addAll(comments, eatComments());
    boolean isStatic = false;
    boolean isFinal = false;
    boolean isConst = false;
    boolean isFactory = false;
    boolean isAbstract = false;
    boolean isLate = false;
    boolean isExternal = false;
    ASyncType asyncType = ASyncType.NONE;
    if (isKey(this.tok, "external")) {
      isExternal = true;
      next();
    }
    if (isKey(this.tok, "static")) {
      isStatic = true;
      next();
    }
    if (isKey(this.tok, "late")) {
      isLate = true;
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
    boolean isConstructor =
        (Objects.equals(this.tok.lit, className))
            && (this.peekTok.kind == TypeKind.Lpar || this.peekTok.kind == TypeKind.Dot);
    String factoryName = null;
    if (!isFactory && !isConstructor) {
      if ((isConst || isFinal) && this.peekTok.kind == TypeKind.Assign) {
      } else {
        if (isKey(this.tok, "Function")) {
          type = readType(true);
        } else if (!isKey(this.tok, "operator")
            && (this.peekTok.kind != TypeKind.Lpar)
            && (this.tok.kind == TypeKind.Name
                && (this.peekTok.kind == TypeKind.Dot || isTypeName(this.tok.lit)))) {
          type = readType(false);
        }
      }
    }
    eatComments();
    String name = checkName();
    while (this.tok.kind == TypeKind.Semicolon
        || this.tok.kind == TypeKind.Assign
        || this.tok.kind == TypeKind.Comma) {
      Expression init = null;
      if (this.tok.kind == TypeKind.Assign) {
        next();
        init = expr(0l);
      }
      if (this.tok.kind == TypeKind.Comma) {
        next();
        listOfMembers.add(
            new FieldDecl(
                annotations, comments, isConst, isExternal, isFinal, name, isStatic, type, init));
        eatComments();
        name = checkName();
        continue;
      }
      check(TypeKind.Semicolon);
      return new FieldDecl(
          annotations, comments, isConst, isExternal, isFinal, name, isStatic, type, init);
    }
    /*
     Method Decal
    */
    boolean isSet = false;
    boolean isGet = false;
    Block body = null;
    Block init = null;
    Expression exp = null;
    DataType alternate = null;
    if (isFactory || (isConstructor && this.tok.kind == TypeKind.Dot)) {
      if (!(Objects.equals(name, className))) {
        error("Factory method should have same class name");
      }
      if (this.tok.kind == TypeKind.Dot) {
        check(TypeKind.Dot);
        factoryName = checkName();
      }
    } else {
      if (Objects.equals(name, "set") && this.tok.kind == TypeKind.Name) {
        isSet = true;
        name = checkName();
      } else if (Objects.equals(name, "get") && this.tok.kind == TypeKind.Name) {
        isGet = true;
        name = checkName();
      }
    }
    boolean operator = false;
    if (name.equals("operator")) {
      name = this.tok.lit;
      next();
      operator = true;
      while (this.tok.kind != TypeKind.Lpar) {
        name += this.tok.lit;
        next();
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
    eatComments();
    if (this.tok.kind == TypeKind.Colon) {
      next();
      init = new Block();
      Statement stmt = readStatement(ListExt.List(), true, true);
      init.statements.add(stmt);
      while (this.tok.kind == TypeKind.Comma) {
        next();
        stmt = readStatement(ListExt.List(), true, true);
        init.statements.add(stmt);
      }
    } else if (this.tok.kind == TypeKind.Assign) {
      next();
      alternate = readType(false);
      if (this.tok.kind == TypeKind.Dot) {
        next();
        String alFac = checkName();
      }
    } else {
      asyncType = readAsyncType();
    }
    String nativeString = null;
    if (isKey(this.tok, "native")) {
      next();
      nativeString = this.tok.lit;
      next();
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
    MethodDecl decl =
        new MethodDecl(
            annotations,
            asyncType,
            body,
            isConst,
            exp,
            isExternal,
            isFactory,
            factoryName,
            isFinal,
            typeParams,
            isGet,
            init,
            name,
            operator,
            params,
            type,
            isSet,
            isStatic);
    decl.nativeString = nativeString;
    return decl;
  }

  public boolean isTypeName(String name) {
    return name != null
        && (TypeParser.primitives.contains(name)
            || ParserUtil.isTypeName(name)
            || Objects.equals(name, "pragma"));
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
      Statement smt = readStatement(comments, false, false);
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

  public Statement readStatement(List<Comment> comments, boolean fromInit, boolean skipSemiColon) {
    comments = comments != null ? comments : eatComments();
    Statement smt = null;
    TypeToken start = this.tok;
    List<Annotation> annotations = readAnnotations();
    if (this.tok.kind == TypeKind.Name && this.peekTok.kind == TypeKind.Colon) {
      String name = checkName();
      next();
      return new LabelStatement(name);
    }
    if (isKey(this.tok, "return")) {
      smt = readReturn();
    } else if (isKey(this.tok, "yield")) {
      smt = readYield();
    } else if (isKey(this.tok, "await")) {
      next();
      smt = new AwaitExpression(expr(0l));
      if (!skipSemiColon) {
        check(TypeKind.Semicolon);
      }
    } else if (isKey(this.tok, "rethrow")) {
      next();
      smt = new RethrowStatement();
      check(TypeKind.Semicolon);
    } else if (isKey(this.tok, "throw")) {
      smt = readThrow(false);
    } else if (isKey(this.tok, "for")) {
      smt = readFor(false);
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
    } else if (isKey(this.tok, "final") || isKey(this.tok, "late")) {
      smt = readDecl();
      if (!skipSemiColon) {
        check(TypeKind.Semicolon);
      }
    } else if (isKey(this.tok, "break")) {
      next();
      String name = null;
      if (this.tok.kind == TypeKind.Name) {
        name = checkName();
      }
      check(TypeKind.Semicolon);
      smt = new Break(name);
    } else if (isKey(this.tok, "continue")) {
      next();
      String label = null;
      if (this.tok.kind == TypeKind.Name) {
        label = checkName();
      }
      check(TypeKind.Semicolon);
      smt = new Continue(label);
    } else if (this.tok.kind == TypeKind.Lcbr) {
      smt = readBlock(true);
    } else {
      save();
      smt = fromInit ? null : readDecl();
      if (smt != null) {
        drop();
      } else {
        restore();
        /*
         must be assignment or method call
        */
        boolean isPrefix = TypeToken.isPrefix(this.tok.kind);
        Expression exp = expr(1l);
        if (TypeToken.isAssign(this.tok.kind)) {
          /*
           Assignment
          */
          smt = readAssignment(exp);
        } else if (exp == null) {
          error("Unknown expression/statement");
        } else {
          smt = ((Statement) exp);
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

  public Assignment readAssignment(Expression left) {
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
    if (isKey(this.tok, "new")) {
      return null;
    }
    while (isKey(this.tok, "final") || isKey(this.tok, "late") || isKey(this.tok, "const")) {
      if (isKey(this.tok, "final")) {
        next();
        isFinal = true;
      }
      if (isKey(this.tok, "late")) {
        next();
        isLate = true;
      }
      if (isKey(this.tok, "const")) {
        next();
        isConst = true;
      }
    }
    DataType type = null;
    if (isFinal && this.peekTok.kind == TypeKind.Assign) {
      /*
       No type
      */
    } else {
      type = readType(false);
      if (type == null || this.tok.kind != TypeKind.Name) {
        return null;
      }
      if (type instanceof ValueType && !isTypeName(type.name)) {
        return null;
      }
    }
    List<NameAndValue> names = ListExt.asList();
    String name = checkName();
    if (isType() || this.tok.kind == TypeKind.Lpar) {
      /*
       May be inline function
      */
      TypeParams typeParams = null;
      if (isType()) {
        typeParams = readTypeParams();
        if (typeParams == null) {
          return null;
        }
      }
      MethodParams params = readMethodParams(false);
      Block block = null;
      Expression exp = null;
      ASyncType asyncType = readAsyncType();
      if (this.tok.kind == TypeKind.Lcbr) {
        block = readBlock(true);
      } else {
        check(TypeKind.Arrow);
        exp = expr(0l);
        check(TypeKind.Semicolon);
      }
      MethodDecl method =
          new MethodDecl(
              ListExt.List(),
              asyncType,
              block,
              false,
              exp,
              false,
              false,
              null,
              false,
              typeParams,
              false,
              null,
              name,
              false,
              params,
              type,
              false,
              false);
      return new InlineMethodStatement(method);
    }
    Expression value = null;
    while (this.tok.kind == TypeKind.Comma || this.tok.kind == TypeKind.Assign) {
      if (this.tok.kind == TypeKind.Comma) {
        next();
        names.add(new NameAndValue(name, value));
        name = checkName();
      } else {
        next();
        value = expr(0l);
      }
    }
    names.add(new NameAndValue(name, value));
    return new Declaration(isConst, isFinal, isLate, names, type);
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
        catchPart.onType = readType(false);
      }
      if (isKey(this.tok, "catch")) {
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
      }
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
    Statement thenVal = readStatement(ListExt.List(), false, false);
    eatComments();
    Statement elseS = null;
    if (isKey(this.tok, "else")) {
      next();
      elseS = readStatement(ListExt.List(), false, false);
    }
    return new IfStatement(elseS, test, thenVal);
  }

  public Statement readSwitch() {
    TypeToken start = this.tok;
    next();
    check(TypeKind.Lpar);
    Expression test = expr(0l);
    SwitchStatement ss = new SwitchStatement(test);
    check(TypeKind.Rpar);
    check(TypeKind.Lcbr);
    String label = null;
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
        if (label != null) {
          caseBlock.label = label;
          label = null;
        }
        while (true) {
          List<Comment> comments2 = eatComments();
          if (this.tok.kind == TypeKind.Rcbr
              || isKey(this.tok, "case")
              || isKey(this.tok, "default")) {
            break;
          }
          if (this.tok.kind == TypeKind.Name && this.peekTok.kind == TypeKind.Colon) {
            break;
          }
          Statement smt = readStatement(comments2, false, false);
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
          List<Comment> comments2 = eatComments();
          ss.defaults.add(readStatement(comments2, false, false));
        }
        break;
      } else if (this.tok.kind == TypeKind.Name && this.peekTok.kind == TypeKind.Colon) {
        label = checkName();
        next();
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
    Statement stmt = null;
    if (this.tok.kind == TypeKind.Lcbr) {
      stmt = readBlock(true);
    } else {
      stmt = readStatement(ListExt.List(), false, false);
    }
    return new WhileLoop(stmt, test);
  }

  public ForEachLoop readForEachLoop(boolean forCollection) {
    DataType type = null;
    if (!isKey(this.peekTok, "in")) {
      type = readType(false);
      if (type == null) {
        return null;
      }
    }
    if (this.tok.kind != TypeKind.Name) {
      return null;
    }
    String name = checkName();
    if (!isKey(this.tok, "in")) {
      return null;
    }
    next();
    Expression exp = expr(0l);
    if (this.tok.kind != TypeKind.Rpar) {
      return null;
    }
    next();
    Expression block = null;
    if (!forCollection) {
      block = readStatement(ListExt.List(), false, false);
    }
    return new ForEachLoop(block, exp, type, name);
  }

  public Statement readFor(boolean forCollection) {
    TypeToken start = this.tok;
    next();
    check(TypeKind.Lpar);
    boolean isFinal = false;
    if (isKey(this.tok, "final")) {
      next();
      isFinal = true;
    }
    save();
    ForEachLoop loop = readForEachLoop(forCollection);
    if (loop != null) {
      drop();
      return loop;
    } else {
      restore();
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
          next();
          break;
        }
        inits.add(readStatement(ListExt.List(), false, false));
        if (this.tok.kind != TypeKind.Comma) {
          break;
        }
        next();
      }
      Declaration decl = null;
      if ((ListExt.length(inits) > 0l) && (ListExt.get(inits, 0l) instanceof Declaration)) {
        decl = ((Declaration) ListExt.removeAt(inits, 0l));
      }
      Expression exp = null;
      if (this.tok.kind != TypeKind.Semicolon) {
        exp = expr(0l);
      }
      next();
      /*
       Skip over the semi-colon
      */
      List<Statement> resets = ListExt.asList();
      while (true) {
        if (this.tok.kind == TypeKind.Rpar || this.tok.kind == TypeKind.Eof) {
          break;
        }
        resets.add(readStatement(ListExt.List(), false, true));
        if (this.tok.kind != TypeKind.Comma) {
          break;
        }
        next();
      }
      check(TypeKind.Rpar);
      Expression block = null;
      if (!forCollection) {
        if (this.tok.kind != TypeKind.Semicolon) {
          block = readStatement(ListExt.List(), false, false);
        } else {
          next();
        }
      }
      return new ForLoop(block, decl, inits, resets, exp);
    }
  }

  public YieldExpression readYield() {
    next();
    boolean pointer = this.tok.kind == TypeKind.Mul;
    if (pointer) {
      next();
    }
    Expression exp = expr(0l);
    check(TypeKind.Semicolon);
    return new YieldExpression(exp, pointer);
  }

  public ThrowStatement readThrow(boolean asExp) {
    TypeToken start = this.tok;
    next();
    ThrowStatement ret = new ThrowStatement();
    ret.exp = expr(0l);
    if (!asExp) {
      check(TypeKind.Semicolon);
    }
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
            } else if (isKey(this.tok, "throw")) {
              return readThrow(true);
            } else if (isKey(this.tok, "r") && this.peekTok.kind == TypeKind.String) {
              node = stringExpr();
            } else if (isKey(this.tok, "new")) {
              next();
              return expr(precedence);
            } else {
              node = nameExpr();
            }
          }
          break;
        }
      case Hash:
        {
          {
            node = symbolExpr();
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
            ArrayExpression arr = arrayInit(ArrayType.List);
            node = arr;
          }
          break;
        }
      case Lcbr:
        {
          {
            ArrayExpression arr = arrayInit(ArrayType.Map);
            node = arr;
          }
          break;
        }
      case Lt:
        {
          {
            check(TypeKind.Lt);
            DataType type = readType(false);
            if (this.tok.kind == TypeKind.Gt) {
              check(TypeKind.Gt);
              boolean isSet = this.tok.kind == TypeKind.Lcbr;
              ArrayExpression arr = arrayInit(isSet ? ArrayType.Set : ArrayType.List);
              arr.enforceType = type;
              node = arr;
            } else {
              check(TypeKind.Comma);
              DataType valueType = readType(false);
              check(TypeKind.Gt);
              ArrayExpression arr = arrayInit(ArrayType.Map);
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
    eatComments();
    while (precedence < this.tok.getPrecedence()) {
      if (this.tok.kind == TypeKind.Dot) {
        node = dotExpr(node, false, false);
      } else if (this.tok.kind == TypeKind.DotDot) {
        node = dotDotExpr(node, false, false);
      } else if (this.tok.kind == TypeKind.Lpar) {
        node = fnCallExp(node);
      } else if (this.tok.kind == TypeKind.Lsbr) {
        node = indexExpr(node, false, false);
      } else if (isKey(this.tok, "as")) {
        next();
        DataType type = readType(false);
        node = new TypeCastOrCheckExpression(false, type, node, false);
      } else if (isKey(this.tok, "is")) {
        next();
        boolean isNot = false;
        if (this.tok.kind == TypeKind.Not) {
          isNot = true;
          next();
        }
        DataType type = readType(false);
        node = new TypeCastOrCheckExpression(true, type, node, isNot);
      } else if (this.tok.kind == TypeKind.LeftShift) {
        TypeToken ttok = this.tok;
        next();
        Expression right = expr(precedence - 1l);
        if (ttok.kind == TypeKind.Assign) {
          node = new Assignment(node, ttok.lit, right);
        } else {
          node = new BinaryExpression(node, ttok.lit, right);
        }
      } else if (TypeToken.isInfix(this.tok.kind)) {
        node = infixExpr(node);
      } else if (this.tok.kind == TypeKind.Not && this.peekTok.kind == TypeKind.Dot) {
        next();
        node = dotExpr(node, false, true);
      } else if (this.tok.kind == TypeKind.Inc
          || this.tok.kind == TypeKind.Dec
          || this.tok.kind == TypeKind.Not) {
        if (this.tok.kind == TypeKind.Not
            && this.peekTok.kind == TypeKind.Lt
            && isBeside(this.tok, this.peekTok)) {
          next();
          List<DataType> typeArgs = readTypeArgs();
          MethodCall call = callExpr(null, typeArgs);
          call.notNull = true;
          call.on = node;
        } else {
          node = new PostfixExpression(node, this.tok.lit);
          next();
        }
      } else if (this.tok.kind == TypeKind.Question) {
        next();
        if (this.tok.kind == TypeKind.Dot) {
          node = dotExpr(node, true, false);
        } else if (this.tok.kind == TypeKind.DotDot) {
          node = dotDotExpr(node, true, false);
        } else if (this.tok.kind == TypeKind.Lsbr) {
          node = indexExpr(node, true, false);
        } else {
          return ternaryExpr(node);
        }
      } else {
        return node;
      }
      if (this.tok.kind == TypeKind.CommentSingle || this.tok.kind == TypeKind.CommentMulti) {
        ListExt.addAll(node.comments, eatComments());
      }
    }
    if (node != null) {
      node.comments = comments;
    }
    return node;
  }

  public boolean isType() {
    return this.tok.kind == TypeKind.Lt;
  }

  public Expression parOrLambdaExpr() {
    TypeToken start = this.tok;
    if (this.tok.kind == TypeKind.Lpar && this.peekTok.kind == TypeKind.Rpar) {
      next();
      return lambdaExp();
    }
    check(TypeKind.Lpar);
    save();
    Expression exp = null;
    if (this.tok.kind != TypeKind.Rpar) {
      exp = lambdaExp();
    }
    if (exp == null) {
      restore();
      exp = expr(0l);
      check(TypeKind.Rpar);
      return new ParExpression(exp);
    } else {
      drop();
      return exp;
    }
  }

  public Expression lambdaExp() {
    TypeToken start = this.tok;
    List<Param> params = ListExt.asList();
    while (this.tok.kind != TypeKind.Rpar) {
      if (this.tok.lit.equals("_")) {
        params.add(new Param("_", null));
        next();
        if (this.tok.kind == TypeKind.Comma) {
          next();
        } else {
          break;
        }
        continue;
      }
      if ((this.peekTok.kind == TypeKind.Rpar || this.peekTok.kind == TypeKind.Comma)) {
        /*
         Name without type
        */
        if (this.tok.kind != TypeKind.Name) {
          return null;
        }
        String name = checkName();
        params.add(new Param(name, null));
      } else {
        DataType type = readType(false);
        if (this.tok.kind != TypeKind.Name) {
          return null;
        }
        String name = checkName();
        params.add(new Param(name, type));
      }
      if (this.tok.kind == TypeKind.Comma) {
        next();
      } else {
        break;
      }
    }
    if (this.tok.kind != TypeKind.Rpar) {
      return null;
    }
    next();
    LambdaExpression exp = new LambdaExpression(params);
    exp.asyncType = readAsyncType();
    if (this.tok.kind == TypeKind.Arrow) {
      next();
      Expression val = expr(1l);
      if (TypeToken.isAssign(this.tok.kind)) {
        /*
         Added because Assignment is only being read as part of a statement.
        */
        val = readAssignment(val);
      }
      exp.expression = val;
    } else if (this.tok.kind == TypeKind.Lcbr) {
      exp.body = readBlock(true);
    } else {
      return null;
    }
    return exp;
  }

  public ASyncType readAsyncType() {
    if (isKey(this.tok, "async")) {
      next();
      if (this.tok.kind == TypeKind.Mul) {
        next();
        return ASyncType.ASYNCP;
      }
      return ASyncType.ASYNC;
    }
    if (isKey(this.tok, "sync")) {
      next();
      next();
      return ASyncType.SYNCP;
    }
    return ASyncType.NONE;
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
    if (this.tok.kind != TypeKind.Colon) {
      return null;
    } else {
      next();
    }
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
    String lit = ttok.lit;
    long precedence = this.tok.getPrecedence();
    next();
    if (this.prevTok.kind == TypeKind.Gt && this.tok.kind == TypeKind.Gt) {
      next();
      precedence = TypeToken.preShift;
      lit = ">>";
    }
    if (isKey(ttok, "as") || isKey(ttok, "is")) {
      this.expectingType = true;
    }
    Expression right = expr(precedence);
    if (ttok.kind == TypeKind.Assign) {
      return new Assignment(left, ttok.lit, right);
    }
    return new BinaryExpression(left, lit, right);
  }

  public DataType readValueType(boolean acceptFnWithNoRet) {
    if (this.tok.kind != TypeKind.Name) {
      return null;
    }
    String name = checkName();
    String packageValue = null;
    if (this.tok.kind == TypeKind.Dot) {
      next();
      packageValue = name;
      name = checkName();
    }
    ValueType type = new ValueType(name, false);
    type.in = packageValue;
    if (this.tok.kind == TypeKind.Lt) {
      List<DataType> typeArgs = readTypeArgs();
      if (typeArgs == null) {
        return null;
      }
      type.args = typeArgs;
    }
    DataType result = type;
    if (acceptFnWithNoRet && !isTypeName(name) && this.tok.kind == TypeKind.Lpar) {
      MethodParams params = readMethodParams(false);
      DataType returnType = new ValueType("void", false);
      var value$1 = params == null ? null : params.toFixedParams();
      List<MethodParam> value$ = value$1;
      if (value$ == null) {
        value$ = ListExt.asList();
      }
      FunctionType fnType = new FunctionType(false, value$, returnType, type.args);
      result = fnType;
    }
    if (this.tok.kind == TypeKind.Question && isBeside(this.prevTok, this.tok)) {
      result.optional = true;
      next();
    }
    return type;
  }

  public DefType readDefType() {
    eatComments();
    if (this.tok.kind != TypeKind.Name) {
      return null;
    }
    String name = checkName();
    String packageValue = null;
    if (this.tok.kind == TypeKind.Dot) {
      next();
      packageValue = name;
      name = checkName();
    }
    DefType type = new DefType(name, false);
    type.in = packageValue;
    if (this.tok.kind == TypeKind.Lt) {
      TypeParams params = readTypeParams();
      if (params == null) {
        return null;
      }
      type.params = params;
    }
    if (this.tok.kind == TypeKind.Question && isBeside(this.prevTok, this.tok)) {
      type.optional = true;
      next();
    }
    return type;
  }

  public boolean isBeside(TypeToken first, TypeToken next) {
    return first.pos + 1l == next.pos;
  }

  public DataType readType(boolean acceptFnWithNoRet) {
    if (isKey(this.tok, "Function")) {
      next();
      return readFunctionType(null);
    }
    DataType type = readValueType(acceptFnWithNoRet);
    if (type == null) {
      return null;
    }
    if (isKey(this.tok, "Function")) {
      next();
      type = readFunctionType(type);
    }
    return type;
  }

  public List<DataType> readTypeArgs() {
    TypeToken start = this.tok;
    check(TypeKind.Lt);
    List<DataType> args = ListExt.asList();
    while (true) {
      DataType sub = readType(false);
      if (sub == null) {
        return null;
      }
      args.add(sub);
      eatComments();
      if (this.tok.kind != TypeKind.Comma) {
        break;
      } else {
        next();
      }
    }
    if (this.tok.kind != TypeKind.Gt) {
      return null;
    }
    next();
    return args;
  }

  public void error(String msg) {
    D3ELogger.error(msg);
    throw new RuntimeException(msg);
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

  public ArrayExpression arrayInit(ArrayType type) {
    ArrayExpression arrExp = new ArrayExpression();
    arrExp.type = type;
    TypeKind start = type == ArrayType.List ? TypeKind.Lsbr : TypeKind.Lcbr;
    TypeKind end = type == ArrayType.List ? TypeKind.Rsbr : TypeKind.Rcbr;
    check(start);
    while (true) {
      List<Comment> comments = eatComments();
      if (this.tok.kind == end || this.tok.kind == TypeKind.Eof) {
        break;
      }
      ArrayItem item = arrayItem(arrExp, comments);
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
    return arrExp;
  }

  public ArrayItem arrayItem(ArrayExpression arrExp, List<Comment> comments) {
    ListExt.addAll(comments, eatComments());
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
              ArrayItem exp = arrayItem(arrExp, ListExt.List());
              ArrayItem elseS = null;
              if (isKey(this.tok, "else")) {
                next();
                elseS = arrayItem(arrExp, ListExt.List());
              }
              CollectionIf cif = new CollectionIf(elseS, test, exp);
              cif.beforeComments = comments;
              return cif;
            } else if (isKey(this.tok, "for")) {
              Statement stmt = readFor(true);
              ArrayItem value = arrayItem(arrExp, ListExt.List());
              CollectionFor cFor = new CollectionFor(stmt, value);
              cFor.beforeComments = comments;
              return cFor;
            } else {
              if (arrExp.type == ArrayType.Map) {
                Expression key = expr(0l);
                if (this.tok.kind == TypeKind.Colon) {
                  next();
                  Expression value = expr(0l);
                  return new MapItem(comments, key, value);
                } else {
                  arrExp.type = ArrayType.Set;
                  ExpressionArrayItem item = new ExpressionArrayItem(key);
                  item.beforeComments = comments;
                  return item;
                }
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

  public Symbol symbolExpr() {
    String value = "";
    next();
    while (true) {
      value = value + this.tok.lit;
      next();
      if (this.tok.kind == TypeKind.Dot) {
        value += ".";
        next();
      } else {
        break;
      }
    }
    return new Symbol(value);
  }

  public Expression stringExpr() {
    TypeToken start = this.tok;
    boolean isRaw = false;
    String value = "";
    do {
      isRaw = this.tok.kind == TypeKind.Name && Objects.equals(this.tok.lit, "r");
      if (isRaw) {
        next();
      }
      value = value + this.tok.lit;
      next();
      eatComments();
    } while (this.tok.kind == TypeKind.String || Objects.equals(this.tok.lit, "r"));
    LiteralExpression node;
    if (this.tok.kind != TypeKind.StrIntr) {
      node = new LiteralExpression(isRaw, LiteralType.TypeString, isRaw ? value : value);
      return node;
    } else {
      return readStringIntrExp(value, isRaw);
    }
  }

  public StringInterExp readStringIntrExp(String value, boolean isRaw) {
    StringInterExp exp = new StringInterExp(value);
    while (this.tok.kind == TypeKind.StrIntr) {
      next();
      Expression sub = expr(0l);
      exp.values.add(sub);
      exp.str += "%s";
      while (this.tok.kind == TypeKind.String) {
        exp.str += this.tok.lit;
        next();
        eatComments();
      }
    }
    return exp;
  }

  public Expression nameExpr() {
    if (this.expectingType) {
      this.expectingType = false;
      DataType type = readType(false);
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
    if (name.equals("await")) {
      return new AwaitExpression(expr(0l));
    }
    if (name.equals("const")) {
      return new ConstExpression(expr(0l));
    }
    if (this.tok.kind == TypeKind.Lt) {
      save();
      List<DataType> args = readTypeArgs();
      if (args == null) {
        restore();
      } else {
        drop();
        return callExpr(name, args);
      }
    }
    if (this.tok.kind == TypeKind.Lpar) {
      return callExpr(name, ListExt.List());
    }
    return new FieldOrEnumExpression(false, name, false, null);
  }

  public MethodCall callExpr(String name, List<DataType> typeArgs) {
    TypeToken start = this.tok;
    String onTypeName = null;
    if (this.tok.kind == TypeKind.Dot) {
      /*
       Call on Type
      */
      next();
      onTypeName = name;
      name = checkName();
    }
    if (this.tok.kind != TypeKind.Lpar) {
      return null;
    } else {
      next();
    }
    MethodCall mCall = new MethodCall(name, ListExt.List(), onTypeName, ListExt.List(), typeArgs);
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

  public Expression indexExpr(Expression left, boolean checkNull, boolean notNull) {
    TypeToken start = this.tok;
    next();
    Expression idx = expr(0l);
    check(TypeKind.Rsbr);
    return new ArrayAccess(checkNull, idx, notNull, left);
  }

  public Expression fnCallExp(Expression left) {
    MethodCall call = callExpr(null, ListExt.List());
    return new FnCallExpression(call, left);
  }

  public Expression dotExpr(Expression left, boolean checkNull, boolean notNull) {
    TypeToken start = this.tok;
    next();
    String name = checkName();
    List<DataType> typeArgs = ListExt.asList();
    if (isType()) {
      save();
      typeArgs = readTypeArgs();
      if (typeArgs != null) {
        drop();
      } else {
        restore();
      }
    }
    if (this.tok.kind == TypeKind.Lpar) {
      MethodCall call = callExpr(name, typeArgs);
      call.on = left;
      call.checkNull = checkNull;
      call.notNull = notNull;
      return call;
    }
    return new FieldOrEnumExpression(checkNull, name, notNull, left);
  }

  public Expression dotDotExpr(Expression left, boolean checkNull, boolean notNull) {
    TypeToken start = this.tok;
    CascadeExp exp = new CascadeExp(left);
    while (this.tok.kind == TypeKind.DotDot) {
      next();
      if (this.tok.kind == TypeKind.Name) {
        Expression sub = expr(0l);
        if (sub instanceof Statement) {
          exp.calls.add(((Statement) sub));
        } else {
          error("This is not a valid statement");
        }
      } else {
        Expression index = indexExpr(null, false, false);
        Assignment assign = readAssignment(index);
        exp.calls.add(assign);
      }
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
            this.scanner.doingDollor,
            this.scanner.insideInfo,
            this.peekTok,
            this.peekTok2,
            this.peekTok3,
            this.peekTok4,
            this.scanner.pos,
            this.prevTok,
            this.tok);
    frame.stack = ListExt.from(this.scanner.stack, false);
    this.savedFrames.add(frame);
  }

  public void restore() {
    if (this.savedFrames.isEmpty()) {
      error("No saved frames found");
    }
    TokenFrame frame = ListExt.removeLast(this.savedFrames);
    this.scanner.pos = frame.pos;
    this.scanner.insideInfo = frame.insideInfo;
    this.scanner.doingDollor = frame.doingDollor;
    this.scanner.stack = ListExt.from(frame.stack, false);
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
