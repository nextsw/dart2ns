package classes;

import d3e.core.D3ELogger;
import d3e.core.IntegerExt;
import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Objects;

public class CppGen implements Gen {
  public Dart2NSContext context;
  public String base;
  public List<String> cppLines = ListExt.asList();
  public List<String> hppLines = ListExt.asList();
  public List<String> cpLines = ListExt.asList();
  public List<String> hpLines = ListExt.asList();
  public List<String> blockLines = ListExt.asList();
  public List<String> blockWords = ListExt.asList();
  public List<String> keywords = ListExt.asList("string", "int", "long", "bool", "auto", "char");
  public DataType objectType = new ValueType("Object", false);
  public DataType booleanType = new ValueType("bool", false);
  public DataType integerType = new ValueType("int", false);
  public DataType doubleType = new ValueType("double", false);
  public DataType nullType = new ValueType("null", false);
  public DataType stringType = new ValueType("String", false);
  public ClassDecl instanceClass = null;
  public Scope scope;
  public long tempCount = 0l;

  public CppGen() {}

  public String variable(String s) {
    if (this.keywords.contains(s)) {
      return s + "Value";
    } else {
      return s;
    }
  }

  public DataType ofUnknownType() {
    D3ELogger.error("Unknown type");
    return this.objectType;
  }

  public void gen(Dart2NSContext context, String base) {
    this.context = context;
    this.base = base;
    generate();
  }

  public void generate() {
    this.context.libs.forEach(
        (lib) -> {
          genLibrary(lib);
        });
  }

