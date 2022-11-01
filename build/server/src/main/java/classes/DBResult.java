package classes;

import d3e.core.SchemaConstants;
import java.util.List;
import store.D3EPersistanceList;
import store.DBObject;

public class DBResult extends DBObject {
  public static final int _STATUS = 0;
  public static final int _ERRORS = 1;
  private long id;
  private DBResultStatus status;
  private List<String> errors = new D3EPersistanceList<>(this, _ERRORS);

  public DBResult() {}

  public DBResult(List<String> errors, DBResultStatus status) {
    this.errors.addAll(errors);
    this.status = status;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public DBResultStatus getStatus() {
    return status;
  }

  public void setStatus(DBResultStatus status) {
    fieldChanged(_STATUS, this.status, status);
    this.status = status;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    ((D3EPersistanceList<String>) this.errors).setAll(errors);
  }

  public void addToErrors(String val, long index) {
    if (index == -1) {
      this.errors.add(val);
    } else {
      this.errors.add(((int) index), val);
    }
  }

  public void removeFromErrors(String val) {
    this.errors.remove(val);
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.DBResult;
  }

  @Override
  public String _type() {
    return "DBResult";
  }

  @Override
  public int _fieldsCount() {
    return 2;
  }

  public void _convertToObjectRef() {}
}
