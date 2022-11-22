package classes;

import d3e.core.D3ELogger;
import d3e.core.ListExt;
import d3e.core.StringExt;
import java.util.List;

public class ClassDecl extends TopDecl {
  public boolean isAbstract = false;
  public TypeParams generics;
  public DataType extendType;
  public List<DataType> impls = ListExt.asList();
  public List<ClassMember> members = ListExt.asList();
  public List<DataType> mixins = ListExt.asList();
  public List<DataType> ons = ListExt.asList();
  public boolean isMixin = false;
  public DataType mixinApplicationType;
  private String _packagePath;

  public ClassDecl(boolean isMixin, String name) {
    super(name, TopDeclType.Class, "");
    this.isMixin = isMixin;
  }

  public void collectUsedTypes() {
    ListExt.addAll(this.usedTypes, this.impls);
    if (this.extendType != null) {
      this.usedTypes.add(this.extendType);
    }
    for (ClassMember cm : this.members) {
      if (cm instanceof FieldDecl) {
        FieldDecl fd = ((FieldDecl) cm);
        if (fd.type != null) {
          this.usedTypes.add(fd.type);
        }
      } else if (cm instanceof MethodDecl) {
        MethodDecl md = ((MethodDecl) cm);
        if (md.returnType != null) {
          this.usedTypes.add(md.returnType);
        }
        for (MethodParam m : md.params) {
          if (m.dataType != null) {
            this.usedTypes.add(m.dataType);
          }
        }
      }
    }
  }

  public void resolveFields(ResolveContext context) {
    for (ClassMember cm : this.members) {
      cm.cls = this;
    }
    D3ELogger.info("Resolving Class: " + this.name);
    context.instanceClass = this;
    context.scope = new Scope(context.scope, null);
    for (ClassMember cm :
        ListExt.where(
            this.members,
            (m) -> {
              return m instanceof FieldDecl;
            })) {
      (((FieldDecl) cm)).resolve(context);
    }
    context.scope = context.scope.parent;
    context.instanceClass = null;
  }

  public void resolveMethods(ResolveContext context) {
    D3ELogger.info("Resolving Class: " + this.name);
    context.instanceClass = this;
    context.scope = new Scope(context.scope, null);
    for (ClassMember cm :
        ListExt.where(
            this.members,
            (m) -> {
              return m instanceof MethodDecl;
            })) {
      (((MethodDecl) cm)).resolve(context);
    }
    context.scope = context.scope.parent;
    context.instanceClass = null;
  }

  public void simplify(Simplifier s) {
    for (ClassMember cm : this.members) {
      cm.simplify(s);
    }
  }

  public String getPackagePath() {
    if (this._packagePath != null) {
      return this._packagePath;
    }
    String outPath = this.lib.packagePath;
    List<String> split = StringExt.split(outPath, "/");
    ListExt.removeLast(split);
    split.add(this.name);
    this._packagePath = ListExt.join(split, "/");
    return this._packagePath;
  }

  public String toString() {
    return this.name;
  }
}
