package classes;

import d3e.core.D3ELogger;
import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.StringExt;
import java.util.List;

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
    String outPath = lib.path;
    outPath = StringExt.replaceAll(outPath, ".dart", "");
    List<String> split = StringExt.split(outPath, "/");
    String nameOnly = ListExt.last(split);
    String upper = nameOnly.toUpperCase();
    hpp("#ifndef " + upper + "_H");
    hpp("#define " + upper + "_H");
    cpp("#include \"" + nameOnly + ".hpp\"");
    hpp("#include <memory>");
    lib.exports.forEach(
        (e) -> {
          String path = StringExt.replaceAll(e.path, ".dart", "");
          if (StringExt.startsWith(path, "package:", 0l)
              || StringExt.startsWith(path, "dart:", 0l)) {
            path = ListExt.get(StringExt.split(path, ":"), 1l);
            hpp("#include <" + path + ".hpp>");
          } else {
            hpp("#include \"" + path + ".hpp\"");
          }
        });
    hpp("");
    lib.imports.forEach(
        (i) -> {
          String path = StringExt.replaceAll(i.path, ".dart", "");
          if (StringExt.startsWith(path, "package:", 0l)
              || StringExt.startsWith(path, "dart:", 0l)) {
            path = ListExt.get(StringExt.split(path, ":"), 1l);
            hpp("#include <" + path + ".hpp>");
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
            genMethodDecl(((MethodDecl) obj));
          } else if (obj instanceof FieldDecl) {
            genFieldDecl(((FieldDecl) obj));
          } else {
            D3ELogger.error("Unknown object type");
          }
        });
    hpp("");
    hpp("#endif");
    String outFolder = libOutFolder(lib);
    FileUtils.writeFile(outFolder + outPath + ".hpp", ListExt.join(this.hppLines, "\n"));
    FileUtils.writeFile(outFolder + outPath + ".cpp", ListExt.join(this.cppLines, "\n"));
    this.cppLines.clear();
    this.hppLines.clear();
  }

  public String libOutFolder(Library lib) {
    if (lib.parent != null) {
      Library top = lib.parent;
      while (top.parent != null) {
        top = top.parent;
      }
      String out = libOutFolder(top);
      String extra = StringExt.replaceAll(lib.base, top.base, "");
      out += extra;
      return out;
    }
    if (StringExt.startsWith(lib.packagePath, "package:", 0l)) {
      List<String> split = StringExt.split(lib.packagePath, ":");
      split = StringExt.split(ListExt.last(split), "/");
      return this.base + "packages/" + ListExt.first(split) + "/";
    } else if (StringExt.startsWith(lib.packagePath, "dart:", 0l)) {
      List<String> split = StringExt.split(lib.packagePath, ":");
      return this.base + "dart/" + ListExt.last(split) + "/";
    } else {
      return this.base + "packages/flutter/";
    }
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

  public String typeArgsToList(List<DataType> args) {
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
    if (ListExt.isNotEmpty(v.args)) {
      res += typeArgsToList(v.args);
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
    hpp("} // end " + c.name);
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
              FieldDecl f = ((FieldDecl) m);
              hp("    ");
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
              hp("    ");
              if (m.staticValue) {
                hp("static ");
              }
              /*
               if(f.final) {
                   s += 'final ';
               }
              */
              if (m.constValue) {
                hp("const ");
              }
              if (m.type == null) {
                hp("void ");
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
                              return paramToString(p);
                            }),
                        ", "));
              }
              hp(");");
              hpp("");
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

  public void genMethodDecl(MethodDecl m) {}

  public void genTypeDef(Typedef t) {}

  public void genFieldDecl(FieldDecl f) {}
}
