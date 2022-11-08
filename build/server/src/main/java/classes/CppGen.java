package classes;

import d3e.core.D3ELogger;
import d3e.core.IterableExt;
import d3e.core.ListExt;
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

  public CppGen() {}

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
    hpp("#include <memory>");
    lib.exports.forEach(
        (e) -> {
          String path = e.path;
          if (StringExt.startsWith(e.path, "packages:", 0l)
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
          if (StringExt.startsWith(i.path, "packages:", 0l)
              || StringExt.startsWith(i.path, "dart:", 0l)) {
            hpp("#include <" + i.lib.packagePath + ".hpp>");
          } else {
            hpp("#include \"" + path + ".hpp\"");
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

  public void hn(String word) {
    this.hpLines.add(word);
    this.hppLines.add(ListExt.join(this.hpLines, ""));
    this.hpLines.clear();
  }

  public String generics(TypeParams params) {
    if (params == null || params.params.isEmpty()) {
      return "";
    }
    return "<"
        + IterableExt.join(
            ListExt.map(
                params.params,
                (p) -> {
                  return typeParamToString(p);
                }),
            ", ")
        + ">";
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
                  return dataTypeToString(p);
                }),
            ", ")
        + ">";
  }

  public String typeParamToString(TypeParam p) {
    String res = p.name;
    if (p.extendType != null) {
      res += " extends " + dataTypeToString(p.extendType);
    }
    return res;
  }

  public String valueTypeToString(ValueType v) {
    String res = v.name;
    if (Objects.equals(res, "var")) {
      res = "auto";
    }
    if (ListExt.isNotEmpty(v.args)) {
      res += typeArgsToString(v.args);
    }
    return res;
  }

  public String functionTypeToString(FunctionType f) {
    return "FunctionType";
  }

  public String defTypeToString(DefType d) {
    return "DefType";
  }

  public String dataTypeToString(DataType d) {
    if (d instanceof ValueType) {
      return valueTypeToString(((ValueType) d));
    } else if (d instanceof FunctionType) {
      return functionTypeToString(((FunctionType) d));
    } else if (d instanceof DefType) {
      return defTypeToString(((DefType) d));
    }
    return "Unknown";
  }

  public void genClassDecl(ClassDecl c) {
    hpp("");
    if (c.isAbstract) {}
    hp("class ");
    hp(c.name);
    hp(generics(c.generics));
    if (c.extendType != null) {
      hp(" : ");
      hp(dataTypeToString(c.extendType));
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
      out += dataTypeToString(p.dataType);
    } else {
      out += "Unknown";
    }
    out += " " + p.name;
    return out;
  }

  public List<MethodParam> sortMethodParams(MethodParams params) {
    List<MethodParam> out = ListExt.asList();
    ListExt.addAll(out, params.positionalParams);
    ListExt.addAll(out, params.optionalParams);
    ListExt.addAll(out, params.namedParams);
    out.forEach(
        (i) -> {
          if (i.name == null) {
            i.name = "";
          }
        });
    ListExt.sort(
        out,
        (a, b) -> {
          return StringExt.compareTo(a.name, b.name);
        });
    return out;
  }

  public void genEnum(Enum e) {
    hpp("");
    hpp("enum " + e.name + "{");
    e.values.forEach(
        (v) -> {
          hpp("    " + v + ",");
        });
    hpp("} // end " + e.name);
  }

  public void genMethodDecl(ClassDecl c, MethodDecl m, long depth) {
    while (depth > 0l) {
      hp("    ");
      depth--;
    }
    if (m.external) {
      hp("external ");
    }
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
    if (m.returnType == null) {
      if (c != null && Objects.equals(c.name, m.name)) {
      } else {
        hp("void ");
      }
    } else {
      hp(dataTypeToString(m.returnType));
    }
    hp(" ");
    hp(m.name);
    if (m.generics != null) {
      hp(generics(m.generics));
    }
    hp("(");
    if (m.params != null) {
      List<MethodParam> params = sortMethodParams(m.params);
      hp(
          IterableExt.join(
              ListExt.map(
                  params,
                  (p) -> {
                    if (p.dataType == null) {
                      if (Objects.equals(p.thisToken, "this") && c != null) {
                        DataType type = getFieldType(c, p.name);
                        return dataTypeToString(type) + " " + p.name;
                      }
                      return "Unknown";
                    } else {
                      return paramToString(p);
                    }
                  }),
              ", "));
    }
    hp(");");
    hpp("");
    if (m.body == null && m.init == null && m.exp == null) {
      return;
    }
    if (m.returnType == null) {
      if (c != null && Objects.equals(c.name, m.name)) {
      } else {
        cp("void");
        cp(" ");
      }
    } else {
      cp(dataTypeToString(m.returnType));
      cp(" ");
    }
    if (c == null) {
      cp(m.name);
    } else {
      cp(c.name);
      cp("::");
      cp(m.name);
    }
    if (m.generics != null) {
      cp(generics(m.generics));
    }
    cp("(");
    List<MethodParam> thisParams = ListExt.asList();
    if (m.params != null) {
      List<MethodParam> params = sortMethodParams(m.params);
      cp(
          IterableExt.join(
              ListExt.map(
                  params,
                  (p) -> {
                    if (p.dataType == null) {
                      if (Objects.equals(p.thisToken, "this") && c != null) {
                        thisParams.add(p);
                        DataType type = getFieldType(c, p.name);
                        return dataTypeToString(type) + " " + p.name;
                      }
                      return "Unknown";
                    } else {
                      return paramToString(p);
                    }
                  }),
              ", "));
    }
    cp(")");
    boolean isConstuctor = c != null && Objects.equals(c.name, m.name);
    if (isConstuctor) {
      cl(" {");
      if (m.init != null) {
        cp(tab(1l));
        genExp(m.init, 1l);
        cl("");
      }
      if (m.body != null) {
        cp(tab(1l));
        genBlock(m.body, 1l);
        cl("");
      }
      cl("}");
    } else if (m.body != null) {
      cp(" ");
      genBlock(m.body, 0l);
    } else if (m.exp != null) {
      cl(" {");
      cp("    return ");
      genExp(m.exp, 1l);
      cl(";");
      cl("}");
    }
    cpp("");
  }

  public void genExp(Expression exp, long depth) {
    if (exp instanceof ArrayAccess) {
      genArrayAccess(((ArrayAccess) exp), depth);
    } else if (exp instanceof ArrayExpression) {
      genArrayExpression(((ArrayExpression) exp), depth);
    } else if (exp instanceof ArrayItem) {
      genArrayItem(((ArrayItem) exp), depth);
    } else if (exp instanceof Assignment) {
      genAssignment(((Assignment) exp), depth);
    } else if (exp instanceof AwaitExpression) {
      genAwaitExpression(((AwaitExpression) exp), depth);
    } else if (exp instanceof BinaryExpression) {
      genBinaryExpression(((BinaryExpression) exp), depth);
    } else if (exp instanceof Block) {
      genBlock(((Block) exp), depth);
    } else if (exp instanceof Break) {
      genBreak(((Break) exp), depth);
    } else if (exp instanceof CascadeExp) {
      genCascadeExp(((CascadeExp) exp), depth);
    } else if (exp instanceof ConstExpression) {
      genConstExpression(((ConstExpression) exp), depth);
    } else if (exp instanceof Continue) {
      genContinue(((Continue) exp), depth);
    } else if (exp instanceof Declaration) {
      genDeclaration(((Declaration) exp), depth);
    } else if (exp instanceof DoWhileLoop) {
      genDoWhileLoop(((DoWhileLoop) exp), depth);
    } else if (exp instanceof DynamicTypeExpression) {
      genDynamicTypeExpression(((DynamicTypeExpression) exp), depth);
    } else if (exp instanceof FieldOrEnumExpression) {
      genFieldOrEnumExpression(((FieldOrEnumExpression) exp), depth);
    } else if (exp instanceof FnCallExpression) {
      genFnCallExpression(((FnCallExpression) exp), depth);
    } else if (exp instanceof ForEachLoop) {
      genForEachLoop(((ForEachLoop) exp), depth);
    } else if (exp instanceof ForLoop) {
      genForLoop(((ForLoop) exp), depth);
    } else if (exp instanceof IfStatement) {
      genIfStatement(((IfStatement) exp), depth);
    } else if (exp instanceof InlineMethodStatement) {
      genInlineMethodStatement(((InlineMethodStatement) exp), depth);
    } else if (exp instanceof LabelStatement) {
      genLabelStatement(((LabelStatement) exp), depth);
    } else if (exp instanceof LambdaExpression) {
      genLambdaExpression(((LambdaExpression) exp), depth);
    } else if (exp instanceof LiteralExpression) {
      genLiteralExpression(((LiteralExpression) exp), depth);
    } else if (exp instanceof MethodCall) {
      genMethodCall(((MethodCall) exp), depth);
    } else if (exp instanceof NullExpression) {
      genNullExpression(((NullExpression) exp), depth);
    } else if (exp instanceof ParExpression) {
      genParExpression(((ParExpression) exp), depth);
    } else if (exp instanceof PostfixExpression) {
      genPostfixExpression(((PostfixExpression) exp), depth);
    } else if (exp instanceof PrefixExpression) {
      genPrefixExpression(((PrefixExpression) exp), depth);
    } else if (exp instanceof RethrowStatement) {
      genRethrowStatement(((RethrowStatement) exp), depth);
    } else if (exp instanceof Return) {
      genReturn(((Return) exp), depth);
    } else if (exp instanceof SwitchExpression) {
      genSwitchExpression(((SwitchExpression) exp), depth);
    } else if (exp instanceof TerinaryExpression) {
      genTerinaryExpression(((TerinaryExpression) exp), depth);
    } else if (exp instanceof TypeCastOrCheckExpression) {
      genTypeCastOrCheckExpression(((TypeCastOrCheckExpression) exp), depth);
    } else if (exp instanceof WhileLoop) {
      genWhileLoop(((WhileLoop) exp), depth);
    } else if (exp instanceof YieldExpression) {
      genYieldExpression(((YieldExpression) exp), depth);
    } else if (exp instanceof ArrayAccess) {
      genArrayAccess(((ArrayAccess) exp), depth);
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

  public void genArrayAccess(ArrayAccess exp, long depth) {
    genExp(exp.on, depth);
    if (exp.checkNull) {
      cp("?");
    } else if (exp.notNull) {
      cp("!");
    }
    cp("[");
    genExp(exp.index, depth);
    cp("]");
  }

  public void genArrayExpression(ArrayExpression exp, long depth) {}

  public void genArrayItem(ArrayItem exp, long depth) {}

  public void genAssignment(Assignment exp, long depth) {
    genExp(exp.left, depth);
    cp(" = ");
    genExp(exp.right, depth);
  }

  public void genAwaitExpression(AwaitExpression exp, long depth) {
    cp("await ");
    genExp(exp.exp, depth);
  }

  public void genBinaryExpression(BinaryExpression exp, long depth) {
    genExp(exp.left, depth);
    cp(" ");
    cp(exp.op);
    cp(" ");
    genExp(exp.right, depth);
  }

  public void genBlock(Block exp, long depth) {
    if (exp == null) {
      return;
    }
    cl("{");
    exp.statements.forEach(
        (s) -> {
          cp(tab(depth + 1l));
          genExp(s, depth + 1l);
          if ((s instanceof WhileLoop)
              || (s instanceof ForLoop)
              || (s instanceof ForEachLoop)
              || (s instanceof Block)
              || (s instanceof IfStatement)) {
            /*
             No need for semicolon
            */
          } else {
            cl(";");
          }
        });
    cp(tab(depth));
    cp("}");
  }

  public void genBreak(Break exp, long depth) {
    cp(tab(depth));
    cp("break");
    if (exp.label != null) {
      cp(" ");
      cp(exp.label);
    }
  }

  public void genCascadeExp(CascadeExp exp, long depth) {}

  public void genConstExpression(ConstExpression exp, long depth) {
    cp("const ");
    genExp(exp.exp, depth);
  }

  public void genContinue(Continue exp, long depth) {
    cp("continue");
    if (exp.label != null) {
      cp(" ");
      cp(exp.label);
    }
  }

  public void genDeclaration(Declaration exp, long depth) {
    if (exp.type != null) {
      cp(dataTypeToString(exp.type));
    } else {
      cp("Unknown");
    }
    cp(" ");
    exp.names.forEach(
        (n) -> {
          cp(n.name);
          if (n.value != null) {
            cp(" = ");
            genExp(n.value, 0l);
          }
          if (!(Objects.equals(ListExt.last(exp.names), n))) {
            cp(", ");
          }
        });
  }

  public void genDoWhileLoop(DoWhileLoop exp, long depth) {
    cp("do ");
    genBlock(exp.body, depth);
    cp(" while (");
    genExp(exp.test, depth);
    cp(")");
  }

  public void genDynamicTypeExpression(DynamicTypeExpression exp, long depth) {}

  public void genFieldOrEnumExpression(FieldOrEnumExpression exp, long depth) {
    if (exp.on != null) {
      genExp(exp.on, depth);
      if (exp.checkNull) {
        cp("?.");
      } else if (exp.notNull) {
        cp("!.");
      } else {
        cp(".");
      }
    }
    cp(exp.name);
  }

  public void genFnCallExpression(FnCallExpression exp, long depth) {
    genExp(exp.on, depth);
    genMethodCall(exp.call, depth);
  }

  public void genForEachLoop(ForEachLoop exp, long depth) {
    cp("for (");
    if (exp.dataType != null) {
      cp(dataTypeToString(exp.dataType));
    } else {
      cp("auto");
    }
    cp(" ");
    cp(exp.name);
    cp(" : ");
    genExp(exp.collection, depth);
    cp(") ");
    genAsBlock(exp.body, depth);
    cl("");
  }

  public void genForLoop(ForLoop exp, long depth) {
    cp("for (");
    exp.inits.forEach(
        (i) -> {
          genExp(i, depth);
          if (!(Objects.equals(i, ListExt.last(exp.inits)))) {
            cp(", ");
          }
        });
    cp("; ");
    genExp(exp.test, depth);
    cp("; ");
    exp.resets.forEach(
        (i) -> {
          genExp(i, depth);
          if (!(Objects.equals(i, ListExt.last(exp.resets)))) {
            cp(", ");
          }
        });
    cp(") ");
    genAsBlock(exp.body, depth);
    cl("");
  }

  public void genAsBlock(Expression exp, long depth) {
    if (exp instanceof Block) {
      genExp(exp, depth);
    } else {
      cp(tab(depth));
      cl("{");
      cp(tab(depth + 1l));
      genExp(exp, depth);
      cl(";");
      cp(tab(depth));
      cp("}");
    }
  }

  public void genIfStatement(IfStatement exp, long depth) {
    cp("if (");
    genExp(exp.test, depth);
    cp(") ");
    genAsBlock(exp.thenStatement, depth);
    if (exp.elseStatement != null) {
      cp(" else ");
      genAsBlock(exp.elseStatement, depth);
      if (exp.elseStatement instanceof IfStatement) {
      } else {
        cl("");
      }
    } else {
      cl("");
    }
  }

  public void genInlineMethodStatement(InlineMethodStatement exp, long depth) {}

  public void genLabelStatement(LabelStatement exp, long depth) {}

  public void genLambdaExpression(LambdaExpression exp, long depth) {}

  public void genLiteralExpression(LiteralExpression exp, long depth) {
    switch (exp.type) {
      case TypeBoolean:
        {
          {
            cp(exp.value);
          }
          break;
        }
      case TypeString:
        {
          {
            cp("\"");
            cp(exp.value);
            cp("\"");
          }
          break;
        }
      case TypeDouble:
        {
          {
            cp(exp.value);
          }
          break;
        }
      case TypeInteger:
        {
          {
            cp(exp.value);
          }
          break;
        }
      default:
        {
        }
    }
  }

  public void genMethodCall(MethodCall exp, long depth) {
    if (exp.on != null) {
      genExp(exp.on, depth);
      if (exp.checkNull) {
        cp("?.");
      } else if (exp.notNull) {
        cp("!.");
      } else {
        cp(".");
      }
    }
    if (ListExt.isNotEmpty(exp.typeArgs)) {
      cp(typeArgsToString(exp.typeArgs));
    }
    if (exp.name != null) {
      cp(exp.name);
    }
    cp("(");
    exp.positionArgs.forEach(
        (a) -> {
          genExp(a.arg, depth);
          if (!(Objects.equals(a, ListExt.last(exp.positionArgs)))) {
            cp(", ");
          }
        });
    exp.namedArgs.forEach(
        (a) -> {
          genExp(a.value, depth);
          if (!(Objects.equals(a, ListExt.last(exp.namedArgs)))) {
            cp(", ");
          }
        });
    cp(")");
  }

  public void genNullExpression(NullExpression exp, long depth) {
    cp("nullptr");
  }

  public void genParExpression(ParExpression exp, long depth) {
    cp("(");
    genExp(exp.exp, depth);
    cp(")");
  }

  public void genPostfixExpression(PostfixExpression exp, long depth) {
    genExp(exp.on, depth);
    cp(exp.postfix);
  }

  public void genPrefixExpression(PrefixExpression exp, long depth) {
    cp(exp.prefix);
    genExp(exp.on, depth);
  }

  public void genRethrowStatement(RethrowStatement exp, long depth) {}

  public void genReturn(Return exp, long depth) {
    cp("return");
    if (exp.expression != null) {
      cp(" ");
      genExp(exp.expression, depth);
    }
  }

  public void genSwitchExpression(SwitchExpression exp, long depth) {}

  public void genTerinaryExpression(TerinaryExpression exp, long depth) {
    genExp(exp.condition, depth);
    cp("? ");
    genExp(exp.ifTrue, depth);
    cp(" : ");
    genExp(exp.ifFalse, depth);
  }

  public void genTypeCastOrCheckExpression(TypeCastOrCheckExpression exp, long depth) {
    if (exp.check) {
      genExp(exp.exp, depth);
      if (exp.isNot) {
        cp(" is! ");
      } else {
        cp(" is ");
      }
      cp(dataTypeToString(exp.dataType));
    } else {
      cp("(");
    }
  }

  public void genWhileLoop(WhileLoop exp, long depth) {
    cp("while (");
    genExp(exp.test, depth);
    cp(") ");
    genAsBlock(exp.body, depth);
    cl("");
  }

  public void genYieldExpression(YieldExpression exp, long depth) {
    cp("yield ");
    genExp(exp.exp, depth);
  }

  public DataType getFieldType(ClassDecl c, String name) {
    return (((FieldDecl)
            ListExt.firstWhere(
                c.members,
                (m) -> {
                  return (m instanceof FieldDecl) && Objects.equals(m.name, name);
                },
                null)))
        .type;
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
    */
    if (f.constValue) {
      hp("const ");
    }
    if (f.type == null) {
      hp("auto ");
    } else {
      hp(dataTypeToString(f.type));
    }
    hp(" ");
    hp(f.name);
    hp(";");
    hpp("");
  }
}
