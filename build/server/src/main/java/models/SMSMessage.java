package models;

import d3e.core.CloneContext;
import d3e.core.SchemaConstants;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.solr.client.solrj.beans.Field;
import store.D3EPersistanceList;
import store.DBObject;
import store.DatabaseObject;
import store.ICloneable;

public class SMSMessage extends CreatableObject {
  public static final int _FROM = 0;
  public static final int _TO = 1;
  public static final int _BODY = 2;
  public static final int _CREATEDON = 3;
  public static final int _DLTTEMPLATEID = 4;
  @Field private String from;
  @Field private List<String> to = D3EPersistanceList.primitive(this, _TO);
  @Field private String body;
  @Field private LocalDateTime createdOn;
  @Field private String dltTemplateId;

  public SMSMessage() {
    super();
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.SMSMessage;
  }

  @Override
  public String _type() {
    return "SMSMessage";
  }

  @Override
  public int _fieldsCount() {
    return 5;
  }

  public void addToTo(String val, long index) {
    if (index == -1) {
      this.to.add(val);
    } else {
      this.to.add(((int) index), val);
    }
  }

  public void removeFromTo(String val) {
    this.to.remove(val);
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public void visitChildren(Consumer<DBObject> visitor) {
    super.visitChildren(visitor);
  }

  public String getFrom() {
    _checkProxy();
    return this.from;
  }

  public void setFrom(String from) {
    _checkProxy();
    if (Objects.equals(this.from, from)) {
      return;
    }
    fieldChanged(_FROM, this.from, from);
    this.from = from;
  }

  public List<String> getTo() {
    return this.to;
  }

  public void setTo(List<String> to) {
    if (Objects.equals(this.to, to)) {
      return;
    }
    ((D3EPersistanceList<String>) this.to).setAll(to);
  }

  public String getBody() {
    _checkProxy();
    return this.body;
  }

  public void setBody(String body) {
    _checkProxy();
    if (Objects.equals(this.body, body)) {
      return;
    }
    fieldChanged(_BODY, this.body, body);
    this.body = body;
  }

  public LocalDateTime getCreatedOn() {
    _checkProxy();
    return this.createdOn;
  }

  public void setCreatedOn(LocalDateTime createdOn) {
    _checkProxy();
    if (Objects.equals(this.createdOn, createdOn)) {
      return;
    }
    fieldChanged(_CREATEDON, this.createdOn, createdOn);
    this.createdOn = createdOn;
  }

  public String getDltTemplateId() {
    _checkProxy();
    return this.dltTemplateId;
  }

  public void setDltTemplateId(String dltTemplateId) {
    _checkProxy();
    if (Objects.equals(this.dltTemplateId, dltTemplateId)) {
      return;
    }
    fieldChanged(_DLTTEMPLATEID, this.dltTemplateId, dltTemplateId);
    this.dltTemplateId = dltTemplateId;
  }

  public String displayName() {
    return "SMSMessage";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof SMSMessage && super.equals(a);
  }

  public SMSMessage deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    SMSMessage _obj = ((SMSMessage) dbObj);
    _obj.setFrom(from);
    _obj.setTo(to);
    _obj.setBody(body);
    _obj.setCreatedOn(createdOn);
    _obj.setDltTemplateId(dltTemplateId);
  }

  public SMSMessage cloneInstance(SMSMessage cloneObj) {
    if (cloneObj == null) {
      cloneObj = new SMSMessage();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setFrom(this.getFrom());
    cloneObj.setTo(new ArrayList<>(this.getTo()));
    cloneObj.setBody(this.getBody());
    cloneObj.setCreatedOn(this.getCreatedOn());
    cloneObj.setDltTemplateId(this.getDltTemplateId());
    return cloneObj;
  }

  public boolean transientModel() {
    return true;
  }

  public SMSMessage createNewInstance() {
    return new SMSMessage();
  }
}
