package classes;

import d3e.core.D3ELogger;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.SetExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ValidationContext {
  public TypeRegistry registry;
  private Map<String, LocalVar> _valirables = MapExt.Map();
  public ValidationContext parent;
  private FieldDecl _property;
  public Object data;
  public PropType dataType;
  public PropType expectedType;
  public PropType typeHolder;
  public Map<String, PropType> genericTypes;
  public TypeParams generics;
  private long _stackSize = 0l;
  public Range objRange;
  private String _usageContext;
  public String className;
  public String methodName;
  public Statement statement;
  public boolean insideLambda = false;
  private Map<String, Object> _attrs = MapExt.Map();
  private boolean _scopeBoundary = false;

  public ValidationContext(TypeRegistry registry) {
    this.registry = registry;
  }

  public PropType wrap(PropType baseType, PropType element) {
    ParameterizedType type = ParameterizedType.from(baseType);
    type.addArgument(element);
    return type;
  }

  public PropType list(PropType element) {
    return wrap(getType(PropType.LIST_TYPE), element);
  }

  public PropType set(PropType element) {
    return wrap(getType(PropType.SET_TYPE), element);
  }

  public PropType nullType() {
    return this.registry.getType("null");
  }

  public PropType voidType() {
    return this.registry.getType("void");
  }

  public PropType bool() {
    return this.registry.getType("bool");
  }

  public PropType object() {
    return this.registry.getType("Object");
  }

  public PropType findSuperType(List<PropType> all) {
    if (all.isEmpty()) {
      return null;
    }
    PropType reduce =
        ListExt.reduce(
            all,
            (a, b) -> {
              return this.getCommonType(a, b);
            });
    return reduce != null ? reduce : object();
  }

  public PropType getCommonType(PropType lt, PropType rt) {
    if (rt == null || lt == null) {
      return object();
    }
    if (Objects.equals(lt, nullType())) {
      return rt;
    }
    if (Objects.equals(rt, nullType())) {
      return lt;
    }
    if (lt.isAssignableFrom(rt)) {
      return lt;
    }
    if (rt.isAssignableFrom(lt)) {
      return rt;
    }
    if ((lt instanceof ParameterizedType) || (rt instanceof ParameterizedType)) {
      PropType ltBase = lt;
      PropType rtBase = rt;
      if (lt instanceof ParameterizedType) {
        ltBase = (((ParameterizedType) lt)).baseType;
      }
      if (rt instanceof ParameterizedType) {
        rtBase = (((ParameterizedType) rt)).baseType;
      }
      PropType commonType = getCommonType(ltBase, rtBase);
      if (commonType instanceof ParameterizedType) {
        return commonType;
      }
      if (commonType.typeVars.isEmpty()) {
        return commonType;
      }
      ParameterizedType pt = ParameterizedType.from(commonType);
      commonType.typeVars.forEach(
          (t) -> {
            pt.addArgumentWithName(t.name, this.object());
          });
      return pt;
    }
    if (Objects.equals(lt.name, "Object")) {
      return lt;
    }
    if (Objects.equals(lt.name, "Object")) {
      return rt;
    }
    List<PropType> ltAll = ListExt.List(0l);
    if (lt.extendsValue != null) {
      ltAll.add(lt.extendsValue);
    }
    ListExt.addAll(ltAll, lt.impls);
    Set<PropType> commonTypes = SetExt.Set();
    for (PropType p : ltAll) {
      commonTypes.add(getCommonType(p, rt));
    }
    List<PropType> arrayList = ListExt.of(commonTypes, false);
    if (ListExt.length(arrayList) == 1l) {
      return ListExt.get(arrayList, 0l);
    }
    if (ListExt.length(arrayList) == 2l) {
      PropType nonObject = null;
      if (Objects.equals(ListExt.get(arrayList, 0l).name, "Object")) {
        nonObject = ListExt.get(arrayList, 1l);
      }
      if (Objects.equals(ListExt.get(arrayList, 1l).name, "Object")) {
        nonObject = ListExt.get(arrayList, 0l);
      }
      if (nonObject != null) {
        return nonObject;
      }
    }
    ClassType classType = new ClassType("#" + arrayList.toString());
    classType.impls = arrayList;
    return classType;
  }

  public PropType getTypeOrObject(String type) {
    PropType propType = type == null ? null : this.getType(type);
    return propType == null ? this.object() : propType;
  }

  public PropType typeOrObject(Expression exp) {
    return exp == null || exp.expType == null ? this.object() : exp.expType;
  }

  public PropType getType(String type) {
    PropType propType = this.genericType(type);
    if (propType != null) {
      return propType;
    }
    return this.searchType(type);
  }

  public PropType searchType(String type) {
    return this.registry.getType(type);
  }

  public PropType createLambdaFunctionType(
      PropType declType,
      PropType retType,
      String value,
      List<MethodParam> params,
      List<TypeVariable> typeVars,
      boolean raw) {
    MethodDecl method =
        new MethodDecl(
            ListExt.List(),
            null,
            null,
            false,
            null,
            false,
            false,
            null,
            false,
            null,
            false,
            null,
            value,
            false,
            null,
            null,
            false,
            false);
    /*
     method.params.positionalParams = MethodParamsUtil.createPositionalParams(params, this, declType, typeVars, raw);
     method.params.namedParams = MethodParamsUtil.createNamedParams(params, this, declType, typeVars, raw);
     method.params.optionalParams = MethodParamsUtil.createOptionalParams(params, this, declType, typeVars, raw);
    */
    method.returnType = retType.toDataType();
    LambdaType lambdaType = LambdaType.withMethod(method);
    declType.typeVars.forEach(
        (i) -> {
          lambdaType.typeVars.add(i);
        });
    if (typeVars != null) {
      /*
       method.typeVars = typeVars;
      */
      typeVars.forEach(
          (i) -> {
            lambdaType.typeVars.add(i);
          });
    }
    if (declType instanceof ParameterizedType) {
      ParameterizedType dpt = (((ParameterizedType) declType));
      ParameterizedType pt = ParameterizedType.from(lambdaType);
      ListExt.addAll(pt.arguments, dpt.arguments);
      return pt;
    }
    /*
     It should have the type vars and arguments (If available)
    */
    return lambdaType;
  }

  public LocalVar addLocalVar(PropType type, String name, Object value) {
    LocalVar localVar = new LocalVar();
    localVar.name = name;
    localVar.type = type;
    localVar.value = value;
    MapExt.set(this._valirables, name, localVar);
    return localVar;
  }

  public LocalVar findLocalVar(String name) {
    if (this._valirables.containsKey(name)) {
      return this._valirables.get(name);
    }
    return this.parent == null ? null : this.parent.findLocalVar(name);
  }

  public Map<String, LocalVar> getLocalVars() {
    Map<String, LocalVar> vars = MapExt.Map();
    this._valirables.forEach(
        (k, v) -> {
          if (k == null || k.isEmpty() || StringExt.startsWith(k, "#", 0l)) {
            return;
          }
          MapExt.set(vars, k, v);
        });
    if (this._scopeBoundary) {
      return vars;
    }
    if (this.parent != null) {
      MapExt.addAll(vars, this.parent.getLocalVars());
    }
    return vars;
  }

  public void markScopeBoundary() {
    this._scopeBoundary = true;
  }

  public void addLambdaUsed(LocalVar var, boolean insideLambda) {
    if (this._valirables.containsKey(var.name)) {
      if (insideLambda) {
        if (this.statement != null) {
          for (LocalVar v : this.statement.finalVars) {
            if (Objects.equals(v.name, var.name)) {
              return;
            }
          }
          this.statement.finalVars.add(var);
        }
      }
    } else {
      this.parent.addLambdaUsed(var, insideLambda || this.insideLambda);
    }
  }

  public PropType getLocalVarType(String name) {
    return this.findLocalVar(name).type;
  }

  public boolean hasLocalVar(String name) {
    return this.findLocalVar(name) != null;
  }

  public boolean updateLocalVarValue(String name) {
    return this.findLocalVar(name) != null;
  }

  public void markInitialized(String name) {
    LocalVar var = this.findLocalVar(name);
    if (var != null) {
      var.markInitialized();
    }
  }

  public void markNotInitialized(String name) {
    LocalVar var = this.findLocalVar(name);
    if (var != null) {
      var.markNotInitialized();
    }
  }

  public List<String> getAllLocalVariables() {
    List<String> all;
    if (this.parent != null) {
      all = this.parent.getAllLocalVariables();
    } else {
      all = ListExt.List(0l);
    }
    ListExt.addAll(all, MapExt.keys(this._valirables));
    return all;
  }

  public void addAttribute(String name, Object value) {
    MapExt.set(this._attrs, name, value);
  }

  public Object getAttribute(String name) {
    if (this._attrs.containsKey(name)) {
      return this._attrs.get(name);
    }
    if (this.parent != null) {
      return this.parent.getAttribute(name);
    }
    return null;
  }

  public void setAttribute(String name, Object value) {
    if (this._setInternal(name, value)) {
      return;
    }
    this.parent.setAttribute(name, value);
  }

  public boolean _setInternal(String name, Object value) {
    if (!this._attrs.containsKey(name)) {
      return false;
    }
    MapExt.set(this._attrs, name, value);
    return true;
  }

  public ValidationContext subWithField(FieldDecl prop) {
    ValidationContext sub = this.createSub();
    sub._property = prop;
    sub.expectedType = prop.type.resolvedType;
    return sub;
  }

  public ValidationContext createSharedSub() {
    return createSub();
  }

  public ValidationContext createSub() {
    ValidationContext sub = newContext();
    sub.parent = this;
    sub.dataType = this.dataType;
    sub._property = this._property;
    sub.data = this.data;
    sub.expectedType = this.expectedType;
    sub.typeHolder = this.typeHolder;
    sub.objRange = this.objRange;
    sub.setUsageContext(this.getUsageContext());
    sub.className = this.className;
    sub.methodName = this.methodName;
    return sub;
  }

  public PropType typeOrObjectData(DataType exp) {
    return exp == null ? this.object() : exp.type(this);
  }

  public void addError(Range range, String message) {
    D3ELogger.error(message);
  }

  public void setExpectedType(PropType expectedType) {
    this.expectedType = expectedType;
  }

  public PropType getExpectedType() {
    return this.expectedType;
  }

  public PropType getTypeHolder() {
    return this.typeHolder;
  }

  public void setTypeHolder(PropType typeHolder) {
    this.typeHolder = typeHolder;
  }

  public void setProperty(FieldDecl property) {
    this._property = property;
    if (property != null) {
      this.expectedType = property.type.resolvedType;
    }
  }

  public Object getData() {
    return this.data;
  }

  public FieldDecl getProperty() {
    return this._property;
  }

  public void addUsedType(PropType type) {
    if (this.typeHolder == null) {
      return;
    }
    if (type instanceof TypeVariable) {
      PropType ext = type.extendsValue;
      if (ext != null) {
        addUsedType(ext);
      }
      return;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType pt = ((ParameterizedType) type);
      addUsedType(pt.baseType);
      pt.arguments.forEach(
          (a) -> {
            addUsedType(a.type);
          });
      return;
    }
    this.typeHolder.usedTypes.add(type);
  }

  public PropType getRawType(String type) {
    PropType propType = type == null ? null : this.genericRawType(type);
    if (propType != null) {
      return propType;
    }
    propType = StringExt.contains(type, ".", 0l) ? getType(type) : this.searchType(type);
    return propType == null ? this.object() : propType;
  }

  public PropType findRawType(PropType type) {
    if (type instanceof ParameterizedType) {
      return findRawType((((ParameterizedType) type)).baseType);
    }
    return type;
  }

  public PropType genericType(String type) {
    if (this.genericTypes != null) {
      PropType propType = this.genericTypes.get(type);
      if (propType != null) {
        return propType;
      }
    }
    if (this.parent != null) {
      return this.parent.genericType(type);
    }
    return null;
  }

  public PropType genericRawType(String type) {
    if (this.generics != null) {
      PropType propType = this.generics.resolveWithType(this, type);
      if (propType != null) {
        return propType;
      }
    }
    if (this.parent != null) {
      return this.parent.genericRawType(type);
    }
    return null;
  }

  public void addWarn(Range range, String message) {
    D3ELogger.info(message);
  }

  public String getUsageContext() {
    return this._usageContext;
  }

  public void setUsageContext(String ctx) {
    this._usageContext = ctx;
  }

  public PropType getDataType() {
    return this.dataType;
  }

  public String toString() {
    return this._valirables.toString();
  }

  public boolean hasErrors() {
    return false;
  }

  public ValidationContext newContext() {
    return new ValidationContext(this.registry);
  }

  public String toCreatableVarName(String name) {
    return StringExt.replaceAll(name.toLowerCase(), " ", "") + "s";
  }

  public ValidationContext sub() {
    return this.createSub();
  }

  public MethodDecl getLibraryMethod(String name) {
    TopDecl top = this.registry.get(name);
    if (top instanceof MethodDecl) {
      return ((MethodDecl) top);
    }
    return null;
  }

  public FieldDecl getLibraryField(String name) {
    TopDecl top = this.registry.get(name);
    if (top instanceof FieldDecl) {
      return ((FieldDecl) top);
    }
    return null;
  }

  public ValidationContext subWithGenerics(TypeParams generics) {
    ValidationContext sub = this.createSub();
    sub.generics = generics;
    sub.genericTypes = generics.resolve(this);
    return sub;
  }

  public ValidationContext subWithType(PropType expectedType) {
    ValidationContext sub = this.createSub();
    if (expectedType instanceof TypeVariable) {
      PropType gen = this.genericType(expectedType.name);
      if (gen != null) {
        expectedType = gen;
      }
    }
    sub.expectedType = expectedType;
    return sub;
  }

  public ValidationContext subWithDataAndType(Object data, PropType type) {
    ValidationContext sub = this.createSub();
    sub.data = data;
    sub.dataType = type;
    return sub;
  }

  public PropType futureOf(PropType type) {
    PropType futureType = this.registry.getType("Future");
    return futureType.wrap(ListExt.asList(type));
  }

  public PropType streamOf(PropType type) {
    PropType futureType = this.registry.getType("Stream");
    return futureType.wrap(ListExt.asList(type));
  }

  public List<String> getErrors() {
    /*
     TODO Auto-generated method stub
    */
    return null;
  }

  public ValidationContext subWithRange(Range objRange) {
    ValidationContext s = sub();
    s.objRange = objRange;
    return s;
  }

  public ValidationContext child(String field, String identity, long index) {
    ValidationContext s = sub();
    if (identity != null) {
      if (this.methodName == null || this.methodName.isEmpty()) {
        s.methodName = field + "_" + identity;
      } else {
        s.methodName += "_" + field + "_" + identity;
      }
    }
    return s;
  }

  public void addToCurrent(PropType type) {
    this.registry.addType(type);
  }

  public void addFieldUsege(PropType type, FieldDecl field) {
    /*
     if (field.usageType != null && field.usageName != null) {
         addAttributeUsegeInternal(field.usageType, field.usageName, null, field.name, usageContext);
     } else {
         addAttributeUsege(type, field.name);
     }
    */
  }

  public void addMethodUsege(PropType type, MethodDecl method) {
    /*
     addTypeUsageInternal(method.returnType);
     if (method.usageType != null && method.usageName != null) {
         addAttributeUsegeInternal(method.usageType, method.usageName, null, method.name, usageContext);
     } else {
     }
    */
  }

  public void addTypeUsage(PropType type) {
    addTypeUsageInternal(type);
  }

  public void addTypeUsageInternal(PropType type) {}

  public void validateType(DataType type) {
    if (type == null) {
      return;
    }
    ExpressionValidationUtil.validateDataType(type, this);
  }

  public void validateExpression(Expression exp) {
    if (exp == null) {
      return;
    }
    ExpressionValidationUtil.validate(exp, this);
  }
}
