package classes;

import d3e.core.D3ELogger;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.SetExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Library implements TypeRegistry {
  public String fullPath;
  public String packagePath;
  public String id;
  public Library parent;
  public List<Import> imports = ListExt.asList();
  public List<Export> exports = ListExt.asList();
  public Map<String, TopDecl> objects = MapExt.Map();
  public List<Part> parts = ListExt.asList();
  public String partOf;
  public Map<String, PropType> types = MapExt.Map();
  public Map<String, TopDecl> cache = MapExt.Map();
  public Set<String> notFound = SetExt.Set();

  public Library(String fullPath, String packagePath, Library parent) {
    this.fullPath = fullPath;
    this.packagePath = packagePath;
    this.parent = parent;
  }

  public TopDecl get(String name) {
    if (name == null) {
      return null;
    }
    if (StringExt.startsWith(name, "__", 0l)) {
      return null;
    }
    return _getInternal(name, SetExt.Set(), true);
  }

  public TopDecl _getInternal(String name, Set<Library> checked, boolean checkImports) {
    /*
     if(notFound.contains(name)){
         return null;
     }
    */
    TopDecl obj = this.cache.get(name);
    if (obj != null) {
      return obj;
    }
    if (checked.contains(this)) {
      return null;
    }
    D3ELogger.info("Checking for \"" + name + "\" in " + this.packagePath);
    /*
     checked.add(this);
    */
    TopDecl top = this.objects.get(name);
    if (top == null) {
      /*
       Check in Exports
      */
      for (Export e : this.exports) {
        if (ListExt.isNotEmpty(e.show) && !e.show.contains(name)) {
          continue;
        }
        top = e.lib._getInternal(name, checked, false);
        if (top != null && e.hide.contains(name)) {
          top = null;
        }
        if (top != null) {
          break;
        }
      }
      if (checkImports) {
        /*
         Check in Imports
        */
        for (Import e : this.imports) {
          if (ListExt.isNotEmpty(e.show) && !e.show.contains(name)) {
            continue;
          }
          top = e.lib._getInternal(name, checked, false);
          if (top != null && e.hide.contains(name)) {
            top = null;
          }
          if (top != null) {
            break;
          }
        }
      }
    }
    if (top != null) {
      MapExt.set(this.cache, name, top);
    } else {
      this.notFound.add(name);
    }
    return top;
  }

  public void resolveFields(ResolveContext context) {
    D3ELogger.info("Resolving fields in Library: " + this.packagePath);
    context.currentLib = this;
    for (TopDecl obj : this.objects.values()) {
      if (obj instanceof FieldDecl) {
        (((FieldDecl) obj)).resolve(context);
      } else if (obj instanceof ClassDecl) {
        (((ClassDecl) obj)).resolveFields(context);
      }
    }
  }

  public void resolveMethods(ResolveContext context) {
    D3ELogger.info("Resolving methods in Library: " + this.packagePath);
    context.currentLib = this;
    for (TopDecl obj : this.objects.values()) {
      if (obj instanceof MethodDecl) {
        (((MethodDecl) obj)).resolve(context);
      } else if (obj instanceof ClassDecl) {
        (((ClassDecl) obj)).resolveMethods(context);
      }
    }
  }

  public void collectUsedTypes(List<DataType> list) {
    for (TopDecl obj : this.objects.values()) {
      obj.collectUsedTypes();
      ListExt.addAll(list, obj.usedTypes);
    }
  }

  public void addExtensions() {
    for (TopDecl obj : this.objects.values()) {
      if (obj instanceof ClassDecl) {
        (((ClassDecl) obj)).addExtensions();
      }
    }
  }

  public void simplify(Simplifier s) {
    for (TopDecl obj : this.objects.values()) {
      obj.simplify(s);
    }
  }

  public void visit(ExpressionVisitor visitor) {
    for (TopDecl obj : this.objects.values()) {
      obj.visit(visitor);
    }
  }

  public PropType getType(String name) {
    return this.types.get(name);
  }

  public void addType(PropType type) {
    MapExt.set(this.types, type.name, type);
  }

  public void validate() {
    ValidationContext ctx = new ValidationContext(this);
    for (TopDecl top : this.objects.values()) {
      top.register(ctx);
    }
    D3ELogger.info("Validating : " + this.packagePath);
    for (TopDecl top : this.objects.values()) {
      top.validate(ctx, 0l);
    }
    for (TopDecl top : this.objects.values()) {
      top.validate(ctx, 1l);
    }
  }

  public void addAll(List<TopDecl> list) {
    for (TopDecl i : list) {
      i.lib = this;
    }
    MapExt.addAll(
        this.objects,
        MapExt.fromIterable(
            list,
            (i) -> {
              return i.name;
            },
            (j) -> {
              return j;
            }));
  }
}
