package classes;

import d3e.core.SchemaConstants;
import java.util.List;
import lists.TypeAndId;
import models.VerificationData;
import store.D3EPersistanceList;
import store.DBObject;

public class VerificationDataByToken extends DBObject {
  public static final int _STATUS = 0;
  public static final int _ERRORS = 1;
  public static final int _ITEMS = 2;
  private long id;
  private DBResultStatus status;
  private List<String> errors = new D3EPersistanceList<>(this, _ERRORS);
  private List<VerificationData> items = new D3EPersistanceList<>(this, _ITEMS);
  private List<TypeAndId> itemsRef;

  public VerificationDataByToken() {}

  public VerificationDataByToken(
      List<String> errors, List<VerificationData> items, DBResultStatus status) {
    this.errors.addAll(errors);
    this.items.addAll(items);
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

  public List<VerificationData> getItems() {
    return items;
  }

  public List<TypeAndId> getItemsRef() {
    return itemsRef;
  }

  public void setItems(List<VerificationData> items) {
    ((D3EPersistanceList<VerificationData>) this.items).setAll(items);
  }

  public void setItemsRef(List<TypeAndId> itemsRef) {
    if (this.itemsRef != null) {
      ((D3EPersistanceList<TypeAndId>) this.itemsRef).setAll(itemsRef);
    } else {
      this.itemsRef = itemsRef;
    }
  }

  public void addToItems(VerificationData val, long index) {
    if (index == -1) {
      this.items.add(val);
    } else {
      this.items.add(((int) index), val);
    }
  }

  public void addToItemsRef(TypeAndId val, long index) {
    if (index == -1) {
      this.itemsRef.add(val);
    } else {
      this.itemsRef.add(((int) index), val);
    }
  }

  public void removeFromItems(VerificationData val) {
    this.items.remove(val);
  }

  public void removeFromItemsRef(TypeAndId val) {
    this.itemsRef.remove(val);
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.VerificationDataByToken;
  }

  @Override
  public String _type() {
    return "VerificationDataByToken";
  }

  @Override
  public int _fieldsCount() {
    return 3;
  }

  public void _convertToObjectRef() {
    this.itemsRef = TypeAndId.fromList(this.items, _ITEMS, this);
    this.items.clear();
  }
}
