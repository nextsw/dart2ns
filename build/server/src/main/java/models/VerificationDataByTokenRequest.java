package models;

import d3e.core.CloneContext;
import d3e.core.SchemaConstants;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.solr.client.solrj.beans.Field;
import store.DBObject;
import store.DatabaseObject;
import store.ICloneable;

public class VerificationDataByTokenRequest extends CreatableObject {
  public static final int _TOKEN = 0;
  @Field private String token;

  public VerificationDataByTokenRequest() {
    super();
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.VerificationDataByTokenRequest;
  }

  @Override
  public String _type() {
    return "VerificationDataByTokenRequest";
  }

  @Override
  public int _fieldsCount() {
    return 1;
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public void visitChildren(Consumer<DBObject> visitor) {
    super.visitChildren(visitor);
  }

  public String getToken() {
    _checkProxy();
    return this.token;
  }

  public void setToken(String token) {
    _checkProxy();
    if (Objects.equals(this.token, token)) {
      return;
    }
    fieldChanged(_TOKEN, this.token, token);
    this.token = token;
  }

  public String displayName() {
    return "VerificationDataByTokenRequest";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof VerificationDataByTokenRequest && super.equals(a);
  }

  public VerificationDataByTokenRequest deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    VerificationDataByTokenRequest _obj = ((VerificationDataByTokenRequest) dbObj);
    _obj.setToken(token);
  }

  public VerificationDataByTokenRequest cloneInstance(VerificationDataByTokenRequest cloneObj) {
    if (cloneObj == null) {
      cloneObj = new VerificationDataByTokenRequest();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setToken(this.getToken());
    return cloneObj;
  }

  public boolean transientModel() {
    return true;
  }

  public VerificationDataByTokenRequest createNewInstance() {
    return new VerificationDataByTokenRequest();
  }
}
