package classes;

import d3e.core.ListExt;
import java.util.List;

public class MethodDecl extends ClassMember {
  public String name;
  public List<Annotation> annotations = ListExt.asList();
  public MethodParams params;
  public DataType returnType;
  public boolean finalValue = false;
  public boolean staticValue = false;
  public boolean constValue = false;
  public boolean setter = false;
  public boolean getter = false;
  public boolean factory = false;
  public Expression init;
  public String factoryName;
  public TypeParams generics;
  public Block body;
  public ASyncType asyncType;
  public String content;

  public MethodDecl(
      List<Annotation> annotations,
      ASyncType asyncType,
      Block body,
      boolean constValue,
      boolean factory,
      String factoryName,
      boolean finalValue,
      TypeParams generics,
      boolean getter,
      Expression init,
      String name,
      MethodParams params,
      DataType returnType,
      boolean setter,
      boolean staticValue) {
    this.annotations = annotations;
    this.asyncType = asyncType;
    this.body = body;
    this.constValue = constValue;
    this.factory = factory;
    this.factoryName = factoryName;
    this.finalValue = finalValue;
    this.generics = generics;
    this.getter = getter;
    this.init = init;
    this.name = name;
    this.params = params;
    this.returnType = returnType;
    this.setter = setter;
    this.staticValue = staticValue;
  }
}
