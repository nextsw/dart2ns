package models;

import java.time.LocalDateTime;

import d3e.core.CloneContext;
import store.DBSaveStatus;
import store.DatabaseObject;
import store.ICloneable;

public abstract class CreatableObject extends DatabaseObject {

  public DBSaveStatus getSaveStatus() {
    return this.saveStatus;
  }

  public void setSaveStatus(DBSaveStatus ss) {
    this.saveStatus = ss;
  }

  @Override
  public void deepCloneIntoObj(ICloneable cloned, CloneContext ctx) {
	super.deepCloneIntoObj(cloned, ctx);
	CreatableObject cloneObj = (CreatableObject) cloned;
    cloneObj.setSaveStatus(this.getSaveStatus());
  }
  
  public void cloneInstance(CreatableObject cloneObj) {
    super.cloneInstance(cloneObj);
    cloneObj.setSaveStatus(this.getSaveStatus());
  }

  public CreatableObject getOld() {
    return null;
  }

  public void setOld(DatabaseObject old) {
  }

  public void recordOld(CloneContext ctx) {
    this.setOld(ctx.getFromCache(this));
  }
  
  @Override
  public boolean _creatable() {
	return true;
  }
}
