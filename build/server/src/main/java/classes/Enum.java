package classes;

import d3e.core.ListExt;
import java.util.List;

public class Enum extends TopDecl {
  public List<String> values = ListExt.asList();
  public ClassDecl cls;

  public Enum(String name) {
    super(name, TopDeclType.Enum, "");
  }

  public void collectUsedTypes() {}

  public void resolve(ResolveContext context) {
    if (this.cls == null) {
      this.cls = toClassDecl();
    }
  }

  public ClassDecl toClassDecl() {
    this.cls = new ClassDecl(false, this.name);
    for (String v : this.values) {
      FieldDecl field =
          new FieldDecl(
              ListExt.List(),
              ListExt.List(),
              false,
              false,
              false,
              v,
              true,
              new ValueType(this.name, false),
              null);
      this.cls.members.add(field);
    }
    ValueType valuesType = new ValueType("List", false);
    valuesType.args.add(new ValueType(this.name, false));
    FieldDecl valuesField =
        new FieldDecl(
            ListExt.List(), ListExt.List(), false, false, false, "values", true, valuesType, null);
    this.cls.members.add(valuesField);
    return this.cls;
  }

  public void simplify(Simplifier s) {}
}
