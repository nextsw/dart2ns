package classes;

import d3e.core.D3ELogger;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Dart2NSContext {
  public List<Library> libs = ListExt.asList();
  public List<Library> stack = ListExt.asList();
  public Library current;
  public static final String pkgBase =
      "/Users/rajesh/Downloads/flutter/.pub-cache/hosted/pub.dartlang.org/";
  public static Map<String, String> packages = Dart2NSContext.preparePackages();

  public Dart2NSContext() {}

  public static Map<String, String> preparePackages() {
    Map<String, String> pkgs = MapExt.Map();
    MapExt.set(pkgs, "characters", "1.2.1");
    MapExt.set(pkgs, "collection", "1.16.0");
    MapExt.set(pkgs, "material_color_utilities", "0.1.5");
    MapExt.set(pkgs, "meta", "1.8.0");
    MapExt.set(pkgs, "vector_math", "2.1.2");
    return pkgs;
  }

  public void start(String base, String path) {
    String fullPath = base + path;
    List<String> parts = StringExt.split(fullPath, "/");
    String last = ListExt.removeLast(parts);
    Library lib = new Library(ListExt.join(parts, "/") + "/", fullPath, path, last);
    this.libs.add(lib);
    this.current = lib;
    _parse(lib.fullPath);
  }

  public Library loadLibrary(String path) {
    Library lib = null;
    if (StringExt.startsWith(path, "package:", 0l)) {
      lib = packageLibrary(path);
    } else if (StringExt.startsWith(path, "dart:", 0l)) {
      lib = dartLibrary(path);
    } else {
      lib = relativeLibrary(path);
    }
    Library lib$final = lib;
    Library loadedLib =
        ListExt.firstWhere(
            this.libs,
            (l) -> {
              return Objects.equals(l.fullPath, lib$final.fullPath);
            },
            null);
    if (loadedLib == null) {
      this.libs.add(lib);
      _push(lib);
      _parse(lib.fullPath);
      _pop();
      D3ELogger.info("Resuming : " + this.current.fullPath);
      loadedLib = lib;
    }
    return loadedLib;
  }

  public void _push(Library lib) {
    this.stack.add(this.current);
    this.current = lib;
  }

  public void _pop() {
    this.current = ListExt.removeLast(this.stack);
  }

  public boolean loadPart(String part) {
    String fullPath = Dart2NSContext.join(this.current.base, part);
    Part p = new Part(part, this.current);
    this.current.parts.add(p);
    _parse(fullPath);
    return false;
  }

  public Export loadExport(String path) {
    Library lib = loadLibrary(path);
    Export exp = new Export(this.current, path, lib);
    this.current.exports.add(exp);
    return exp;
  }

  public Import loadImport(String path) {
    Library lib = loadLibrary(path);
    Import imp = new Import(this.current, path, lib);
    this.current.imports.add(imp);
    return imp;
  }

  public void _parse(String path) {
    D3ELogger.info("Parsing : " + path);
    String content = FileUtils.readContent(path);
    List<TopDecl> list = TypeParser.parse(this, content);
    ListExt.addAll(this.current.objects, list);
  }

  public static String join(String base, String sub) {
    return base + "/" + sub;
  }

  public Library packageLibrary(String path) {
    List<String> split = StringExt.split(path, ":");
    List<String> pathItems = StringExt.split(ListExt.get(split, 1l), "/");
    String first = ListExt.first(pathItems);
    ListExt.removeLast(pathItems);
    String version = Dart2NSContext.packages.get(first);
    String newBase = Dart2NSContext.pkgBase + first + "-" + version + "/lib/";
    if (Objects.equals(ListExt.get(pathItems, 0l), "flutter")) {
      newBase = "/Users/rajesh/Downloads/flutter/packages/flutter/lib/";
    }
    String fullPath = newBase + ListExt.join(pathItems, "/");
    return new Library(newBase, fullPath, path, ListExt.join(pathItems, "/"));
  }

  public Library dartLibrary(String path) {
    List<String> split = StringExt.split(path, ":");
    String base = "/Users/rajesh/Downloads/flutter/bin/cache/pkg/sky_engine/lib/";
    String fullPath = base + "/" + ListExt.get(split, 1l) + "/" + ListExt.get(split, 1l) + ".dart";
    return new Library(
        base + "/" + ListExt.get(split, 1l) + "/",
        fullPath,
        path,
        ListExt.get(split, 1l) + ".dart");
  }

  public Library relativeLibrary(String path) {
    String fullPath = this.current.base + path;
    List<String> parts = StringExt.split(fullPath, "/");
    String last = ListExt.removeLast(parts);
    return new Library(ListExt.join(parts, "/") + "/", fullPath, path, last);
  }
}
