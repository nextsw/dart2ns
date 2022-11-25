package classes;

import java.util.List;

public abstract class ResolveContext {
  public DataType objectType = new ValueType("Object", false);
  public DataType booleanType = new ValueType("bool", false);
  public DataType integerType = new ValueType("int", false);
  public DataType doubleType = new ValueType("double", false);
  public DataType nullType = new ValueType("null", false);
  public DataType stringType = new ValueType("String", false);
  public DataType typeType = new ValueType("__Type", false);
  public DataType libraryType = new ValueType("__Library", false);
  public DataType statementType = new ValueType("__Statement", false);
  public DataType dynamicType = new ValueType("dynamic", false);
  public ClassDecl instanceClass;
  public Library currentLib;
  public MethodDecl method;
  public Scope scope;
  public DataType expectedType;

  public abstract TopDecl get(String name);

  public abstract DataType fieldTypeFromScope(String name);

  public abstract DataType resolveType(ClassDecl onType, ClassDecl from, DataType type);

  public abstract ClassMember getMember(
      ClassDecl onType, String name, MemberFilter filter, boolean noSuperCons, DataType thisType);

  public abstract DataType commonType(DataType first, DataType second);

  public abstract DataType subType(DataType type, long index);

  public abstract List<MethodParam> sortMethodParams(MethodParams params);

  public abstract DataType getListValueType(ClassDecl cls);

  public abstract DataType ofUnknownType();

  public abstract void error(String msg);
}
