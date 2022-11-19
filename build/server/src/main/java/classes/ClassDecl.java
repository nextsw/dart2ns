package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.SetExt;
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
    SetExt.addAll(
        this.usedTypes,
        IterableExt.toList(
            ListExt.map(
                this.impls,
                (i) -> {
                  return i.name;
                }),
            false));
    if (this.extendType != null) {
      this.usedTypes.add(this.extendType.name);
    }
    for (ClassMember cm : this.members) {
      if (cm instanceof FieldDecl) {
        FieldDecl fd = ((FieldDecl) cm);
        if (fd.type != null) {
          fd.type.collectUsedTypes(this.usedTypes);
        }
      } else if (cm instanceof MethodDecl) {
        MethodDecl md = ((MethodDecl) cm);
        if (md.returnType != null) {
          md.returnType.collectUsedTypes(this.usedTypes);
        }
        if (md.params != null) {
          for (MethodParam m : md.params.positionalParams) {
            if (m.dataType != null) {
              m.dataType.collectUsedTypes(this.usedTypes);
            }
          }
        }
      }
    }
    SetExt.addAll(
        this.usedTypes,
        IterableExt.toList(
            ListExt.map(
                this.impls,
                (i) -> {
                  return i.name;
                }),
            false));
  }

  public void resolve(ResolveContext context) {
    context.instanceClass = this;
    context.scope = new Scope(context.scope, null);
    for (ClassMember cm :
        ListExt.where(
            this.members,
            (m) -> {
              return m instanceof FieldDecl;
            })) {
      cm.resolve(context);
    }
    for (ClassMember cm : this.members) {
      cm.resolve(context);
    }
    context.scope = context.scope.parent;
    context.instanceClass = null;
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
