package classes;

import d3e.core.D3ELogger;
import d3e.core.ListExt;
import d3e.core.StringExt;
import java.util.List;

public class CppGen implements Gen{
  public Dart2NSContext context;
  public String base;
  public List<String> cppLines = ListExt.asList();
  public List<String> hppLines = ListExt.asList();
  public List<String> cpLines = ListExt.asList();
  public List<String> hpLines = ListExt.asList();

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
    String fileName = StringExt.replaceAll(lib.path, ".dart", "");
    String upper = fileName.toUpperCase();
    hpp("#ifndef " + upper + "_H1");
    hpp("#define " + upper + "_H");
    cpp("#include \"" + fileName + ".h\"");
    hpp("#include <memory>");
    lib.exports.forEach(
        (e) -> {
          String path = StringExt.replaceAll(e.path, ".dart", "");
          hpp("#include \"" + path + ".h\"");
        });
    hpp("");
    lib.imports.forEach(
        (i) -> {
          String path = StringExt.replaceAll(i.path, ".dart", "");
          hpp("#include \"" + path + ".h\"");
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
    FileUtils.writeFile(outFolder + fileName + ".hpp", ListExt.join(this.hppLines, "\n"));
    FileUtils.writeFile(outFolder + fileName + ".cpp", ListExt.join(this.cppLines, "\n"));
    this.cppLines.clear();
    this.hppLines.clear();
  }

  public String libOutFolder(Library lib) {
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

  public void genClassDecl(ClassDecl c) {
    hpp("");
    if (c.isAbstract) {}
    hpp((c.isAbstract ? "abstract " : "") + "class " + c.name + " {");
    hpp("    public:");
    declareFields(c, true);
    hpp("    private:");
    declareFields(c, false);
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
                hp(f.type.name);
              }
              hp(" ");
              hp(f.name);
              hp(";");
              hpp("");
            });
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
