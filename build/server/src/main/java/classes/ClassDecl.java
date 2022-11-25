package classes;

import d3e.core.D3ELogger;
import d3e.core.IterableExt;
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
  private Map<String, ClassMember> _members = MapExt.Map();
  public List<DataType> mixins = ListExt.asList();
  public List<DataType> ons = ListExt.asList();
  public List<DataType> extensions = ListExt.asList();
  public boolean isMixin = false;
  public DataType mixinApplicationType;
  private String _packagePath;
  public boolean isExtension = false;
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
    for (ClassMember cm : this.getMembers()) {
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
    Map<String, DataType> ourGenercis = MapExt.Map();
    if (this.generics != null) {
      for (TypeParam p : this.generics.params) {
        if (p.extendType != null) {
          MapExt.set(ourGenercis, p.name, p.extendType);
        } else {
          MapExt.set(ourGenercis, p.name, new ValueType(p.name, false));
        }
      }
    }
    MapExt.set(this.resolvedGenerics, this.name, ourGenercis);
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
      TopDecl ex = this.lib.get(type.name);
      if (ex != null && ex instanceof ClassDecl) {
        ClassDecl cls = ((ClassDecl) ex);
        cls.resolveGenerics(context);
        Map<String, DataType> ourGenercis = this.resolvedGenerics.get(this.name);
        ValueType extType = ((ValueType) type);
        Map<String, DataType> replaced = MapExt.Map();
        for (long x = 0l; x < ListExt.length(extType.args); x++) {
          DataType arg = ListExt.get(extType.args, x);
          TypeParam param = ListExt.get(cls.generics.params, x);
          if (ourGenercis.containsKey(arg.name)) {
            arg = ourGenercis.get(arg.name);
          }
          MapExt.set(replaced, param.name, arg);
        }
        addResolvedGenerics(cls.resolvedGenerics, replaced);
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
    this._members.forEach(
        (k, cm) -> {
          cm.cls = this;
        });
    resolveGenerics(context);
    /*
     D3ELogger.info('Resolving Class: ' + name);
    */
    context.instanceClass = this;
    context.scope = new Scope(context.scope, null);
    for (ClassMember cm :
        IterableExt.where(
            this.getFields(),
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
        IterableExt.where(
            this.getMethods(),
            (m) -> {
              return m instanceof MethodDecl;
            })) {
      (((MethodDecl) cm)).resolve(context);
    }
    context.scope = context.scope.parent;
    context.instanceClass = null;
  }

  public void addExtensions() {
    if (this.isExtension) {
      for (DataType t : this.ons) {
        TopDecl top = this.lib.get(t.name);
        if (top instanceof ClassDecl) {
          ClassDecl cls = ((ClassDecl) top);
          ValueType type = new ValueType(this.name, false);
          if (this.generics != null) {
            for (TypeParam p : this.generics.params) {
              type.args.add(p.toType());
            }
          }
          cls.extensions.add(type);
        } else if (top instanceof Typedef) {
          Typedef def = ((Typedef) top);
          ValueType type = new ValueType(this.name, false);
          if (this.generics != null) {
            for (TypeParam p : this.generics.params) {
              type.args.add(p.toType());
            }
          }
          def.extensions.add(type);
        }
      }
    }
  }

  public void simplify(Simplifier s) {
    for (ClassMember cm : this.getMembers()) {
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
    for (ClassMember cm : this.getMembers()) {
      cm.visit(visitor);
    }
  }

  public Iterable<FieldDecl> getFields() {
    return IterableExt.map(
        IterableExt.where(
            this.getMembers(),
            (m) -> {
              return m instanceof FieldDecl;
            }),
        (m) -> {
          return ((FieldDecl) m);
        });
  }

  public Iterable<MethodDecl> getMethods() {
    return IterableExt.map(
        IterableExt.where(
            this.getMembers(),
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
    for (ClassMember m : this.getMembers()) {
      m.validate(ctx, phase);
    }
  }

  public void register(ValidationContext ctx) {
    for (ClassMember cm : this.getMembers()) {
      cm.cls = this;
    }
    this.type = new ClassType(this.name);
    this.type.cls = this;
    ctx.addToCurrent(this.type);
  }

  public ClassMember get(String name) {
    return this._members.get(name);
  }

  public FieldDecl field(String name) {
    ClassMember cm = this._members.get(name);
    if (cm instanceof FieldDecl) {
      return ((FieldDecl) cm);
    }
    /*
     if(extendType != null) {
         ClassDecl ext = getClass(extendType);
         cm = ext.method(name);
         if(cm is FieldDecl){
             return cm as FieldDecl;
         }
     }
     for(DataType i in impls){
         ClassDecl iCls = getClass(i);
         cm = iCls.method(name);
         if(cm is FieldDecl){
             return cm as FieldDecl;
         }
     }
     for(DataType i in ons){
         // ctx.validateType(i);
         //type.impls.add(i.resolvedType);
     }
     for(DataType i in mixins){
         ClassDecl iCls = getClass(i);
         cm = iCls.method(name);
         if(cm is FieldDecl){
             return cm as FieldDecl;
         }
     }
    */
    return null;
  }

  public MethodDecl method(String name) {
    ClassMember cm = this._members.get(name);
    if (cm instanceof MethodDecl) {
      return ((MethodDecl) cm);
    }
    /*
     if(extendType != null) {
         ClassDecl ext = getClass(extendType);
         cm = ext.method(name);
         if(cm is MethodDecl){
             return cm as MethodDecl;
         }
     }
     for(DataType i in impls){
         ClassDecl iCls = getClass(i);
         cm = iCls.method(name);
         if(cm is MethodDecl){
             return cm as MethodDecl;
         }
     }
     for(DataType i in ons){
         // ctx.validateType(i);
         //type.impls.add(i.resolvedType);
     }
     for(DataType i in mixins){
         ClassDecl iCls = getClass(i);
         cm = iCls.method(name);
         if(cm is MethodDecl){
             return cm as MethodDecl;
         }
     }
    */
    return null;
  }

  public ClassDecl getClass(DataType t) {
    TopDecl top = this.lib.get(t.name);
    return ((ClassDecl) top);
  }

  public Iterable<ClassMember> getMembers() {
    return this._members.values();
  }

  public void add(ClassMember cm) {
    String name = cm.name;
    if (cm instanceof MethodDecl) {
      MethodDecl m = ((MethodDecl) cm);
      if (m.factoryName != null) {
        name = name + "." + m.factoryName;
      }
    }
    MapExt.set(this._members, name, cm);
  }
}