  public void genLibrary(Library lib) {
    String outPath = lib.packagePath;
    List<String> split = StringExt.split(outPath, "/");
    String nameOnly = ListExt.last(split);
    String upper = StringExt.replaceAll(outPath, "/", "_").toUpperCase();
    hpp("#ifndef " + upper);
    hpp("#define " + upper);
    cpp("#include \"" + nameOnly + ".hpp\"");
    hpp("#include <base.hpp>");
    lib.exports.forEach(
        (e) -> {
          String path = e.path;
          if (StringExt.startsWith(e.path, "package:", 0l)
              || StringExt.startsWith(e.path, "dart:", 0l)) {
            hpp("#include <" + e.lib.packagePath + ".hpp>");
          } else {
            hpp("#include \"" + path + ".hpp\"");
          }
        });
    hpp("");
    lib.imports.forEach(
        (i) -> {
          String path = i.path;
          if (StringExt.startsWith(i.path, "package:", 0l)
              || StringExt.startsWith(i.path, "dart:", 0l)) {
            hpp("#include <" + i.lib.packagePath + ".hpp>");
          } else {
            hpp("#include \"" + path + ".hpp\"");
          }
        });
    hpp("");
    lib.objects.forEach(
        (obj) -> {
          if (obj instanceof ClassDecl) {
            genClassDecl(((ClassDecl) obj));
          } else if (obj instanceof Enum) {
            genEnum(((Enum) obj));
          } else if (obj instanceof Typedef) {
            genTypeDef(((Typedef) obj));
          } else if (obj instanceof MethodDecl) {
            genMethodDecl(null, ((MethodDecl) obj), 0l);
          } else if (obj instanceof FieldDecl) {
            genFieldDecl(((FieldDecl) obj), 0l);
          } else {
            D3ELogger.error("Unknown object type");
          }
        });
    hpp("");
    if (ListExt.isNotEmpty(lib.parts)) {
      hpp("// Parts");
    }
    lib.parts.forEach(
        (i) -> {
          hpp("#include \"" + i.path + ".hpp\"");
        });
    hpp("");
    hpp("#endif");
    FileUtils.writeFile(this.base + lib.packagePath + ".hpp", ListExt.join(this.hppLines, "\n"));
    FileUtils.writeFile(this.base + lib.packagePath + ".cpp", ListExt.join(this.cppLines, "\n"));
    this.cppLines.clear();
    this.hppLines.clear();
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

  public String generics(TypeParams params) {
    if (params == null || params.params.isEmpty()) {
      return "";
    }
    return "template<"
        + IterableExt.join(
            ListExt.map(
                params.params,
                (p) -> {
                  return "typename " + typeParamToString(p);
                }),
            ", ")
        + ">\n";
  }

  public String typeArgsToString(List<DataType> args) {
    if (args.isEmpty()) {
      return "";
    }
    return "<"
        + IterableExt.join(
            ListExt.map(
                args,
                (p) -> {
                  return dataTypeToString(p, false, null);
                }),
            ", ")
        + ">";
  }

  public String typeParamToString(TypeParam p) {
    String res = p.name;
    /*
     if(p.extendType != null) {
         res += ' : ' + dataTypeToString(p.extendType);
     }
    */
    return res;
  }

  public String valueTypeToString(ValueType v, boolean cls) {
    String res = v.name;
    if (cls) {
      res += "Cls";
    }
    if (Objects.equals(res, "var")) {
      res = "auto";
    }
    if (ListExt.isNotEmpty(v.args)) {
      res += typeArgsToString(v.args);
    } else {
      TopDecl top = this.context.get(v.name);
      if (top != null && top instanceof ClassDecl) {
        ClassDecl cd = ((ClassDecl) top);
        if (cd.generics != null) {
          res += "<";
          for (TypeParam p : cd.generics.params) {
            res += "any";
            if (!(Objects.equals(p, ListExt.last(cd.generics.params)))) {
              res += ", ";
            }
          }
          res += ">";
        }
      }
    }
    return res;
  }

  public void functionTypeToString(FunctionType f, String name, Xp xp) {
    xp.apply("std::function<");
    if (f.returnType == null) {
      xp.apply("void ");
    } else {
      xp.apply(dataTypeToString(f.returnType, false, null));
    }
    xp.apply("(");
    if (f.params != null) {
      List<MethodParam> params = sortMethodParams(f.params);
      xp.apply(
          IterableExt.join(
              ListExt.map(
                  params,
                  (p) -> {
                    return paramToString(p);
                  }),
              ", "));
    }
    xp.apply(")>");
  }

  public String defTypeToString(DefType d) {
    return "DefType";
  }

  public String dataTypeToString(DataType d, boolean cls, String name) {
    if (d instanceof ValueType) {
      return valueTypeToString(((ValueType) d), cls);
    } else if (d instanceof FunctionType) {
      List<String> res = ListExt.asList();
      functionTypeToString(
          ((FunctionType) d),
          name,
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
    hpp("");
    if (c.generics != null) {
      hp(generics(c.generics));
    }
    hp("class ");
    hp(c.name);
    hp("Cls");
    hp(" : public ");
    if (c.extendType != null) {
      hp(dataTypeToString(c.extendType, true, null));
    } else {
      hp("ObjectCls");
    }
    hp(" {");
    hpp("public:");
    declareFields(c, true);
    hpp("");
    declareMethods(c, true);
    hpp("private:");
    declareFields(c, false);
    hpp("");
    declareMethods(c, false);
    ListExt.where(
            c.members,
            (o) -> {
              return o instanceof MethodDecl;
            })
        .forEach(
            (o) -> {
              MethodDecl m = ((MethodDecl) o);
            });
    hpp("};");
    hp(generics(c.generics));
    hp("using ");
    hp(c.name);
    hp(" = std::shared_ptr<");
    hp(c.name);
    hp("Cls");
    if (c.generics != null && ListExt.isNotEmpty(c.generics.params)) {
      hp("<");
      hp(
          IterableExt.join(
              ListExt.map(
                  c.generics.params,
                  (p) -> {
                    return typeParamToString(p);
                  }),
              ", "));
      hp(">");
    }
    hl(">;");
  }

  public void declareFields(ClassDecl c, boolean publicValue) {
    ListExt.where(
            c.members,
            (m) -> {
              return publicValue != StringExt.startsWith(m.name, "_", 0l)
                  && (m instanceof FieldDecl);
            })
        .forEach(
            (m) -> {
              genFieldDecl(((FieldDecl) m), 1l);
            });
  }

  public void declareMethods(ClassDecl c, boolean publicValue) {
    ListExt.where(
            c.members,
            (x) -> {
              return publicValue != StringExt.startsWith(x.name, "_", 0l)
                  && (x instanceof MethodDecl);
            })
        .forEach(
            (i) -> {
              MethodDecl m = ((MethodDecl) i);
              genMethodDecl(c, m, 1l);
            });
  }

  public String paramToString(MethodParam p) {
    String out = "";
    if (p.dataType != null) {
      out += dataTypeToString(p.dataType, false, variable(p.name));
      if (this.scope != null) {
        this.scope.add(p.name, p.dataType);
      }
    } else {
      out += "Unknown";
      if (this.scope != null) {
        this.scope.add(p.name, ofUnknownType());
      }
    }
    String name = p.name;
    out += " ";
    out += variable(name);
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
    hpp("");
    hpp("enum " + e.name + "{");
    e.values.forEach(
        (v) -> {
          hpp("    " + variable(v) + ",");
        });
    hpp("} // end " + e.name);
  }

  public void genMethodDecl(ClassDecl c, MethodDecl m, long depth) {
    while (depth > 0l) {
      hp("    ");
      depth--;
    }
    if (m.generics != null) {
      hp(generics(m.generics));
      hp(" ");
    }
    /*
     if(m.external) {
         hp('extern ');
     }
    */
    if (m.staticValue) {
      hp("static ");
    }
    /*
     if(f.final) {
         s += 'final ';
     }
     if(m.const) {
         hp('const');
     }
    */
    boolean isConstructor = c != null && Objects.equals(c.name, m.name);
    if (!isConstructor && !m.staticValue && !m.external && c != null) {
      hp("virtual ");
    }
    if (m.returnType == null) {
      if (c != null && Objects.equals(c.name, m.name)) {
      } else {
        hp("void ");
      }
    } else {
      hp(dataTypeToString(m.returnType, false, null));
    }
    hp(" ");
    if (!ParserUtil.isNameChar(m.name)) {
      hp("operator");
    }
    hp(variable(m.name));
    if (isConstructor) {
      hp("Cls");
    }
    hp("(");
    if (m.params != null) {
      genMethodParams(
          m.params,
          c,
          (x) -> {
            hp(x);
          });
    }
    hp(")");
    if (m.body == null && m.init == null && m.exp == null) {
      if (isConstructor || c == null) {
        hl(";");
      } else {
        if (isOverriding(c, m.name)) {
          hl(" override;");
        } else {
          hl(";");
        }
      }
      return;
    }
    hl(";");
    hpp("");
    if (!m.staticValue) {
      this.instanceClass = c;
    } else {
      this.instanceClass = null;
    }
    if (c != null && c.generics != null) {
      cp(generics(c.generics));
    }
    if (m.generics != null) {
      cp(generics(m.generics));
    }
    if (m.returnType == null) {
      if (c != null && Objects.equals(c.name, m.name)) {
      } else {
        cp("void");
        cp(" ");
      }
    } else {
      cp(dataTypeToString(m.returnType, false, null));
      cp(" ");
    }
    if (c == null) {
      cp(m.name);
    } else {
      cp(c.name);
      cp("Cls");
      if (c.generics != null) {
        cp("<");
        cp(
            IterableExt.join(
                ListExt.map(
                    c.generics.params,
                    (p) -> {
                      return p.name;
                    }),
                ", "));
        cp(">");
      }
      cp("::");
      cp(m.name);
      if (isConstructor) {
        cp("Cls");
      }
    }
    cp("(");
    this.scope = new Scope(null, c);
    if (m.params != null) {
      genMethodParams(
          m.params,
          c,
          (x) -> {
            cp(x);
          });
    }
    cp(")");
    if (isConstructor) {
      if (m.init != null) {
        MethodCall superCall = removeSuperCall(m.init);
        if (superCall != null) {
          cp(" : ");
          cp(dataTypeToString(c.extendType, false, null));
          cp("(");
          superCall.positionArgs.forEach(
              (a) -> {
                genExp(
                    a.arg,
                    0l,
                    (x) -> {
                      cp(x);
                    });
                if (!(Objects.equals(a, ListExt.last(superCall.positionArgs)))) {
                  cp(", ");
                }
              });
          superCall.namedArgs.forEach(
              (a) -> {
                genExp(
                    a.value,
                    0l,
                    (x) -> {
                      cp(x);
                    });
                if (!(Objects.equals(a, ListExt.last(superCall.namedArgs)))) {
                  cp(", ");
                }
              });
          cp(")");
        }
        cl(" {");
        if (ListExt.isNotEmpty((((Block) m.init)).statements)) {
          cp(tab(1l));
          genExp(
              m.init,
              1l,
              (x) -> {
                cp(x);
              });
          cl("");
        }
      } else {
        cl(" {");
      }
      if (m.body != null) {
        cp(tab(1l));
        genBlock(
            m.body,
            1l,
            (x) -> {
              cp(x);
            },
            null);
        cl("");
      }
      cl("}");
    } else if (m.body != null) {
      cp(" ");
      genBlock(
          m.body,
          0l,
          (x) -> {
            cp(x);
          },
          null);
    } else if (m.exp != null) {
      cl(" {");
      cp("    return ");
      genExp(
          m.exp,
          1l,
          (x) -> {
            cp(x);
          });
      cl(";");
      cl("}");
    }
    cpp("");
    this.tempCount = 0l;
  }

  public void genMethodParams(MethodParams mp, ClassDecl c, Xp xp) {
    List<MethodParam> thisParams = ListExt.asList();
    List<MethodParam> params = sortMethodParams(mp);
    params.forEach(
        (p) -> {
          if (p.dataType == null) {
            if (p.thisToken != null && c != null) {
              thisParams.add(p);
              DataType type = getFieldType(c, p.name);
              xp.apply(dataTypeToString(type, false, null));
              if (this.scope != null) {
                this.scope.add(p.name, type);
              }
            } else {
              xp.apply("Unknown");
              if (this.scope != null) {
                this.scope.add(p.name, ofUnknownType());
              }
            }
            xp.apply(" ");
            xp.apply(p.name);
          } else {
            xp.apply(paramToString(p));
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
    obj =
        ListExt.firstWhere(
            parent.members,
            (m) -> {
              return Objects.equals(m.name, name);
            },
            null);
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
    } else if (exp instanceof DynamicTypeExpression) {
      genDynamicTypeExpression(((DynamicTypeExpression) exp), depth, xp);
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

  public void genArrayAccess(ArrayAccess exp, long depth, Xp xp) {
    genExp(exp.on, depth, xp);
    if (exp.checkNull) {
      xp.apply("?");
    } else if (exp.notNull) {
      xp.apply("!");
    }
    xp.apply("[");
    genExp(exp.index, depth, xp);
    xp.apply("]");
    DataType value$ = ListExt.first((((ValueType) exp.on.resolvedType)).args);
    if (value$ == null) {
      value$ = ofUnknownType();
    }
    exp.resolvedType = value$;
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
                bp("List<");
                bp(dataTypeToString(exp.enforceType, false, null));
                bp("> list");
                bp(tempVal);
                bp(" = make<ListCls<");
                bl(">>();");
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
                      bp("    list");
                      bp(tempVal);
                      bp(".add(");
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
                        bp("    list");
                        bp(tempVal);
                        bp(".add(");
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
                      bp("    list");
                      bp(tempVal);
                      bp(".add(");
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
                      bp("    list");
                      bp(tempVal);
                      bp(".add(_x");
                      bp(tempVal);
                      bp(");\n");
                      bp("}");
                    } else {
                      bp("list");
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
              xp.apply("list");
              xp.apply(tempVal);
            }
            break;
          }
        case Map:
          {
            {
              if (exp.enforceType != null) {
                bp("Map<");
                bp(dataTypeToString(exp.enforceType, false, null));
                bp(", ");
                bp(dataTypeToString(exp.valueType, false, null));
                bp("> map");
                bp(tempVal);
                bp(" = make<MapCls<");
                bl(">>();");
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
                      bp("    map");
                      bp(tempVal);
                      bp(".set(");
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
                        bp("    map");
                        bp(tempVal);
                        bp(".set(");
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
                      bp("    map");
                      bp(tempVal);
                      bp(".set(");
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
                      bp("map");
                      bp(tempVal);
                      bp(".addAll(");
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
                      bp("map");
                      bp(tempVal);
                      bp(".set(");
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
              xp.apply("list");
              xp.apply(tempVal);
            }
            break;
          }
        case Set:
          {
            {
              if (exp.enforceType != null) {
                bp("Set<");
                bp(dataTypeToString(exp.enforceType, false, null));
                bp("> set");
                bp(tempVal);
                bp(" = make<SetCls<");
                bl(">>();");
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
                      bp("    set");
                      bp(tempVal);
                      bp(".add(");
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
                        bp("    set");
                        bp(tempVal);
                        bp(".add(");
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
                      bp("    set");
                      bp(tempVal);
                      bp(".add(");
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
                      bp("    set");
                      bp(tempVal);
                      bp(".add(_x");
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
              xp.apply("list");
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
            xp.apply("makeList(");
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
            xp.apply("makeMap(");
            xp.apply("makeList(");
            exp.values.forEach(
                (v) -> {
                  MapItem i = ((MapItem) v);
                  genExp(i.key, depth, xp);
                  if (!(Objects.equals(v, ListExt.last(exp.values)))) {
                    xp.apply(", ");
                  }
                });
            xp.apply("), makeList(");
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
            xp.apply("makeSet(");
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
    if (exp.enforceType != null) {
      exp.resolvedType = exp.enforceType;
    } else {
      exp.resolvedType = ofUnknownType();
      D3ELogger.info("Need to check Arrray Objects common type");
    }
  }

  public void genArrayItem(ArrayItem exp, long depth, Xp xp) {
    xp.apply("ArrayItem");
  }

  public void genAssignment(Assignment exp, long depth, Xp xp) {
    genExp(exp.left, depth, xp);
    xp.apply(" ");
    if (Objects.equals(exp.op, "??=")) {
      xp.apply("|=");
    } else {
      xp.apply(exp.op);
    }
    xp.apply(" ");
    genExp(exp.right, depth, xp);
    exp.resolvedType = exp.left.resolvedType;
  }

  public void genAwaitExpression(AwaitExpression exp, long depth, Xp xp) {
    xp.apply("await ");
    genExp(exp.exp, depth, xp);
    exp.resolvedType = subType(exp.exp.resolvedType, 0l);
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

  public void genBinaryExpression(BinaryExpression exp, long depth, Xp xp) {
    genExp(exp.left, depth, xp);
    xp.apply(" ");
    if (Objects.equals(exp.op, "??")) {
      xp.apply("|");
    } else {
      xp.apply(exp.op);
    }
    xp.apply(" ");
    genExp(exp.right, depth, xp);
    if (exp.left != null && exp.right != null) {
      exp.resolvedType =
          exp.op == "??"
              ? commonType(exp.left.resolvedType, exp.right.resolvedType)
              : exp.left.resolvedType;
    } else {
      exp.resolvedType = ofUnknownType();
      D3ELogger.error("Invalid binary exp");
    }
  }

  public DataType commonType(DataType left, DataType right) {
    /*
     TODO need to implement actual checking
    */
    return left;
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
              || (s instanceof IfStatement)) {
            /*
             No need for semicolon
            */
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
    exp.resolvedType = exp.on.resolvedType;
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
      xp.apply(dataTypeToString(exp.type, false, null));
    } else {
      xp.apply("Unknown");
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
    if (exp.type == null || Objects.equals(exp.type.name, "var")) {
      DataType value$ = resolvedType;
      if (value$ == null) {
        value$ = ofUnknownType();
      }
      exp.resolvedType = value$;
    } else {
      exp.resolvedType = exp.type;
    }
  }

  public void genDoWhileLoop(DoWhileLoop exp, long depth, Xp xp) {
    xp.apply("do ");
    genBlock(exp.body, depth, xp, null);
    xp.apply(" while (");
    genExp(exp.test, depth, xp);
    xp.apply(")");
  }

  public void genDynamicTypeExpression(DynamicTypeExpression exp, long depth, Xp xp) {
    xp.apply("DynamicTypeExpression");
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

  public ClassMember getMember(ClassDecl c, String name) {
    if (c == null) {
      return null;
    }
    ClassMember cm =
        ListExt.firstWhere(
            c.members,
            (m) -> {
              return Objects.equals(m.name, name);
            },
            null);
    if (cm != null) {
      return cm;
    }
    if (c.extendType != null) {
      ClassDecl parent = ((ClassDecl) this.context.get(c.extendType.name));
      if (!(Objects.equals(parent, c))) {
        return getMember(parent, name);
      }
    }
    for (DataType impl : c.impls) {
      ClassDecl parent = ((ClassDecl) this.context.get(impl.name));
      if (!(Objects.equals(parent, c))) {
        return getMember(parent, name);
      }
    }
    for (DataType impl : c.mixins) {
      ClassDecl parent = ((ClassDecl) this.context.get(impl.name));
      if (!(Objects.equals(parent, c))) {
        return getMember(parent, name);
      }
    }
    return null;
  }

  public void genFieldOrEnumExpression(FieldOrEnumExpression exp, long depth, Xp xp) {
    ClassDecl onType = null;
    if (exp.on != null) {
      genExp(exp.on, depth, (x) -> {});
      TopDecl decl = this.context.get(exp.on.resolvedType.name);
      if (decl instanceof ClassDecl) {
        onType = ((ClassDecl) decl);
      } else {
        /*
         D3ELogger.error('Resolved Type is not Class in FEExp');
        */
      }
      genExp(exp.on, depth, xp);
      if (exp.checkNull) {
        xp.apply("?->");
      } else if (exp.notNull) {
        xp.apply("!->");
      } else {
        if (exp.on instanceof FieldOrEnumExpression
            && (ParserUtil.isTypeName((((FieldOrEnumExpression) exp.on)).name))) {
          xp.apply("::");
        } else {
          xp.apply("->");
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
    if (shouldCast) {
      xp.apply("as<");
      xp.apply(castType.name);
      xp.apply("Cls>(");
    }
    if (ParserUtil.isTypeName(exp.name) && !(Objects.equals(exp.name, exp.name.toUpperCase()))) {
      xp.apply(exp.name);
      xp.apply("Cls");
    } else {
      xp.apply(variable(exp.name));
    }
    if (shouldCast) {
      xp.apply(")");
    }
    if (onType == null) {
      onType = this.instanceClass;
    }
    DataType fieldType = fieldTypeFromScope(exp.name);
    if (fieldType == null && onType != null) {
      ClassMember cm = getMember(onType, exp.name);
      if (cm instanceof MethodDecl) {
        MethodDecl md = ((MethodDecl) cm);
        if (md.getter) {
          xp.apply("()");
          exp.resolvedType = resolveType(onType, md.returnType);
        } else {
          exp.resolvedType = ofUnknownType();
        }
      } else {
        FieldDecl field = ((FieldDecl) cm);
        if (field != null) {
          exp.resolvedType = resolveType(onType, field.type);
        } else {
          if (Objects.equals(exp.name, "this")) {
            exp.resolvedType = new ValueType(this.instanceClass.name, false);
          } else {
            D3ELogger.error("No field found: " + exp.name + " in " + onType.name);
            exp.resolvedType = ofUnknownType();
          }
        }
      }
    } else if (fieldType != null) {
      /*
       this must be global field
      */
      exp.resolvedType = fieldType;
    } else {
      D3ELogger.error("No field found: " + exp.name);
      exp.resolvedType = ofUnknownType();
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
      ClassMember cm =
          ListExt.firstWhere(
              this.instanceClass.members,
              (m) -> {
                return m instanceof FieldDecl && Objects.equals(m.name, name);
              },
              null);
      if (cm != null) {
        return (((FieldDecl) cm)).type;
      }
    }
    return null;
  }

  public DataType resolveType(ClassDecl c, DataType r) {
    if (r == null) {
      return ofUnknownType();
    }
    if (c.generics != null) {
      TypeParam genType =
          ListExt.firstWhere(
              c.generics.params,
              (p) -> {
                return Objects.equals(p.name, r.name);
              },
              null);
      if (genType != null) {
        if (genType.resolvedType != null) {
          return genType.resolvedType;
        } else if (genType.extendType != null) {
          return genType.extendType;
        }
      }
      D3ELogger.error("Unknown type in resolveType");
    }
    return r;
  }

  public boolean isGetter(ClassDecl type, String name) {
    return ListExt.any(
        type.members,
        (m) -> {
          return Objects.equals(m.name, name)
              && m instanceof MethodDecl
              && (((MethodDecl) m)).getter;
        });
  }

  public void genFnCallExpression(FnCallExpression exp, long depth, Xp xp) {
    genExp(exp.on, depth, xp);
    genMethodCall(exp.call, depth, xp);
    DataType onType = exp.on.resolvedType;
    if (onType instanceof FunctionType) {
      FunctionType ft = ((FunctionType) onType);
      exp.resolvedType = ft.returnType;
    } else {
      D3ELogger.error("We should not be calling non function types");
      exp.resolvedType = ofUnknownType();
    }
  }

  public void genForEachLoop(ForEachLoop exp, long depth, Xp xp) {
    xp.apply("for (");
    if (exp.dataType != null) {
      xp.apply(dataTypeToString(exp.dataType, false, null));
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
            xp.apply("Unknown ");
          } else {
            xp.apply(dataTypeToString(p.type, false, null));
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
    /*
    TODO we need to get the expected Type here
    */
    exp.resolvedType = ofUnknownType();
  }

  public void genLiteralExpression(LiteralExpression exp, long depth, Xp xp) {
    switch (exp.type) {
      case TypeBoolean:
        {
          {
            xp.apply(exp.value);
            exp.resolvedType = this.booleanType;
          }
          break;
        }
      case TypeString:
        {
          {
            xp.apply("__s(\"");
            xp.apply(exp.value);
            xp.apply("\")");
            exp.resolvedType = this.stringType;
          }
          break;
        }
      case TypeDouble:
        {
          {
            xp.apply(exp.value);
            exp.resolvedType = this.doubleType;
          }
          break;
        }
      case TypeInteger:
        {
          {
            xp.apply(exp.value);
            exp.resolvedType = this.integerType;
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
    if (exp.on != null) {
      genExp(exp.on, depth, xp);
      if (exp.checkNull) {
        xp.apply("?->");
      } else if (exp.notNull) {
        xp.apply("!->");
      } else {
        xp.apply("->");
      }
      if (exp.on.resolvedType instanceof ValueType) {
        TopDecl top = this.context.get(exp.on.resolvedType.name);
        if (top instanceof ClassDecl) {
          onType = ((ClassDecl) top);
        }
      }
    } else {
      if (this.instanceClass != null && getMember(this.instanceClass, exp.name) != null) {
        onType = this.instanceClass;
      }
    }
    if (ListExt.isNotEmpty(exp.typeArgs)) {
      xp.apply(typeArgsToString(exp.typeArgs));
    }
    if (exp.name != null && StringExt.getIsNotEmpty(exp.name)) {
      String name = variable(exp.name);
      if (ParserUtil.isTypeName(name)) {
        xp.apply("make<");
        xp.apply(exp.name);
        xp.apply("Cls>");
      } else {
        xp.apply(variable(exp.name));
      }
    }
    xp.apply("(");
    exp.positionArgs.forEach(
        (a) -> {
          genExp(a.arg, depth, xp);
          if (!(Objects.equals(a, ListExt.last(exp.positionArgs)))) {
            xp.apply(", ");
          }
        });
    if (ListExt.isNotEmpty(exp.positionArgs) && ListExt.isNotEmpty(exp.namedArgs)) {
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
    if (onType != null) {
      ClassMember cm =
          ListExt.firstWhere(
              onType.members,
              (m) -> {
                return Objects.equals(m.name, exp.name);
              },
              null);
      if (cm != null && cm instanceof MethodDecl) {
        MethodDecl md = ((MethodDecl) cm);
        exp.resolvedType = resolveType(onType, md.returnType);
      } else {
        /*
         Error
        */
        exp.resolvedType = ofUnknownType();
      }
    } else {
      TopDecl td = this.context.get(exp.name);
      if (td instanceof MethodDecl) {
        MethodDecl md = ((MethodDecl) td);
        exp.resolvedType = md.returnType;
        /*
        TODO need to resolve on method type generics
        */
      } else {
        exp.resolvedType = ofUnknownType();
      }
    }
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
    xp.apply("throw");
    exp.resolvedType = ofUnknownType();
  }

  public void genThrowStatement(ThrowStatement exp, long depth, Xp xp) {
    xp.apply("throw ");
    genExp(exp.exp, depth, xp);
    exp.resolvedType = ofUnknownType();
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
        xp.apply("!is<");
      } else {
        xp.apply("is<");
      }
      xp.apply(dataTypeToString(exp.dataType, false, null));
      xp.apply(">(");
      genExp(exp.exp, depth, xp);
      xp.apply(")");
      exp.resolvedType = this.booleanType;
    } else {
      xp.apply("as<");
      xp.apply(dataTypeToString(exp.dataType, false, null));
      xp.apply(">(");
      genExp(exp.exp, depth, xp);
      xp.apply(")");
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
    xp.apply("try ");
    genExp(exp.body, depth, xp);
    exp.catchParts.forEach(
        (c) -> {
          xp.apply(" catch (");
          xp.apply(dataTypeToString(c.onType, false, null));
          xp.apply(" ");
          xp.apply(c.exp);
          xp.apply(") ");
          genExp(c.body, depth, xp);
        });
    if (exp.finallyBody != null) {
      xp.apply(" finally ");
      genExp(exp.finallyBody, depth, xp);
    }
  }

  public DataType getFieldType(ClassDecl c, String name) {
    ClassMember member = getMember(c, name);
    if (member instanceof FieldDecl) {
      return (((FieldDecl) member)).type;
    }
    return null;
  }

  public void genTypeDef(Typedef t) {}

  public void genFieldDecl(FieldDecl f, long depth) {
    while (depth > 0l) {
      hp("    ");
      depth--;
    }
    if (f.staticValue) {
      hp("static ");
    }
    /*
     if(f.final) {
         s += 'final ';
     }
     if(f.const) {
         hp('const ');
     }
    */
    if (f.type == null) {
      hp("auto ");
    } else {
      hp(dataTypeToString(f.type, false, null));
    }
    hp(" ");
    hp(f.name);
    hp(";");
    hpp("");
  }
}
