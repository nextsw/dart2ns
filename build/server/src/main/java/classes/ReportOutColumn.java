package classes;

import d3e.core.SchemaConstants;
import java.util.List;
import store.D3EPersistanceList;
import store.DBObject;

public class ReportOutColumn extends DBObject {
  public static final int _TYPE = 0;
  public static final int _VALUE = 1;
  public static final int _ATTRIBUTES = 2;
  private long id;
  private String type;
  private String value;
  private List<ReportOutAttribute> attributes = new D3EPersistanceList<>(this, _ATTRIBUTES);

  public ReportOutColumn() {}

  public ReportOutColumn(List<ReportOutAttribute> attributes, String type, String value) {
    this.attributes.addAll(attributes);
    this.type = type;
    this.value = value;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    fieldChanged(_TYPE, this.type, type);
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    fieldChanged(_VALUE, this.value, value);
    this.value = value;
  }

  public List<ReportOutAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<ReportOutAttribute> attributes) {
    ((D3EPersistanceList<ReportOutAttribute>) this.attributes).setAll(attributes);
  }

  public void addToAttributes(ReportOutAttribute val, long index) {
    if (index == -1) {
      this.attributes.add(val);
    } else {
      this.attributes.add(((int) index), val);
    }
  }

  public void removeFromAttributes(ReportOutAttribute val) {
    this.attributes.remove(val);
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.ReportOutColumn;
  }

  @Override
  public String _type() {
    return "ReportOutColumn";
  }

  @Override
  public int _fieldsCount() {
    return 3;
  }

  public void _convertToObjectRef() {
    this.attributes.forEach((a) -> a._convertToObjectRef());
  }
}
