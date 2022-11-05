package classes;

import d3e.core.ListExt;
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

  public ClassDecl(boolean isMixin, String name) {
    super(name, TopDeclType.Class, "");
    this.isMixin = isMixin;
  }
}
