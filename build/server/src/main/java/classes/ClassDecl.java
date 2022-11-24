package classes;

import d3e.core.D3ELogger;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Map;

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
  public Map<String, Map<String, DataType>> resolvedGenerics;
  public ClassType type;

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
        for (MethodParam m : md.allParams) {
          if (m.dataType != null) {
            this.usedTypes.add(m.dataType);
          }
        }
      }
    }
  }

  public void resolveGenerics(ResolveContext context) {
    if (this.resolvedGenerics != null) {
      return;
    }
    this.resolvedGenerics = MapExt.Map();
    if (this.generics != null) {
      Map<String, DataType> ourGenercis = MapExt.Map();
      for (TypeParam p : this.generics.params) {
        if (p.extendType != null) {
          MapExt.set(ourGenercis, p.name, p.extendType);
        } else {
          MapExt.set(ourGenercis, p.name, new ValueType(p.name, false));
        }
      }
      MapExt.set(this.resolvedGenerics, this.name, ourGenercis);
    }
    /*
     Check extends
    */
    if (this.extendType != null) {
      resolveGenericsForType(context, this.extendType);
    }
    this.impls.forEach(
        (i) -> {
          resolveGenericsForType(context, i);
        });
    this.mixins.forEach(
        (i) -> {
          resolveGenericsForType(context, i);
        });
    D3ELogger.info("Resolved Generics for " + this.name);
  }

  public void resolveGenericsForType(ResolveContext context, DataType type) {
    if (type != null) {
      ClassDecl ex = ((ClassDecl) this.lib.get(type.name));
      if (ex != null) {
        ex.resolveGenerics(context);
        ValueType extType = ((ValueType) type);
        Map<String, DataType> replaced = MapExt.Map();
        for (long x = 0l; x < ListExt.length(extType.args); x++) {
          DataType arg = ListExt.get(extType.args, x);
          TypeParam param = ListExt.get(ex.generics.params, x);
          MapExt.set(replaced, param.name, arg);
        }
        addResolvedGenerics(ex.resolvedGenerics, replaced);
      } else {
        context.error("Unable to find Type: " + type.name);
      }
    }
  }

  public void addResolvedGenerics(
      Map<String, Map<String, DataType>> from, Map<String, DataType> replaced) {
    from.forEach(
        (k, v) -> {
          Map<String, DataType> temp = MapExt.Map();
          v.forEach(
              (k1, v1) -> {
                DataType value$ = replaced.get(k1);
                if (value$ == null) {
                  value$ = v1;
                }
                DataType r = value$;
                MapExt.set(temp, k1, r);
              });
          MapExt.set(this.resolvedGenerics, k, temp);
        });
  }

  public void resolveFields(ResolveContext context) {
    for (ClassMember cm : this.members) {
      cm.cls = this;
    }
    resolveGenerics(context);
    /*
     D3ELogger.info('Resolving Class: ' + name);
    */
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
    /*
     D3ELogger.info('Resolving Class: ' + name);
    */
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

  public void visit(ExpressionVisitor visitor) {
    for (ClassMember cm : this.members) {
      cm.visit(visitor);
    }
  }

  public Iterable<FieldDecl> getFields() {
    return ListExt.map(
        ListExt.where(
            this.members,
            (m) -> {
              return m instanceof FieldDecl;
            }),
        (m) -> {
          return ((FieldDecl) m);
        });
  }

  public Iterable<MethodDecl> getMethods() {
    return ListExt.map(
        ListExt.where(
            this.members,
            (m) -> {
              return m instanceof MethodDecl;
            }),
        (m) -> {
          return ((MethodDecl) m);
        });
  }

  public void validate(ValidationContext ctx, long phase) {
    D3ELogger.info("Validating : " + this.name);
    ctx.dataType = this.type;
    if (phase == 0l) {
      if (this.generics != null) {
        ExpressionValidationUtil.validateTypeParams(this.generics, ctx);
        this.generics
            .resolveRawTypes(ctx, this.type, null, null)
            .forEach(
                (i) -> {
                  this.type.typeVars.add(i);
                });
      }
      if (this.extendType != null) {
        ctx.validateType(this.extendType);
        this.type.extendsValue = this.extendType.resolvedType;
      }
      for (DataType i : this.impls) {
        ctx.validateType(i);
        this.type.impls.add(i.resolvedType);
      }
      for (DataType i : this.ons) {
        ctx.validateType(i);
        /*
        type.impls.add(i.resolvedType);
        */
      }
      for (DataType i : this.mixins) {
        ctx.validateType(i);
      }
    }
    for (ClassMember m : this.members) {
      m.validate(ctx, phase);
    }
  }

  public void register(ValidationContext ctx) {
    for (ClassMember cm : this.members) {
      cm.cls = this;
    }
    this.type = new ClassType(this.name);
    this.type.cls = this;
    ctx.addToCurrent(this.type);
  }
}
