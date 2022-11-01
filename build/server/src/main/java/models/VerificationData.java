package models;

import d3e.core.CloneContext;
import d3e.core.SchemaConstants;
import java.util.Objects;
import java.util.function.Consumer;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.SolrDocument;
import store.DBObject;
import store.DatabaseObject;
import store.ICloneable;

@SolrDocument(collection = "VerificationData")
@Entity
public class VerificationData extends CreatableObject {
  public static final int _METHOD = 0;
  public static final int _CONTEXT = 1;
  public static final int _TOKEN = 2;
  public static final int _SUBJECT = 3;
  public static final int _BODY = 4;
  public static final int _PROCESSED = 5;
  @Field @NotNull private String method;
  @Field @NotNull private String context;
  @Field private String token;
  @Field private String subject;
  @Field @NotNull private String body;
  @Field private boolean processed = false;

  public VerificationData() {
    super();
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.VerificationData;
  }

  @Override
  public String _type() {
    return "VerificationData";
  }

  @Override
  public int _fieldsCount() {
    return 6;
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public void visitChildren(Consumer<DBObject> visitor) {
    super.visitChildren(visitor);
  }

  public String getMethod() {
    _checkProxy();
    return this.method;
  }

  public void setMethod(String method) {
    _checkProxy();
    if (Objects.equals(this.method, method)) {
      return;
    }
    fieldChanged(_METHOD, this.method, method);
    this.method = method;
  }

  public String getContext() {
    _checkProxy();
    return this.context;
  }

  public void setContext(String context) {
    _checkProxy();
    if (Objects.equals(this.context, context)) {
      return;
    }
    fieldChanged(_CONTEXT, this.context, context);
    this.context = context;
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

  public String getSubject() {
    _checkProxy();
    return this.subject;
  }

  public void setSubject(String subject) {
    _checkProxy();
    if (Objects.equals(this.subject, subject)) {
      return;
    }
    fieldChanged(_SUBJECT, this.subject, subject);
    this.subject = subject;
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

  public boolean isProcessed() {
    _checkProxy();
    return this.processed;
  }

  public void setProcessed(boolean processed) {
    _checkProxy();
    if (Objects.equals(this.processed, processed)) {
      return;
    }
    fieldChanged(_PROCESSED, this.processed, processed);
    this.processed = processed;
  }

  public String displayName() {
    return "VerificationData";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof VerificationData && super.equals(a);
  }

  public VerificationData deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    VerificationData _obj = ((VerificationData) dbObj);
    _obj.setMethod(method);
    _obj.setContext(context);
    _obj.setToken(token);
    _obj.setSubject(subject);
    _obj.setBody(body);
    _obj.setProcessed(processed);
  }

  public VerificationData cloneInstance(VerificationData cloneObj) {
    if (cloneObj == null) {
      cloneObj = new VerificationData();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setMethod(this.getMethod());
    cloneObj.setContext(this.getContext());
    cloneObj.setToken(this.getToken());
    cloneObj.setSubject(this.getSubject());
    cloneObj.setBody(this.getBody());
    cloneObj.setProcessed(this.isProcessed());
    return cloneObj;
  }

  public VerificationData createNewInstance() {
    return new VerificationData();
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
