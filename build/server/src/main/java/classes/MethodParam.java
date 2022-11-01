package classes;

import d3e.core.ListExt;
import java.util.List;

public class MethodParam {
  public List<Comment> beforeComments = ListExt.asList();
  public String thisToken;
  public Expression defaultValue;
  public boolean required = false;
  public boolean deprecated = false;
  public DataType dataType;
  public String name;
  public List<Annotation> annotations = ListExt.asList();
  public MethodParams fParams;

  public MethodParam(
      List<Annotation> annotations,
      DataType dataType,
      Expression defaultValue,
      boolean deprecated,
      String name,
      boolean required,
      String thisToken,
      MethodParams fParams) {
    this.annotations = annotations;
    this.dataType = dataType;
    this.defaultValue = defaultValue;
    this.deprecated = deprecated;
    this.name = name;
    this.required = required;
    this.thisToken = thisToken;
    this.fParams = fParams;
  }
}
