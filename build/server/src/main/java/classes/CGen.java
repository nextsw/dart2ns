package classes;

import d3e.core.D3ELogger;
import d3e.core.IntegerExt;
import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.SetExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CGen extends ResolveContext implements Gen {
  public Dart2NSContext context;
  public String base;
  public List<String> cppLines = ListExt.asList();
  public List<String> hppLines = ListExt.asList();
  public List<String> cpLines = ListExt.asList();
  public List<String> hpLines = ListExt.asList();
  public List<String> blockLines = ListExt.asList();
  public List<String> blockWords = ListExt.asList();
  public List<String> keywords =
      ListExt.asList("string", "int", "long", "bool", "auto", "char", "union");
  public List<String> primitives = ListExt.asList("int", "bool", "double", "num", "void");
  public long tempCount = 0l;

  public CGen() {}

  public void error(String msg) {
    D3ELogger.error(msg);
    long a = 0l;
    a++;
    /*
    throw Exception(msg);
    */
  }

  public TopDecl get(String name) {
    return this.context.get(name);
  }

  public String variable(String s) {
    if (this.keywords.contains(s)) {
      return s + "Value";
    } else {
      return s;
    }
  }

  public DataType commonType(DataType a, DataType b) {
    /*
    TODO we need to provide common type here
    */
    return a;
  }

  public DataType subType(DataType t, long index) {
    if (t instanceof ValueType) {
      ValueType vt = ((ValueType) t);
      if (ListExt.length(vt.args) <= index) {
        return ofUnknownType();
      } else {
        return ListExt.get(vt.args, index);
      }
    }
    return ofUnknownType();
  }

  public DataType ofUnknownType() {
    error("Unknown type");
    return this.objectType;
  }

  public void gen(Dart2NSContext context, String base) {
    this.context = context;
    this.base = base;
    generate();
  }

  public void generate() {
    List<DataType> types = ListExt.asList();
    for (Library lib : this.context.libs) {
      lib.addExtensions();
    }
    for (Library lib : this.context.libs) {
      lib.simplify(new Simplifier());
    }
    /*
     for(Library lib in context.libs){
         lib.validate();
     }
    */
    for (Library lib : this.context.libs) {
      lib.resolveFields(this);
    }
    for (Library lib : this.context.libs) {
      lib.resolveMethods(this);
      lib.collectUsedTypes(types);
    }
    /*
     for(Library lib in context.libs){
         // List<TopDecl> subs = List();
         // lib.subs(subs, Set());
         List<ClassDecl> classes = lib.objects.where(m => m is ClassDecl).map(m => m as ClassDecl).toList();
         for(ClassDecl cls in classes) {
             genLibraryClass(cls);
         }
         genLibrary(lib);
     }
    */
  }

  public void genLibrary(Library lib) {
    String outPath = lib.packagePath;
    List<String> split = StringExt.split(outPath, "/");
    String nameOnly = ListExt.last(split);
    String upper = StringExt.replaceAll(outPath, "/", "_").toUpperCase();
    hpp("#ifndef " + upper);
    hpp("#define " + upper);
    cpp("#include \"" + nameOnly + "lib.h\"");
    hpp("#include <base.h>");
    /*
     lib.exports.forEach((e){
         String path = e.path;
         if(e.path.startsWith('package:') || e.path.startsWith('dart:')) {
             cpp('#include <' + e.lib.packagePath +'.h>');
         } else {
             cpp('#include "' + path +'.h"');
         }
     });
     hpp('');
     lib.imports.forEach((i){
         String path = i.path;
         if(i.path.startsWith('package:') || i.path.startsWith('dart:')) {
             cpp('#include <' + i.lib.packagePath +'.h>');
         } else {
             cpp('#include "' + path +'.h"');
         }
     });
    */
    hpp("");
    List<TopDecl> objects = ListExt.from(lib.objects.values(), false);
    /*
     objects.sort((a, b) => a == b? 0 : a.usedTypes.contains(b.name)? 1 : -1);
    */
    Set<String> usedTypes = SetExt.Set();
    /*
     Collect parents used types too
    */
    objects.forEach(
        (obj) -> {
          if (obj instanceof ClassDecl) {
          } else {
            for (DataType t : obj.usedTypes) {
              if (t != null) {
                t.collectUsedTypes(usedTypes);
              }
            }
          }
        });
    SetExt.removeAll(usedTypes, this.primitives);
    usedTypes.remove("void");
    usedTypes.remove("Object");
    List<String> importPaths = ListExt.asList();
    for (String str : usedTypes) {
      /*
       Lets import those specific types
      */
      TopDecl top = this.context.get(str);
      if (top != null) {
        String p = top.getPackagePath();
        if (!importPaths.contains(p)) {
          importPaths.add(p);
        }
      }
    }
    for (String str : importPaths) {
      hpp("#include <" + str + ".h>");
    }
    objects.forEach(
        (obj) -> {
          if (obj instanceof Enum) {
            genEnum(((Enum) obj));
          }
        });
    objects.forEach(
        (obj) -> {
          if (obj instanceof Typedef) {
            genTypeDef(((Typedef) obj));
          }
        });
    objects.forEach(
        (obj) -> {
          if (obj instanceof FieldDecl) {
            genFieldDecl(
                ((FieldDecl) obj),
                null,
                true,
                0l,
                (x) -> {
                  hp(x);
                });
            genFieldDecl(
                ((FieldDecl) obj),
                null,
                false,
                0l,
                (x) -> {
                  cp(x);
                });
          }
        });
    objects.forEach(
        (obj) -> {
          if (obj instanceof MethodDecl) {
            MethodDecl method = ((MethodDecl) obj);
            genMethodDecl(null, method, 0l);
          }
        });
    hpp("");
    /*
     if(lib.parts.isNotEmpty) {
         hpp('// Parts');
     }
     lib.parts.forEach((i){
         hpp('#include "' + i.path +'.h"');
     });
    */
    hpp("");
    hpp("#endif");
    cpp("");
    FileUtils.writeFile(this.base + lib.packagePath + "lib.h", ListExt.join(this.hppLines, "\n"));
    FileUtils.writeFile(this.base + lib.packagePath + "lib.c", ListExt.join(this.cppLines, "\n"));
    this.cppLines.clear();
    this.hppLines.clear();
  }

  public void genLibraryClass(ClassDecl cls) {
    String path = cls.getPackagePath();
    String upper = StringExt.replaceAll(path, "/", "_").toUpperCase();
    hpp("#ifndef " + upper);
    hpp("#define " + upper);
    cpp("#include \"" + cls.name + ".h\"");
    hpp("#include <base.h>");
    hp("CLASS(");
    hp(cls.name);
    hl(")");
    Set<String> usedTypes = SetExt.Set();
    List<DataType> types = ListExt.List(0l);
    collectUsedTypesRec(cls, types);
    Set<String> fnTypeSignatures = SetExt.Set();
    List<FunctionType> fnTypes = ListExt.List(0l);
    for (DataType t : types) {
      if (t instanceof FunctionType) {
        FunctionType ft = (((FunctionType) t));
        ft.signature = ft.computeSignature();
        if (fnTypeSignatures.contains(ft.signature)) {
          continue;
        }
        fnTypeSignatures.add(ft.signature);
        fnTypes.add(ft);
      }
    }
    for (DataType t : types) {
      usedTypes.add(t.name);
    }
    SetExt.removeAll(usedTypes, this.primitives);
    usedTypes.remove("void");
    usedTypes.remove("Object");
    List<String> importPaths = ListExt.asList();
    for (String str : usedTypes) {
      /*
       Lets import those specific types
      */
      TopDecl top = this.context.get(str);
      if (top != null) {
        String p = top.getPackagePath();
        if (!importPaths.contains(p)) {
          importPaths.add(p);
        }
      }
    }
    for (String str : importPaths) {
      hpp("#include <" + str + ".h>");
    }
    /*
     lib.exports.forEach((e){
         String path = e.path;
         if(e.path.startsWith('package:') || e.path.startsWith('dart:')) {
             cpp('#include <' + e.lib.packagePath +'.h>');
         } else {
             cpp('#include "' + path +'.h"');
         }
     });
     hpp('');
     lib.imports.forEach((i){
         String path = i.path;
         if(i.path.startsWith('package:') || i.path.startsWith('dart:')) {
             cpp('#include <' + i.lib.packagePath +'.h>');
         } else {
             cpp('#include "' + path +'.h"');
         }
     });
    */
    hpp("");
    /*
     cls.resolve(this);
    */
    hp("CLASS(__");
    hp(cls.name);
    hl("Type)");
    for (FunctionType fnType : fnTypes) {
      hp("typedef struct ");
      hp(fnType.signature);
      hl(" {");
      hl("    void* __data;");
      hp("    ");
      functionTypeToString(
          fnType,
          "call",
          (x) -> {
            hp(x);
          });
      hl(";");
      hp("} ");
      hp(fnType.signature);
      hl(";");
    }
    genClassDecl(cls);
    hp("extern __");
    hp(cls.name);
    hp("Type* ___");
    hp(cls.name);
    hl(";");
    hpp("");
    hpp("#endif");
    cpp("");
    FileUtils.writeFile(this.base + path + ".h", ListExt.join(this.hppLines, "\n"));
    FileUtils.writeFile(this.base + path + ".c", ListExt.join(this.cppLines, "\n"));
    this.cppLines.clear();
    this.hppLines.clear();
  }

  public void collectUsedTypesRec(ClassDecl cls, List<DataType> types) {
    cls.collectUsedTypes();
    if (cls.extendType != null) {
      TopDecl top = this.context.get(cls.extendType.name);
      if (top instanceof ClassDecl) {
        ClassDecl parent = ((ClassDecl) top);
        collectUsedTypesRec(parent, types);
      }
    }
    ListExt.addAll(types, cls.usedTypes);
  }

  public void cpp(String line) {
    if (ListExt.isNotEmpty(this.cpLines)) {
      this.cppLines.add(ListExt.join(this.cpLines, ""));
      this.cpLines.clear();
    }
    this.cppLines.add(line);
  }

  public void hpp(String line) {
    if (ListExt.isNotEmpty(this.hpLines)) {
      this.hppLines.add(ListExt.join(this.hpLines, ""));
      this.hpLines.clear();
    }
    this.hppLines.add(line);
  }

  public void cp(String word) {
    this.cpLines.add(word);
  }

  public void hp(String word) {
    this.hpLines.add(word);
  }

  public void cl(String word) {
    this.cpLines.add(word);
    this.cppLines.add(ListExt.join(this.cpLines, ""));
    this.cpLines.clear();
  }

  public void bp(String word) {
    this.blockWords.add(word);
  }

  public void bl(String word) {
    this.blockWords.add(word);
    this.blockLines.add(ListExt.join(this.blockWords, ""));
    this.blockWords.clear();
  }

  public void hl(String word) {
    this.hpLines.add(word);
    this.hppLines.add(ListExt.join(this.hpLines, ""));
    this.hpLines.clear();
  }

  public String valueTypeToString(ValueType v, boolean noPtr) {
    String res = v.name;
    TopDecl top = this.context.get(res);
    if (Objects.equals(res, "auto")) {
      res = "auto";
    } else if (Objects.equals(res, "dynamic")
        || Objects.equals(res, "var")
        || StringExt.length(res) == 1l) {
      res = "Object*";
    } else if (top instanceof Typedef || top instanceof Enum) {
      /*
       No * in Typedef or enum
      */
    } else if (!noPtr && !this.primitives.contains(res)) {
      res += "*";
    }
    return res;
  }

  public void functionTypeToString(FunctionType f, String name, Xp xp) {
    if (name == null) {
      xp.apply(f.signature);
      return;
    }
    if ((f.returnType == null || Objects.equals(f.returnType.name, "void"))
        && name == null
        && (f.params == null || f.params.isEmpty())) {
      xp.apply("__Function");
      return;
    }
    if (f.returnType == null) {
      xp.apply("void ");
    } else {
      xp.apply(dataTypeToString(f.returnType, null, false));
    }
    xp.apply("(*");
    if (name != null) {
      xp.apply(name);
    }
    xp.apply(")(");
    xp.apply(
        IterableExt.join(
            ListExt.map(
                f.params,
                (p) -> {
                  return paramToString(p, true);
                }),
            ", "));
    xp.apply(")");
  }

  public String defTypeToString(DefType d) {
    return d.name;
  }

  public String dataTypeToString(DataType d, String name, boolean noPtr) {
    if (d instanceof ValueType) {
      return valueTypeToString(((ValueType) d), noPtr);
    } else if (d instanceof FunctionType) {
      List<String> res = ListExt.asList();
      functionTypeToString(
          ((FunctionType) d),
          null,
          (x) -> {
            res.add(x);
          });
      return ListExt.join(res, "");
    } else if (d instanceof DefType) {
      return defTypeToString(((DefType) d));
    }
    return "Unknown";
  }

  public void genClassDecl(ClassDecl c) {
    hp("typedef struct ");
    hp(c.name);
    hl(" {");
    hp("    __");
    hp(c.name);
    hl("Type* __type;");
    hl("    i32 __refcount;");
    declareClassFields(c, false);
    hp("} ");
    hp(c.name);
    hl(";");
    hpp("");
    hp("typedef struct __");
    hp(c.name);
    hl("Type {");
    hl("    i32 id;");
    if (c.extendType != null) {
      hp("    __");
      hp(c.extendType.name);
      hl("Type* super;");
    }
    declareClassFields(c, true);
    if (!c.isAbstract) {
      hp("    ");
      hp(c.name);
      hl("* (*__make)();");
    }
    declareClassMethods(c, SetExt.Set(), true);
    hp("} __");
    hp(c.name);
    hl("Type;");
    hpp("");
    c.getMethods()
        .forEach(
            (obj) -> {
              genMethodDecl(c, ((MethodDecl) obj), 0l);
            });
  }

  public void declareClassFields(ClassDecl c, boolean staticValue) {
    if (c.extendType != null && !(Objects.equals(c.name, "Object"))) {
      TopDecl top = this.context.get(c.extendType.name);
      if (top instanceof ClassDecl) {
        ClassDecl parent = ((ClassDecl) top);
        declareClassFields(parent, staticValue);
      }
    }
    hp("    // ");
    hp(c.name);
    hl(" fields");
    declareFields(c, staticValue);
  }

  public void declareFields(ClassDecl c, boolean staticValue) {
    c.getFields()
        .forEach(
            (m) -> {
              FieldDecl fd = ((FieldDecl) m);
              if (staticValue == fd.staticValue) {
                genFieldDecl(
                    fd,
                    null,
                    false,
                    1l,
                    (x) -> {
                      hp(x);
                    });
              }
              if (staticValue && fd.staticValue) {
                genFieldDecl(
                    fd,
                    null,
                    false,
                    1l,
                    (x) -> {
                      cp(x);
                    });
              }
            });
  }

  public void declareClassMethods(ClassDecl c, Set<String> added, boolean topLevel) {
    if (c.extendType != null) {
      TopDecl top = this.context.get(c.extendType.name);
      if (top instanceof ClassDecl) {
        ClassDecl parent = ((ClassDecl) top);
        declareClassMethods(parent, added, false);
      }
    }
    hp("    // ");
    hp(c.name);
    hl(" methods");
    c.getMethods()
        .forEach(
            (i) -> {
              MethodDecl m = ((MethodDecl) i);
              genClassMethodDecl(c, m, added, topLevel);
            });
  }

  public void genClassMethodDecl(ClassDecl c, MethodDecl m, Set<String> added, boolean topLevel) {
    boolean isConstructor = c != null && Objects.equals(c.name, m.name);
    if (isConstructor && !topLevel) {
      return;
    }
    if (!isConstructor && added.contains(m.name)) {
      return;
    }
    if (m.setter) {
      added.add("__set_" + m.name);
    } else {
      added.add(m.name);
    }
    hp(tab(1l));
    if (m.returnType == null) {
      if (isConstructor) {
        hp(c.name);
        hp("*");
      } else {
        hp("void ");
      }
    } else {
      hp(dataTypeToString(m.returnType, null, false));
    }
    if (c != null) {
      hp("(*");
    } else {
      hp(" ");
    }
    if (!ParserUtil.isNameChar(m.name)) {
      hp("_");
      if (m.params.getIsEmpty()) {
        hp("uminus");
      } else {
        hp(operatorToName(m.name));
      }
    } else {
      if (isConstructor) {
        if (m.factoryName != null) {
          hp(m.factoryName);
        } else {
          hp("new");
        }
      } else {
        if (m.setter) {
          hp("__set_");
        }
        hp(variable(m.name));
      }
    }
    if (c != null) {
      hp(")");
    }
    hp("(");
    boolean needThis = c != null && !m.staticValue;
    boolean needComma = false;
    if (!m.factory && (needThis || isConstructor)) {
      hp(c.name);
      hp("* this ");
      needComma = true;
    }
    if (m.params != null) {
      if (c != null) {
        updateSuperAndParams(m, c);
      }
      genMethodParams(
          m.allParams,
          c,
          needComma,
          (x) -> {
            hp(x);
          });
    }
    hp(")");
    hl(";");
  }

  public String paramToString(MethodParam p, boolean skipName) {
    if (Objects.equals(p.name, "")) {
      p.name = p.dataType.name;
      p.dataType = this.objectType;
    }
    String out = "";
    if (p.dataType != null) {
      out += dataTypeToString(p.dataType, null, false);
      if (this.scope != null) {
        this.scope.add(p.name, p.dataType);
      }
    } else {
      out += "Object*";
      if (this.scope != null) {
        this.scope.add(p.name, ofUnknownType());
      }
    }
    String name = p.name;
    if (!skipName) {
      out += " ";
      out += variable(name);
    }
    return out;
  }

  public List<MethodParam> sortMethodParams(MethodParams params) {
    List<MethodParam> out = ListExt.asList();
    ListExt.addAll(out, params.positionalParams);
    ListExt.addAll(out, params.optionalParams);
    ListExt.sort(
        params.namedParams,
        (a, b) -> {
          String value$ = a.name;
          if (value$ == null) {
            value$ = "";
          }
          String value$1 = b.name;
          if (value$1 == null) {
            value$1 = "";
          }
          return StringExt.compareTo((value$), (value$1));
        });
    ListExt.addAll(out, params.namedParams);
    out.forEach(
        (i) -> {
          if (i.name == null) {
            i.name = "";
          }
        });
    return out;
  }

  public void genEnum(Enum e) {
    hpp("typedef enum " + e.name + "{");
    e.values.forEach(
        (v) -> {
          hpp("    " + variable(v) + ",");
        });
    hp("} ");
    hp(e.name);
    hl(";");
  }

  public String operatorToName(String op) {
    /*
     < 	+ 	| 	>>>
     > 	/ 	^ 	[]
     <= 	~/ 	& 	[]=
    >= 	* 	<< 	~
    - 	% 	>> 	==
    */
    switch (op) {
      case "<":
        {
          return "lt";
        }
      case ">":
        {
          return "gt";
        }
      case "<=":
        {
          return "le";
        }
      case ">=":
        {
          return "ge";
        }
      case "-":
        {
          return "minus";
        }
      case "+":
        {
          return "plus";
        }
      case "/":
        {
          return "div";
        }
      case "~/":
        {
          return "div2";
        }
      case "*":
        {
          return "mul";
        }
      case "%":
        {
          return "mod";
        }
      case "|":
        {
          return "or";
        }
      case "^":
        {
          return "not";
        }
      case "&":
        {
          return "and";
        }
      case "<<":
        {
          return "lshift";
        }
      case ">>":
        {
          return "rshift";
        }
      case ">>>":
        {
          return "rshift2";
        }
      case "[]":
        {
          return "get";
        }
      case "[]=":
        {
          return "set";
        }
      case "~":
        {
          return "tilt";
        }
      case "==":
        {
          return "eq";
        }
      default:
        {
          return op;
        }
    }
  }

  public void genMethodDecl(ClassDecl c, MethodDecl m, long depth) {
    if (m.body == null && m.exp == null && m.init == null) {
      return;
    }
    if (m.external) {
      hp("extern ");
    }
    boolean isConstructor = c != null && Objects.equals(c.name, m.name);
    if (!isConstructor && !m.staticValue && !m.external && c != null) {
      /*
      hp('virtual ');
      */
    }
    if (m.returnType == null) {
      if (isConstructor) {
        hp(c.name);
        hp("*");
      } else {
        hp("void ");
      }
    } else {
      hp(dataTypeToString(m.returnType, null, false));
    }
    hp(" ");
    if (!ParserUtil.isNameChar(m.name)) {
      hp("_");
      if (m.params.getIsEmpty()) {
        hp("uminus");
      } else {
        hp(operatorToName(m.name));
      }
    } else {
      if (isConstructor) {
        if (m.factoryName != null) {
          hp(m.factoryName);
        } else {
          hp("new");
        }
      } else {
        if (m.setter) {
          hp("__set_");
        }
        hp(m.name);
      }
    }
    if (c != null) {
      hp("_");
      hp(c.name);
    }
    hp("(");
    boolean needThis = c != null && !m.staticValue;
    boolean needComma = false;
    if (!m.factory && (needThis || isConstructor)) {
      hp(c.name);
      hp("* this");
      needComma = true;
    }
    if (m.params != null) {
      if (c != null) {
        updateSuperAndParams(m, c);
      }
      genMethodParams(
          m.allParams,
          c,
          needComma,
          (x) -> {
            hp(x);
          });
    }
    hp(")");
    hl(";");
    hpp("");
    if (!m.staticValue) {
      this.instanceClass = c;
    } else {
      this.instanceClass = null;
    }
    if (m.returnType == null) {
      if (isConstructor) {
        cp(c.name);
        cp("* ");
      } else {
        cp("void ");
      }
    } else {
      cp(dataTypeToString(m.returnType, null, false));
      cp(" ");
    }
    if (!ParserUtil.isNameChar(m.name)) {
      cp("_");
      if (m.params.getIsEmpty()) {
        cp("uminus");
      } else {
        cp(operatorToName(m.name));
      }
    } else {
      if (isConstructor) {
        if (m.factoryName != null) {
          cp(m.factoryName);
        } else {
          cp("new");
        }
      } else {
        cp(m.name);
      }
    }
    if (c != null) {
      cp("_");
      cp(c.name);
    }
    cp("(");
    if (!m.factory && (needThis || isConstructor)) {
      cp(c.name);
      cp("* this ");
    }
    this.scope = new Scope(null, c);
    genMethodParams(
        m.allParams,
        c,
        needComma,
        (x) -> {
          cp(x);
        });
    cp(")");
    genBlock(
        m.body,
        0l,
        (x) -> {
          cp(x);
        },
        null);
    cpp("");
    this.tempCount = 0l;
  }

  public DataType getSuperParamType(ClassDecl c, String name) {
    ClassMember cm = getMember(c, c.name, null, false, null);
    if (cm == null) {
      /*
       No constructor found
      */
      return null;
    }
    MethodDecl md = ((MethodDecl) cm);
    if (md.params == null) {
      return null;
    }
    MethodParam param =
        ListExt.firstWhere(
            md.allParams,
            (m) -> {
              return Objects.equals(m.name, name);
            },
            null);
    if (param != null) {
      return param.dataType;
    }
    return null;
  }

  public void updateSuperAndParams(MethodDecl md, ClassDecl c) {
    if (md.params == null) {
      return;
    }
    List<MethodParam> superPosParams = ListExt.asList();
    List<MethodParam> superNamedParams = ListExt.asList();
    MethodDecl superCon = null;
    if (c.extendType != null) {
      ClassDecl superCls = ((ClassDecl) this.context.get(c.extendType.name));
      if (superCls != null) {
        superCon = ((MethodDecl) getMember(superCls, superCls.name, null, false, null));
      }
    }
    long x = 0l;
    for (MethodParam p : md.allParams) {
      if (Objects.equals(p.thisToken, "this")) {
        p.dataType = getFieldType(c, p.name);
      } else if (Objects.equals(p.thisToken, "super") && superCon != null) {
        superPosParams.add(p);
        MethodParam superParam =
            ListExt.length(superCon.allParams) > x ? ListExt.get(superCon.allParams, x) : null;
        var value$ = superParam == null ? null : superParam.dataType;
        p.dataType = value$;
      }
      x++;
    }
    if (superPosParams.isEmpty() || superNamedParams.isEmpty()) {
      return;
    }
    MethodCall superCall = null;
    if (md.init == null) {
      Block block = new Block();
      md.init = block;
      superCall = new MethodCall("super", ListExt.List(), null, ListExt.List(), ListExt.List());
      block.statements.add(superCall);
    } else {
      Block block = (((Block) md.init));
      superCall =
          ((MethodCall)
              ListExt.firstWhere(
                  block.statements,
                  (s) -> {
                    return s instanceof MethodCall
                        && Objects.equals((((MethodCall) s)).name, "super");
                  },
                  null));
      if (superCall == null) {
        superCall = new MethodCall("super", ListExt.List(), null, ListExt.List(), ListExt.List());
        ListExt.insert(block.statements, 0l, superCall);
      }
    }
    List<Argument> posArgs =
        IterableExt.toList(
            ListExt.map(
                superPosParams,
                (p) -> {
                  return new Argument(
                      ListExt.List(), new FieldOrEnumExpression(false, p.name, false, null));
                }),
            false);
    ListExt.insertAll(superCall.positionArgs, 0l, posArgs);
    List<NamedArgument> namedArgs =
        IterableExt.toList(
            ListExt.map(
                superPosParams,
                (p) -> {
                  return new NamedArgument(
                      ListExt.List(),
                      p.name,
                      new FieldOrEnumExpression(false, p.name, false, null));
                }),
            false);
    ListExt.insertAll(superCall.namedArgs, 0l, namedArgs);
  }

  public MethodParam getParamAtIndex(List<MethodParam> m, long i) {
    if (i < ListExt.length(m)) {
      return ListExt.get(m, i);
    }
    return null;
  }

  public MethodParam getParamByName(MethodParams m, String name) {
    return ListExt.firstWhere(
        m.namedParams,
        (p) -> {
          return Objects.equals(p.name, name);
        },
        null);
  }

  public void genMethodParams(List<MethodParam> params, ClassDecl c, boolean addedParams, Xp xp) {
    if (ListExt.isNotEmpty(params) && addedParams) {
      xp.apply(", ");
      /*
      for this*
      */
    }
    params.forEach(
        (p) -> {
          if (p.dataType == null) {
            xp.apply("Object*");
            if (this.scope != null) {
              this.scope.add(p.name, ofUnknownType());
            }
            xp.apply(" ");
            xp.apply(p.name);
          } else {
            xp.apply(paramToString(p, false));
          }
          if (!(Objects.equals(p, ListExt.last(params)))) {
            xp.apply(", ");
          }
        });
  }

  public boolean isOverriding(ClassDecl c, String name) {
    if (c.extendType == null) {
      return false;
    }
    TopDecl obj = this.context.get(c.extendType.name);
    if (!(obj instanceof ClassDecl)) {
      return false;
    }
    ClassDecl parent = ((ClassDecl) obj);
    obj = parent.get(name);
    if (obj == null || !(obj instanceof MethodDecl)) {
      return false;
    }
    return true;
  }

  public MethodCall removeSuperCall(Expression init) {
    /*
     We remove super from all init calls..
     so that we can generate like B() : A() {}
    */
    if (init instanceof Block) {
      Block block = ((Block) init);
      for (Statement s : block.statements) {
        if (s instanceof MethodCall) {
          MethodCall m = (((MethodCall) s));
          if (Objects.equals(m.name, "super")) {
            block.statements.remove(m);
            return m;
          }
        }
      }
    }
    return null;
  }

  public void genExp(Expression exp, long depth, Xp xp) {
    if (exp == null) {
      return;
    }
    if (exp instanceof FieldOrEnumExpression) {
      genFieldOrEnumExpression(((FieldOrEnumExpression) exp), depth, xp);
    } else if (exp instanceof ArrayAccess) {
      genArrayAccess(((ArrayAccess) exp), depth, xp);
    } else if (exp instanceof ArrayExpression) {
      genArrayExpression(((ArrayExpression) exp), depth, xp);
    } else if (exp instanceof ArrayItem) {
      genArrayItem(((ArrayItem) exp), depth, xp);
    } else if (exp instanceof Assignment) {
      genAssignment(((Assignment) exp), depth, xp);
    } else if (exp instanceof AwaitExpression) {
      genAwaitExpression(((AwaitExpression) exp), depth, xp);
    } else if (exp instanceof BinaryExpression) {
      genBinaryExpression(((BinaryExpression) exp), depth, xp);
    } else if (exp instanceof Block) {
      genBlock(((Block) exp), depth, xp, null);
    } else if (exp instanceof Break) {
      genBreak(((Break) exp), depth, xp);
    } else if (exp instanceof CascadeExp) {
      genCascadeExp(((CascadeExp) exp), depth, xp);
    } else if (exp instanceof ConstExpression) {
      genConstExpression(((ConstExpression) exp), depth, xp);
    } else if (exp instanceof Continue) {
      genContinue(((Continue) exp), depth, xp);
    } else if (exp instanceof Declaration) {
      genDeclaration(((Declaration) exp), depth, xp);
    } else if (exp instanceof DoWhileLoop) {
      genDoWhileLoop(((DoWhileLoop) exp), depth, xp);
    } else if (exp instanceof FnCallExpression) {
      genFnCallExpression(((FnCallExpression) exp), depth, xp);
    } else if (exp instanceof ForEachLoop) {
      genForEachLoop(((ForEachLoop) exp), depth, xp);
    } else if (exp instanceof ForLoop) {
      genForLoop(((ForLoop) exp), depth, xp);
    } else if (exp instanceof IfStatement) {
      genIfStatement(((IfStatement) exp), depth, xp);
    } else if (exp instanceof InlineMethodStatement) {
      genInlineMethodStatement(((InlineMethodStatement) exp), depth, xp);
    } else if (exp instanceof LabelStatement) {
      genLabelStatement(((LabelStatement) exp), depth, xp);
    } else if (exp instanceof LambdaExpression) {
      genLambdaExpression(((LambdaExpression) exp), depth, xp);
    } else if (exp instanceof LiteralExpression) {
      genLiteralExpression(((LiteralExpression) exp), depth, xp);
    } else if (exp instanceof MethodCall) {
      genMethodCall(((MethodCall) exp), depth, xp);
    } else if (exp instanceof NullExpression) {
      genNullExpression(((NullExpression) exp), depth, xp);
    } else if (exp instanceof ParExpression) {
      genParExpression(((ParExpression) exp), depth, xp);
    } else if (exp instanceof PostfixExpression) {
      genPostfixExpression(((PostfixExpression) exp), depth, xp);
    } else if (exp instanceof PrefixExpression) {
      genPrefixExpression(((PrefixExpression) exp), depth, xp);
    } else if (exp instanceof ThrowStatement) {
      genThrowStatement(((ThrowStatement) exp), depth, xp);
    } else if (exp instanceof RethrowStatement) {
      genRethrowStatement(((RethrowStatement) exp), depth, xp);
    } else if (exp instanceof Return) {
      genReturn(((Return) exp), depth, xp);
    } else if (exp instanceof SwitchExpression) {
      genSwitchExpression(((SwitchExpression) exp), depth, xp);
    } else if (exp instanceof TerinaryExpression) {
      genTerinaryExpression(((TerinaryExpression) exp), depth, xp);
    } else if (exp instanceof TypeCastOrCheckExpression) {
      genTypeCastOrCheckExpression(((TypeCastOrCheckExpression) exp), depth, xp);
    } else if (exp instanceof WhileLoop) {
      genWhileLoop(((WhileLoop) exp), depth, xp);
    } else if (exp instanceof YieldExpression) {
      genYieldExpression(((YieldExpression) exp), depth, xp);
    } else if (exp instanceof TryCatcheStatment) {
      genTryCatchStatement(((TryCatcheStatment) exp), depth, xp);
    } else if (exp instanceof Symbol) {
      genSymbol(((Symbol) exp), xp);
    } else if (exp instanceof StringInterExp) {
      genStringIntr(((StringInterExp) exp), xp);
    } else {
    }
  }

  public String tab(long depth) {
    String s = "";
    while (depth > 0l) {
      s += "    ";
      depth--;
    }
    return s;
  }

  public void genStringIntr(StringInterExp exp, Xp xp) {
    xp.apply("__sf(\"");
    xp.apply(exp.str);
    xp.apply("\", ");
    for (Expression val : exp.values) {
      genExp(val, 0l, xp);
      if (!(Objects.equals(val, ListExt.last(exp.values)))) {
        xp.apply(", ");
      }
    }
    xp.apply(")");
  }

  public void genSymbol(Symbol exp, Xp xp) {
    xp.apply("__symbol(\"");
    xp.apply(exp.name);
    xp.apply("\")");
  }

  public void genArrayAccess(ArrayAccess exp, long depth, Xp xp) {
    genExp(exp.on, depth, xp);
    if (exp.checkNull) {
      xp.apply("?");
    } else if (exp.notNull) {
      /*
       No need for anything
      */
    }
    xp.apply(".get(");
    genExp(exp.index, depth, xp);
    xp.apply(")");
  }

  public long temp() {
    this.tempCount++;
    return this.tempCount;
  }

  public void genArrayExpression(ArrayExpression exp, long depth, Xp xp) {
    if (ListExt.any(
        exp.values,
        (v) -> {
          return !(v instanceof ExpressionArrayItem);
        })) {
      /*
       We need builder here
      */
      String tempVal = IntegerExt.toString(temp());
      switch (exp.type) {
        case List:
          {
            {
              if (exp.enforceType != null) {
                bp("List* list");
                bp(tempVal);
                bl(" = new_List();");
              }
              exp.values.forEach(
                  (v) -> {
                    if (v instanceof CollectionIf) {
                      CollectionIf cIf = ((CollectionIf) v);
                      bp("if (");
                      genExp(
                          cIf.test,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(") {\n");
                      bp("    add_List(list");
                      bp(tempVal);
                      bp(", ");
                      genExp(
                          cIf.thenItem,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(");\n");
                      bp("}");
                      if (cIf.elseItem != null) {
                        bp(" else {\n");
                        bp("    add_List(list");
                        bp(tempVal);
                        bp(", ");
                        genExp(
                            cIf.elseItem,
                            depth,
                            (x) -> {
                              bp(x);
                            });
                        bp(");\n");
                        bp("}");
                      }
                    } else if (v instanceof CollectionFor) {
                      CollectionFor cFor = ((CollectionFor) v);
                      genExp(
                          cFor.stmt,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp("{\n");
                      bp("    add_List(list");
                      bp(tempVal);
                      bp(", ");
                      genExp(
                          cFor.value,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(");\n");
                      bp("}");
                    } else if (v instanceof CollectionSpread) {
                      CollectionSpread spread = ((CollectionSpread) v);
                      bp("for (Iterator* _x");
                      bp(tempVal);
                      bp(" = ");
                      genExp(
                          spread.values,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp("; hasNext_Iterator(_x");
                      bp(tempVal);
                      bp("); next_Iterator(_x");
                      bp(tempVal);
                      bp(")) {\n");
                      bp("{\n");
                      bp("    add_List(list");
                      bp(tempVal);
                      bp(", get_Iterator(_x");
                      bp(tempVal);
                      bp("));\n");
                      bp("}");
                    } else {
                      bp("add_List(list");
                      bp(tempVal);
                      bp(", ");
                      genExp(
                          v,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(");\n");
                    }
                  });
              xp.apply("list");
              xp.apply(tempVal);
            }
            break;
          }
        case Map:
          {
            {
              if (exp.enforceType != null) {
                bp("Map* map");
                bp(tempVal);
                bl(" = new_Map();");
              }
              exp.values.forEach(
                  (v) -> {
                    if (v instanceof CollectionIf) {
                      CollectionIf cIf = ((CollectionIf) v);
                      if (!(cIf.thenItem instanceof MapItem)) {
                        bp("NeedMapItemHere");
                        return;
                      }
                      if (cIf.elseItem != null && !(cIf.elseItem instanceof MapItem)) {
                        bp("NeedMapItemHere");
                        return;
                      }
                      MapItem thenItem = ((MapItem) cIf.thenItem);
                      MapItem elseItem = ((MapItem) cIf.elseItem);
                      bp("if (");
                      genExp(
                          cIf.test,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(") ");
                      bp("{\n");
                      bp("    set_Map(map");
                      bp(tempVal);
                      bp(", ");
                      genExp(
                          thenItem.key,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(", ");
                      genExp(
                          thenItem.value,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(");\n");
                      bp("}");
                      if (elseItem != null) {
                        bp(" else {\n");
                        bp("    set_Map(map");
                        bp(tempVal);
                        bp(", ");
                        genExp(
                            elseItem.key,
                            depth,
                            (x) -> {
                              bp(x);
                            });
                        bp(", ");
                        genExp(
                            elseItem.value,
                            depth,
                            (x) -> {
                              bp(x);
                            });
                        bp(");\n");
                        bp("}");
                      }
                    } else if (v instanceof CollectionFor) {
                      CollectionFor cFor = ((CollectionFor) v);
                      if (!(cFor.value instanceof MapItem)) {
                        bp("NeedMapItemHere");
                        return;
                      }
                      MapItem item = ((MapItem) cFor.value);
                      genExp(
                          cFor.stmt,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp("{\n");
                      bp("    set_Map(map");
                      bp(tempVal);
                      bp(", ");
                      genExp(
                          item.key,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(", ");
                      genExp(
                          item.value,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(");\n");
                      bp("}");
                    } else if (v instanceof CollectionSpread) {
                      CollectionSpread spread = ((CollectionSpread) v);
                      bp("addAll_Map(map");
                      bp(tempVal);
                      bp(", ");
                      genExp(
                          spread.values,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(");");
                    } else {
                      if (!(v instanceof MapItem)) {
                        bp("NeedMapItemHere");
                        return;
                      }
                      MapItem item = ((MapItem) v);
                      bp("set_Map(map");
                      bp(tempVal);
                      bp(", ");
                      genExp(
                          item.key,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(", ");
                      genExp(
                          item.value,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(");\n");
                    }
                  });
              xp.apply("map");
              xp.apply(tempVal);
            }
            break;
          }
        case Set:
          {
            {
              if (exp.enforceType != null) {
                bp("Set* set");
                bp(tempVal);
                bl(" = new_Set();");
              }
              exp.values.forEach(
                  (v) -> {
                    if (v instanceof CollectionIf) {
                      CollectionIf cIf = ((CollectionIf) v);
                      bp("if (");
                      genExp(
                          cIf.test,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(") {\n");
                      bp("    add_Set(set");
                      bp(tempVal);
                      bp(", ");
                      genExp(
                          cIf.thenItem,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(");\n");
                      bp("}");
                      if (cIf.elseItem != null) {
                        bp(" else {\n");
                        bp("    add_Set(set");
                        bp(tempVal);
                        bp(", ");
                        genExp(
                            cIf.elseItem,
                            depth,
                            (x) -> {
                              bp(x);
                            });
                        bp(");\n");
                        bp("}");
                      }
                    } else if (v instanceof CollectionFor) {
                      CollectionFor cFor = ((CollectionFor) v);
                      genExp(
                          cFor.stmt,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp("{\n");
                      bp("    add_Set(set");
                      bp(tempVal);
                      bp(", ");
                      genExp(
                          cFor.value,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(");\n");
                      bp("}");
                    } else if (v instanceof CollectionSpread) {
                      CollectionSpread spread = ((CollectionSpread) v);
                      bp("for (auto _x");
                      bp(tempVal);
                      bp(" : ");
                      genExp(
                          spread.values,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(") {\n");
                      bp("{\n");
                      bp("    add_Set(set");
                      bp(tempVal);
                      bp(", _x");
                      bp(tempVal);
                      bp(");\n");
                      bp("}");
                    } else {
                      bp("set");
                      bp(tempVal);
                      bp(".add(");
                      genExp(
                          v,
                          depth,
                          (x) -> {
                            bp(x);
                          });
                      bp(");\n");
                    }
                  });
              xp.apply("set");
              xp.apply(tempVal);
            }
            break;
          }
        default:
          {
          }
      }
    } else {
      /*

      */
      switch (exp.type) {
        case List:
          {
            xp.apply("__makeList(");
            exp.values.forEach(
                (v) -> {
                  genExp(v, depth, xp);
                  if (!(Objects.equals(v, ListExt.last(exp.values)))) {
                    xp.apply(", ");
                  }
                });
            xp.apply(")");
            break;
          }
        case Map:
          {
            xp.apply("__makeMap(");
            xp.apply("__makeList(");
            exp.values.forEach(
                (v) -> {
                  MapItem i = ((MapItem) v);
                  genExp(i.key, depth, xp);
                  if (!(Objects.equals(v, ListExt.last(exp.values)))) {
                    xp.apply(", ");
                  }
                });
            xp.apply("), __makeList(");
            exp.values.forEach(
                (v) -> {
                  MapItem i = ((MapItem) v);
                  genExp(i.value, depth, xp);
                  if (!(Objects.equals(v, ListExt.last(exp.values)))) {
                    xp.apply(", ");
                  }
                });
            xp.apply(")");
            break;
          }
        case Set:
          {
            xp.apply("__makeSet(");
            exp.values.forEach(
                (v) -> {
                  genExp(v, depth, xp);
                  if (!(Objects.equals(v, ListExt.last(exp.values)))) {
                    xp.apply(", ");
                  }
                });
            xp.apply(")");
            break;
          }
        default:
          {
          }
      }
    }
  }

  public void genArrayItem(ArrayItem exp, long depth, Xp xp) {
    xp.apply("ArrayItem");
    /*
     TODO
    */
  }

  public void genAssignment(Assignment exp, long depth, Xp xp) {
    if (Objects.equals(exp.op, "??=")) {
      xp.apply("__or_assign(&");
      /*
      TODO Overload based on type
      */
      genExp(exp.left, depth, xp);
      xp.apply(", ");
      genExp(exp.right, depth, xp);
      xp.apply(")");
    } else {
      genExp(exp.left, depth, xp);
      xp.apply(" ");
      xp.apply(exp.op);
      xp.apply(" ");
      genExp(exp.right, depth, xp);
    }
  }

  public void genAwaitExpression(AwaitExpression exp, long depth, Xp xp) {
    xp.apply("await ");
    genExp(exp.exp, depth, xp);
  }

  public void genBinaryExpression(BinaryExpression exp, long depth, Xp xp) {
    if (Objects.equals(exp.op, "??")) {
      xp.apply("__or(");
      genExp(exp.left, depth, xp);
      xp.apply(", ");
      genExp(exp.right, depth, xp);
      xp.apply(")");
    } else if (Objects.equals(exp.op, "~/")) {
      xp.apply("(i64)(");
      genExp(exp.left, depth, xp);
      xp.apply(" / ");
      genExp(exp.right, depth, xp);
      xp.apply(")");
    } else {
      genExp(exp.left, depth, xp);
      xp.apply(" ");
      xp.apply(exp.op);
      xp.apply(" ");
      genExp(exp.right, depth, xp);
    }
  }

  public void genBlock(Block exp, long depth, Xp xp, Scope localScope) {
    if (exp == null) {
      return;
    }
    if (localScope == null) {
      localScope = new Scope(this.scope, null);
    }
    this.scope = localScope;
    xp.apply("{\n");
    exp.statements.forEach(
        (s) -> {
          xp.apply(tab(depth + 1l));
          long blockIndex = ListExt.length(this.cpLines);
          genExp(s, depth + 1l, xp);
          if ((s instanceof WhileLoop)
              || (s instanceof ForLoop)
              || (s instanceof ForEachLoop)
              || (s instanceof Block)
              || (s instanceof TryCatcheStatment)
              || (s instanceof IfStatement)) {
            /*
             No need for semicolon
            */
            xp.apply("\n");
          } else {
            xp.apply(";\n");
          }
          if (ListExt.isNotEmpty(this.blockWords)) {
            this.blockLines.add(ListExt.join(this.blockWords, ""));
            this.blockWords.clear();
          }
          if (ListExt.isNotEmpty(this.blockLines)) {
            Iterable<String> withTab =
                ListExt.map(
                    StringExt.split(ListExt.join(this.blockLines, "\n"), "\n"),
                    (i) -> {
                      return tab(depth) + i;
                    });
            ListExt.insertAll(this.cpLines, blockIndex, withTab);
            this.blockLines.clear();
          }
        });
    xp.apply(tab(depth));
    xp.apply("}");
    this.scope = this.scope.parent;
  }

  public void genBreak(Break exp, long depth, Xp xp) {
    xp.apply("break");
    if (exp.label != null) {
      xp.apply(" ");
      xp.apply(exp.label);
    }
  }

  public void genCascadeExp(CascadeExp exp, long depth, Xp xp) {
    String tempVal = IntegerExt.toString(temp());
    bp("auto _c");
    bp(tempVal);
    bp(" = ");
    genExp(
        exp.on,
        depth,
        (x) -> {
          bp(x);
        });
    bp(";\n");
    exp.calls.forEach(
        (c) -> {
          bp("_c");
          bp(tempVal);
          if (c instanceof Assignment && (((Assignment) c)).left instanceof ArrayAccess) {
            Assignment a = ((Assignment) c);
            ArrayAccess aa = ((ArrayAccess) a.left);
            bp("[");
            genExp(
                aa.index,
                depth,
                (x) -> {
                  bp(x);
                });
            bp("] ");
            bp(a.op);
            genExp(
                a.right,
                depth,
                (x) -> {
                  bp(x);
                });
          } else {
            bp(".");
            genExp(
                c,
                depth,
                (x) -> {
                  bp(x);
                });
          }
          bp(";\n");
        });
    xp.apply("_c");
    xp.apply(tempVal);
  }

  public void genConstExpression(ConstExpression exp, long depth, Xp xp) {
    /*
     xp('const ');
    */
    genExp(exp.exp, depth, xp);
    exp.resolvedType = exp.exp.resolvedType;
  }

  public void genContinue(Continue exp, long depth, Xp xp) {
    xp.apply("continue");
    if (exp.label != null) {
      xp.apply(" ");
      xp.apply(exp.label);
    }
    exp.resolvedType = ofUnknownType();
  }

  public void genDeclaration(Declaration exp, long depth, Xp xp) {
    if (exp.type != null) {
      xp.apply(dataTypeToString(exp.type, null, false));
    } else {
      xp.apply("auto");
    }
    xp.apply(" ");
    DataType resolvedType = null;
    for (NameAndValue n : exp.names) {
      xp.apply(n.name);
      if (n.value != null) {
        xp.apply(" = ");
        genExp(n.value, 0l, xp);
        resolvedType = n.value.resolvedType;
      }
      if (exp.type == null || Objects.equals(exp.type.name, "var")) {
        DataType value$ = resolvedType;
        if (value$ == null) {
          value$ = ofUnknownType();
        }
        this.scope.add(n.name, value$);
      } else {
        this.scope.add(n.name, exp.type);
      }
      if (!(Objects.equals(ListExt.last(exp.names), n))) {
        xp.apply(", ");
      }
    }
  }

  public void genDoWhileLoop(DoWhileLoop exp, long depth, Xp xp) {
    xp.apply("do ");
    genBlock(exp.body, depth, xp, null);
    xp.apply(" while (");
    genExp(exp.test, depth, xp);
    xp.apply(")");
  }

  public String getRecentCast(String name) {
    Scope s = this.scope;
    while (s != null) {
      if (s.casts.containsKey(name)) {
        return s.casts.get(name);
      }
      s = s.parent;
    }
    return null;
  }

  public ClassDecl computeCastType(Expression exp) {
    if (exp instanceof FieldOrEnumExpression) {
      String name = (((FieldOrEnumExpression) exp)).name;
      String typeName = getRecentCast(name);
      if (typeName != null) {
        TopDecl dec = this.context.get(typeName);
        if (dec != null && dec instanceof ClassDecl) {
          return ((ClassDecl) dec);
        }
      }
    }
    return null;
  }

  public ClassMember getMember(
      ClassDecl c, String name, MemberFilter filter, boolean noSuperCons, DataType thisType) {
    if (c == null) {
      return null;
    }
    ClassMember m = c.get(name);
    if (m != null) {
      if (noSuperCons && Objects.equals(m.name, c.name)) {
        return null;
      }
      if (filter == null) {
        return m;
      }
      switch (filter) {
        case AllFields:
          {
            if (m instanceof MethodDecl) {
              return null;
            }
            break;
          }
        case AllMethods:
          {
            if (m instanceof FieldDecl) {
              return null;
            }
            break;
          }
        case FieldsAndGetters:
          {
            if (m instanceof MethodDecl) {
              MethodDecl md = ((MethodDecl) m);
              if (!md.getter) {
                return null;
              }
            }
            break;
          }
        case FieldsAndSetters:
          {
            if (m instanceof MethodDecl) {
              MethodDecl md = ((MethodDecl) m);
              if (!md.setter) {
                return null;
              }
            }
            break;
          }
        case MethodsWithoutGettersAndSetters:
          {
            if (m instanceof FieldDecl) {
              return null;
            }
            if (m instanceof MethodDecl) {
              MethodDecl md = ((MethodDecl) m);
              if (md.setter || md.getter) {
                return null;
              }
            }
            break;
          }
        default:
          {
          }
      }
    }
    if (m != null) {
      return m;
    }
    if (c.extendType != null) {
      ClassDecl parent = ((ClassDecl) this.context.get(c.extendType.name));
      if (!(Objects.equals(parent, c))) {
        m = getMember(parent, name, filter, true, null);
      }
      if (m != null) {
        return m;
      }
    }
    for (DataType impl : c.impls) {
      ClassDecl parent = ((ClassDecl) this.context.get(impl.name));
      if (!(Objects.equals(parent, c))) {
        m = getMember(parent, name, filter, true, null);
      }
      if (m != null) {
        return m;
      }
    }
    if (c.isMixin || (c.isExtension && !noSuperCons)) {
      for (DataType impl : c.ons) {
        TopDecl top = this.context.get(impl.name);
        if (top instanceof ClassDecl) {
          ClassDecl parent = ((ClassDecl) top);
          if (!(Objects.equals(parent, c))) {
            m = getMember(parent, name, filter, true, null);
          }
        }
        if (m != null) {
          return m;
        }
      }
    }
    for (DataType impl : c.mixins) {
      ClassDecl parent = ((ClassDecl) this.context.get(impl.name));
      if (!(Objects.equals(parent, c))) {
        m = getMember(parent, name, filter, true, null);
      }
      if (m != null) {
        return m;
      }
    }
    for (DataType impl : c.extensions) {
      ClassDecl parent = ((ClassDecl) this.context.get(impl.name));
      if (!(Objects.equals(parent, c))) {
        m = getMember(parent, name, filter, true, null);
      }
      if (m != null) {
        return m;
      }
    }
    return m;
  }

  public void genFieldOrEnumExpression(FieldOrEnumExpression exp, long depth, Xp xp) {
    if (exp.resolvedType instanceof FunctionType && exp.on != null) {
      FunctionType type = ((FunctionType) exp.resolvedType);
      if (type.signature == null) {
        type.signature = type.computeSignature();
      }
      xp.apply("(");
      xp.apply(type.signature);
      xp.apply(")");
      xp.apply("{");
    }
    if (exp.on != null) {
      genExp(exp.on, depth, (x) -> {});
      if (exp.resolvedType instanceof FunctionType) {
        genExp(exp.on, depth, xp);
        xp.apply(", ");
      }
      genExp(exp.on, depth, xp);
      if (exp.on instanceof FieldOrEnumExpression
          && (ParserUtil.isTypeName((((FieldOrEnumExpression) exp.on)).name))) {
        xp.apply("->");
      } else {
        xp.apply("->");
      }
    } else {
      DataType type = this.scope.get(exp.name);
      if (type == null && this.instanceClass != null) {
        ClassMember cm = getMember(this.instanceClass, exp.name, null, false, null);
        if (cm != null) {
          if (cm.staticValue) {
            xp.apply("___");
            xp.apply(this.instanceClass.name);
            xp.apply("->");
          } else {
            xp.apply("this->");
          }
        }
      }
    }
    boolean shouldCast = this.scope.casts.containsKey(exp.name);
    ClassDecl castType = null;
    if (shouldCast) {
      castType = computeCastType(exp);
      if (castType == null) {
        shouldCast = false;
      }
    }
    if (exp.resolvedMember instanceof MethodDecl
        && !exp.resolvedMember.staticValue
        && exp.resolvedMember.lib == null) {
      xp.apply("__type->");
    }
    if (shouldCast) {
      xp.apply("((");
      xp.apply(castType.name);
      xp.apply("*)");
    }
    if (ParserUtil.isTypeName(exp.name) && !(Objects.equals(exp.name, exp.name.toUpperCase()))) {
      xp.apply("___");
      xp.apply(exp.name);
    } else {
      if (Objects.equals(exp.name, "super")) {
        xp.apply("this");
      } else {
        xp.apply(variable(exp.name));
      }
    }
    if (shouldCast) {
      xp.apply(")");
    }
    if (exp.isGetter) {
      xp.apply("(");
      if (!exp.resolvedMember.staticValue) {
        if (exp.on != null) {
          genExp(exp.on, depth, xp);
        } else if (exp.resolvedMember.lib == null) {
          xp.apply("this");
        }
      }
      xp.apply(")");
    }
    if (exp.resolvedType instanceof FunctionType && exp.on != null) {
      xp.apply("}");
    }
  }

  public DataType fieldTypeFromScope(String name) {
    Scope s = this.scope;
    while (s != null) {
      DataType type = s.variables.get(name);
      if (type != null) {
        return type;
      }
      s = s.parent;
    }
    if (this.instanceClass != null) {
      ClassMember cm = this.instanceClass.get(name);
      if (cm != null && cm instanceof FieldDecl) {
        return (((FieldDecl) cm)).type;
      }
    }
    return null;
  }

  public DataType resolveType(ClassDecl c, ClassDecl from, DataType r) {
    if (r == null) {
      return ofUnknownType();
    }
    if (c.generics != null && from != null) {
      Map<String, DataType> forFrom = c.resolvedGenerics.get(from.name);
      if (forFrom == null) {
        return r;
      }
      DataType res = forFrom.get(r.name);
      if (res != null) {
        return res;
      }
      D3ELogger.error("Unknown type in resolveType");
    }
    return r;
  }

  public boolean isGetter(ClassDecl type, String name) {
    ClassMember cm = type.get(name);
    return cm instanceof MethodDecl && (((MethodDecl) cm)).getter;
  }

  public void genFnCallExpression(FnCallExpression exp, long depth, Xp xp) {
    genExp(exp.on, depth, xp);
    genMethodCall(exp.call, depth, xp);
  }

  public void genForEachLoop(ForEachLoop exp, long depth, Xp xp) {
    xp.apply("for (");
    if (exp.dataType != null) {
      xp.apply(dataTypeToString(exp.dataType, null, false));
    } else {
      xp.apply("auto");
    }
    xp.apply(" ");
    xp.apply(variable(exp.name));
    xp.apply(" : ");
    genExp(exp.collection, depth, xp);
    xp.apply(") ");
    genAsBlock(exp.body, depth, xp, null);
    xp.apply("\n");
  }

  public void genForLoop(ForLoop exp, long depth, Xp xp) {
    xp.apply("for (");
    exp.inits.forEach(
        (i) -> {
          genExp(i, depth, xp);
          if (!(Objects.equals(i, ListExt.last(exp.inits)))) {
            xp.apply(", ");
          }
        });
    xp.apply("; ");
    genExp(exp.test, depth, xp);
    xp.apply("; ");
    exp.resets.forEach(
        (i) -> {
          genExp(i, depth, xp);
          if (!(Objects.equals(i, ListExt.last(exp.resets)))) {
            xp.apply(", ");
          }
        });
    xp.apply(") ");
    genAsBlock(exp.body, depth, xp, null);
    xp.apply("\n");
  }

  public void genAsBlock(Expression exp, long depth, Xp xp, Scope localScope) {
    if (exp instanceof Block) {
      genBlock(((Block) exp), depth, xp, localScope);
    } else {
      if (localScope == null) {
        localScope = new Scope(this.scope, null);
      }
      this.scope = localScope;
      xp.apply("{\n");
      xp.apply(tab(depth + 1l));
      genExp(exp, depth, xp);
      xp.apply(";\n");
      xp.apply(tab(depth));
      xp.apply("}");
      this.scope = this.scope.parent;
    }
  }

  public void genIfStatement(IfStatement exp, long depth, Xp xp) {
    xp.apply("if (");
    genExp(exp.test, depth, xp);
    xp.apply(") ");
    Scope scope = new Scope(this.scope, null);
    if (exp.test instanceof TypeCastOrCheckExpression) {
      TypeCastOrCheckExpression check = ((TypeCastOrCheckExpression) exp.test);
      if (check.exp instanceof FieldOrEnumExpression) {
        FieldOrEnumExpression fe = ((FieldOrEnumExpression) check.exp);
        if (fe.on == null) {
          MapExt.set(scope.casts, fe.name, check.dataType.name);
          scope.add(fe.name, check.dataType);
        }
      }
    }
    genAsBlock(exp.thenStatement, depth, xp, scope);
    if (exp.elseStatement != null) {
      xp.apply(" else ");
      genAsBlock(exp.elseStatement, depth, xp, null);
      if (exp.elseStatement instanceof IfStatement) {
      } else {
        xp.apply("\n");
      }
    } else {
      xp.apply("\n");
    }
  }

  public void genInlineMethodStatement(InlineMethodStatement exp, long depth, Xp xp) {
    xp.apply("InlineMethod");
    exp.resolvedType = ofUnknownType();
  }

  public void genLabelStatement(LabelStatement exp, long depth, Xp xp) {
    xp.apply(exp.name);
    xp.apply(":");
    exp.resolvedType = ofUnknownType();
  }

  public void genLambdaExpression(LambdaExpression exp, long depth, Xp xp) {
    xp.apply("[=] (");
    /*
     TODO make sure names and types exist for each param
    */
    exp.params.forEach(
        (p) -> {
          if (p.type == null) {
            xp.apply("Object* ");
          } else {
            xp.apply(dataTypeToString(p.type, null, false));
          }
          xp.apply(" ");
          xp.apply(p.name);
          if (!(Objects.equals(p, ListExt.last(exp.params)))) {
            xp.apply(",");
          }
        });
    xp.apply(") ");
    if (exp.expression != null) {
      genAsBlock(exp.expression, depth, xp, null);
    } else {
      genBlock(exp.body, depth, xp, null);
    }
  }

  public void genLiteralExpression(LiteralExpression exp, long depth, Xp xp) {
    switch (exp.type) {
      case TypeBoolean:
        {
          {
            xp.apply(exp.value);
          }
          break;
        }
      case TypeString:
        {
          {
            xp.apply("__s(\"");
            xp.apply(exp.value);
            xp.apply("\")");
          }
          break;
        }
      case TypeDouble:
        {
          {
            xp.apply(exp.value);
          }
          break;
        }
      case TypeInteger:
        {
          {
            xp.apply(exp.value);
          }
          break;
        }
      default:
        {
        }
    }
  }

  public void genMethodCall(MethodCall exp, long depth, Xp xp) {
    ClassDecl onType = null;
    boolean isSuper = false;
    if (exp.on != null) {
      if (exp.on instanceof FieldOrEnumExpression
          && Objects.equals((((FieldOrEnumExpression) exp.on)).name, "super")) {
        xp.apply("___");
        if (this.instanceClass.extendType != null) {
          xp.apply(this.instanceClass.extendType.name);
        } else {
          xp.apply("FromMixin");
        }
        xp.apply("->");
        isSuper = true;
      } else {
        genExp(exp.on, depth, xp);
        boolean isStaticCall = exp.resolvedMethod != null && exp.resolvedMethod.staticValue;
        if (isStaticCall) {
          xp.apply("->");
        } else {
          xp.apply("->__type->");
        }
      }
    } else {
      DataType fieldType = this.scope.get(exp.name);
      if (fieldType == null && this.instanceClass != null) {
        ClassMember cm = getMember(this.instanceClass, exp.name, null, false, null);
        if (cm != null) {
          if (cm.staticValue) {
            xp.apply("___");
            xp.apply(this.instanceClass.name);
            xp.apply("->");
          } else {
            xp.apply("this->__type->");
          }
        }
      }
    }
    boolean isConstructor = false;
    if (exp.name != null && StringExt.getIsNotEmpty(exp.name)) {
      if (exp.on == null && Objects.equals(exp.name, "super")) {
        xp.apply("___");
        if (this.instanceClass.extendType != null) {
          xp.apply(this.instanceClass.extendType.name);
        } else {
          xp.apply("FromMixin");
        }
        xp.apply("->new");
        isSuper = true;
      } else {
        String name = variable(exp.name);
        if (ParserUtil.isTypeName(name)) {
          xp.apply("new_");
          xp.apply(exp.name);
          isConstructor = true;
        } else {
          xp.apply(variable(exp.name));
        }
      }
    }
    xp.apply("(");
    boolean needComma = false;
    if (isConstructor) {
      xp.apply("___");
      xp.apply(exp.name);
      xp.apply("->__make()");
      needComma = true;
    } else if (isSuper) {
      xp.apply("(");
      if (this.instanceClass.extendType != null) {
        xp.apply(this.instanceClass.extendType.name);
      } else {
        xp.apply("FromMixin");
      }
      xp.apply("*) this");
      needComma = true;
    } else if (exp.resolvedMethod != null && !exp.resolvedMethod.staticValue) {
      if (exp.resolvedMethod.lib != null) {
        /*
         library function
        */
      } else {
        if (exp.on == null) {
          xp.apply("this");
        } else {
          genExp(exp.on, depth, xp);
        }
        needComma = true;
      }
    }
    if (needComma && ListExt.isNotEmpty(exp.positionArgs)) {
      xp.apply(", ");
    }
    for (Argument a : exp.positionArgs) {
      needComma = true;
      genExp(a.arg, depth, xp);
      if (!(Objects.equals(a, ListExt.last(exp.positionArgs)))) {
        xp.apply(", ");
      }
    }
    if (needComma && ListExt.isNotEmpty(exp.namedArgs)) {
      xp.apply(", ");
    }
    exp.namedArgs.forEach(
        (a) -> {
          genExp(a.value, depth, xp);
          if (!(Objects.equals(a, ListExt.last(exp.namedArgs)))) {
            xp.apply(", ");
          }
        });
    xp.apply(")");
  }

  public void genNullExpression(NullExpression exp, long depth, Xp xp) {
    xp.apply("nullptr");
    exp.resolvedType = this.nullType;
  }

  public void genParExpression(ParExpression exp, long depth, Xp xp) {
    xp.apply("(");
    genExp(exp.exp, depth, xp);
    xp.apply(")");
    exp.resolvedType = exp.exp.resolvedType;
  }

  public void genPostfixExpression(PostfixExpression exp, long depth, Xp xp) {
    genExp(exp.on, depth, xp);
    xp.apply(exp.postfix);
    exp.resolvedType = exp.on.resolvedType;
  }

  public void genPrefixExpression(PrefixExpression exp, long depth, Xp xp) {
    xp.apply(exp.prefix);
    genExp(exp.on, depth, xp);
    exp.resolvedType = exp.on.resolvedType;
  }

  public void genRethrowStatement(RethrowStatement exp, long depth, Xp xp) {
    xp.apply("__rethrow()");
  }

  public void genThrowStatement(ThrowStatement exp, long depth, Xp xp) {
    xp.apply("__throw(");
    genExp(exp.exp, depth, xp);
    xp.apply(")");
  }

  public void genReturn(Return exp, long depth, Xp xp) {
    xp.apply("return");
    if (exp.expression != null) {
      xp.apply(" ");
      genExp(exp.expression, depth, xp);
    }
  }

  public void genSwitchExpression(SwitchExpression exp, long depth, Xp xp) {
    xp.apply("SwitchExpNotSupported");
  }

  public void genTerinaryExpression(TerinaryExpression exp, long depth, Xp xp) {
    genExp(exp.condition, depth, xp);
    xp.apply("? ");
    genExp(exp.ifTrue, depth, xp);
    xp.apply(" : ");
    genExp(exp.ifFalse, depth, xp);
    exp.resolvedType = commonType(exp.ifTrue.resolvedType, exp.ifFalse.resolvedType);
  }

  public void genTypeCastOrCheckExpression(TypeCastOrCheckExpression exp, long depth, Xp xp) {
    if (exp.check) {
      if (exp.isNot) {
        xp.apply("!__is(");
      } else {
        xp.apply("__is(");
      }
      genExp(exp.exp, depth, xp);
      xp.apply(", __");
      xp.apply(exp.dataType.name);
      xp.apply("Type)");
      exp.resolvedType = this.booleanType;
    } else {
      xp.apply("(");
      xp.apply(dataTypeToString(exp.dataType, null, false));
      xp.apply(")");
      genExp(exp.exp, depth, xp);
      xp.apply("");
      exp.resolvedType = exp.dataType;
    }
  }

  public void genWhileLoop(WhileLoop exp, long depth, Xp xp) {
    xp.apply("while (");
    genExp(exp.test, depth, xp);
    xp.apply(") ");
    genAsBlock(exp.body, depth, xp, null);
    xp.apply("\n");
  }

  public void genYieldExpression(YieldExpression exp, long depth, Xp xp) {
    xp.apply("yield ");
    genExp(exp.exp, depth, xp);
  }

  public void genTryCatchStatement(TryCatcheStatment exp, long depth, Xp xp) {
    xp.apply("if(__try())");
    genExp(exp.body, depth, xp);
    exp.catchParts.forEach(
        (c) -> {
          if (c.onType != null) {
            xp.apply(tab(depth));
            xp.apply(" else if(__on_catch(___");
            xp.apply(c.onType.name);
            xp.apply("->id) {\n");
            xp.apply(tab(depth + 1l));
            xp.apply(c.onType.name);
            xp.apply("* ");
            xp.apply(c.exp);
            xp.apply("= (");
            xp.apply(c.onType.name);
            xp.apply("*) __get_catch();\n");
            xp.apply(tab(depth + 1l));
            genExp(c.body, depth, xp);
            xp.apply("\n");
            xp.apply(tab(depth));
            xp.apply("}");
          } else {
            xp.apply(" else if(__on_catch(___Exception->id) {\n");
            xp.apply(tab(depth + 1l));
            xp.apply("Exception* ");
            xp.apply(c.exp);
            xp.apply("= (Exception*) __get_catch();\n");
            xp.apply(tab(depth + 1l));
            genExp(c.body, depth + 1l, xp);
            xp.apply("\n");
            xp.apply(tab(depth));
            xp.apply("}");
          }
        });
    if (exp.finallyBody != null) {
      xp.apply(" else ");
      genExp(exp.finallyBody, depth, xp);
    }
  }

  public DataType getFieldType(ClassDecl c, String name) {
    ClassMember member = getMember(c, name, null, false, null);
    if (member instanceof FieldDecl) {
      return (((FieldDecl) member)).type;
    }
    return null;
  }

  public void genTypeDef(Typedef t) {
    hp("typedef struct ");
    hp(t.name);
    hl(" {");
    hl("    void* data;");
    hp("    ");
    functionTypeToString(
        t.fnType,
        "call",
        (x) -> {
          hp(x);
        });
    hl(";");
    hp("} ");
    hp(t.name);
    hl(";");
  }

  public void genFieldDecl(FieldDecl f, String prefix, boolean extern, long depth, Xp xp) {
    while (depth > 0l) {
      xp.apply("    ");
      depth--;
    }
    if (extern) {
      xp.apply("extern ");
    }
    /*
     if(f.final) {
         s += 'final ';
     }
     if(f.const) {
         hp('const ');
     }
    */
    boolean skipName = false;
    DataType type = f.type;
    if (type instanceof FunctionType) {
      skipName = true;
    }
    xp.apply(dataTypeToString(type, f.name, false));
    if (!skipName) {
      xp.apply(" ");
      if (prefix != null) {
        xp.apply(prefix);
        xp.apply("_");
      }
      xp.apply(f.name);
    }
    xp.apply(";\n");
  }

  public DataType getListValueType(ClassDecl cls) {
    MethodDecl method = ((MethodDecl) getMember(cls, "[]", MemberFilter.AllMethods, false, null));
    if (method != null) {
      return method.returnType;
    } else {
      error("No index operator found");
      return ofUnknownType();
    }
  }
}
