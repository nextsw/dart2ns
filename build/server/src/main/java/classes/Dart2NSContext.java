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
  public String flutterHome;
  public String pkgBase;
  public static Map<String, String> packages = Dart2NSContext.preparePackages();
  public Map<String, TopDecl> objects = MapExt.Map();

  public Dart2NSContext(String flutterHome) {
    this.flutterHome = flutterHome;
    this.pkgBase = flutterHome + ".pub-cache/hosted/pub.dartlang.org/";
  }

  public static Map<String, String> preparePackages() {
    Map<String, String> pkgs = MapExt.Map();
    MapExt.set(pkgs, "characters", "1.2.1");
    MapExt.set(pkgs, "collection", "1.16.0");
    MapExt.set(pkgs, "material_color_utilities", "0.1.5");
    MapExt.set(pkgs, "meta", "1.8.0");
    MapExt.set(pkgs, "vector_math", "2.1.2");
    return pkgs;
  }

  public void add(TopDecl obj) {
    obj.lib = this.current;
    if (obj.name == null) {
      return;
    }
    TopDecl existing = this.objects.get(obj.name);
    if (existing != null) {
      D3ELogger.error("Another object exists with same name:" + obj.name);
    }
    MapExt.set(this.objects, obj.name, obj);
  }

  public TopDecl get(String name) {
    return this.objects.get(name);
  }

  public void start(String packagePath) {
    Library lib = new Library(this.flutterHome + packagePath + ".dart", packagePath, null);
    this.libs.add(lib);
    this.current = lib;
    /*
     loadImport('dart:core');
    */
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
      if (!(Objects.equals(path, "dart:core"))) {
        loadImport("dart:core");
      }
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

  public void addPartOf(String partOf) {
    this.current.partOf = partOf;
  }

  public boolean loadPart(String path) {
    Library lib = loadLibrary(path);
    path = StringExt.replaceAll(path, ".dart", "");
    Part p = new Part(this.current, path, lib);
    this.current.parts.add(p);
    return false;
  }

  public Export loadExport(String path) {
    Library lib = loadLibrary(path);
    path = StringExt.replaceAll(path, ".dart", "");
    Export exp = new Export(this.current, path, lib);
    this.current.exports.add(exp);
    return exp;
  }

  public Import loadImport(String path) {
    Library lib = loadLibrary(path);
    path = StringExt.replaceAll(path, ".dart", "");
    Import imp = new Import(this.current, path, lib);
    this.current.imports.add(imp);
    return imp;
  }

  public void _parse(String path) {
    D3ELogger.info("Parsing : " + path);
    String content = FileUtils.readContent(path);
    if (content.isEmpty()) {
      D3ELogger.error("Can not read file: " + path);
      return;
    }
    List<TopDecl> list = TypeParser.parse(this, content);
    if (this.current.partOf != null) {
      ListExt.addAll(this.current.parent.objects, list);
    } else {
      ListExt.addAll(this.current.objects, list);
    }
  }

  public static String join(String base, String sub) {
    return base + "/" + sub;
  }

  public Library packageLibrary(String path) {
    List<String> split = StringExt.split(path, ":");
    List<String> pathItems = StringExt.split(ListExt.get(split, 1l), "/");
    String first = ListExt.first(pathItems);
    String version = Dart2NSContext.packages.get(first);
    String newBase = this.pkgBase + first + "-" + version + "/lib/";
    if (Objects.equals(ListExt.get(pathItems, 0l), "flutter")) {
      newBase = this.flutterHome + "packages/flutter/lib/";
    }
    ListExt.removeAt(pathItems, 0l);
    String fullPath = newBase + ListExt.join(pathItems, "/");
    return new Library(fullPath, "packages/" + first + "/" + first, null);
  }

  public Library dartLibrary(String path) {
    List<String> split = StringExt.split(path, ":");
    String base = this.flutterHome + "bin/cache/pkg/sky_engine/lib/";
    String sub = ListExt.get(split, 1l);
    String fileName = sub;
    if (StringExt.startsWith(fileName, "_", 0l)) {
      fileName = StringExt.substring(sub, 1l, 0l);
    }
    if (Objects.equals(sub, "_internal")) {
      sub = "internal";
    }
    String fullPath = base + sub + "/" + fileName + ".dart";
    String packagePath = "dart/" + sub + "/" + fileName;
    return new Library(fullPath, packagePath, null);
  }

  public Library relativeLibrary(String path) {
    String fullPath = this.current.fullPath;
    List<String> parts = StringExt.split(fullPath, "/");
    ListExt.removeLast(parts);
    List<String> subs = StringExt.split(path, "/");
    ListExt.addAll(parts, subs);
    String fp = ListExt.join(parts, "/");
    path = StringExt.replaceAll(path, ".dart", "");
    subs = StringExt.split(path, "/");
    String packagePath = this.current.packagePath;
    parts = StringExt.split(packagePath, "/");
    ListExt.removeLast(parts);
    ListExt.addAll(parts, subs);
    String pp = ListExt.join(parts, "/");
    return new Library(fp, pp, this.current);
  }
}
