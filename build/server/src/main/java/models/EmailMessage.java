package models;

import d3e.core.CloneContext;
import d3e.core.DFile;
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

public class EmailMessage extends CreatableObject {
  public static final int _FROM = 0;
  public static final int _TO = 1;
  public static final int _BODY = 2;
  public static final int _CREATEDON = 3;
  public static final int _BCC = 4;
  public static final int _CC = 5;
  public static final int _SUBJECT = 6;
  public static final int _HTML = 7;
  public static final int _INLINEATTACHMENTS = 8;
  public static final int _ATTACHMENTS = 9;
  @Field private String from;
  @Field private List<String> to = D3EPersistanceList.primitive(this, _TO);
  @Field private String body;
  @Field private LocalDateTime createdOn;
  @Field private List<String> bcc = D3EPersistanceList.primitive(this, _BCC);
  @Field private List<String> cc = D3EPersistanceList.primitive(this, _CC);
  @Field private String subject;
  @Field private boolean html = false;

  @Field
  private List<DFile> inlineAttachments = D3EPersistanceList.primitive(this, _INLINEATTACHMENTS);

  @Field private List<DFile> attachments = D3EPersistanceList.primitive(this, _ATTACHMENTS);

  public EmailMessage() {
    super();
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.EmailMessage;
  }

  @Override
  public String _type() {
    return "EmailMessage";
  }

  @Override
  public int _fieldsCount() {
    return 10;
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

  public void addToBcc(String val, long index) {
    if (index == -1) {
      this.bcc.add(val);
    } else {
      this.bcc.add(((int) index), val);
    }
  }

  public void removeFromBcc(String val) {
    this.bcc.remove(val);
  }

  public void addToCc(String val, long index) {
    if (index == -1) {
      this.cc.add(val);
    } else {
      this.cc.add(((int) index), val);
    }
  }

  public void removeFromCc(String val) {
    this.cc.remove(val);
  }

  public void addToInlineAttachments(DFile val, long index) {
    if (index == -1) {
      this.inlineAttachments.add(val);
    } else {
      this.inlineAttachments.add(((int) index), val);
    }
  }

  public void removeFromInlineAttachments(DFile val) {
    this.inlineAttachments.remove(val);
  }

  public void addToAttachments(DFile val, long index) {
    if (index == -1) {
      this.attachments.add(val);
    } else {
      this.attachments.add(((int) index), val);
    }
  }

  public void removeFromAttachments(DFile val) {
    this.attachments.remove(val);
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

  public List<String> getBcc() {
    return this.bcc;
  }

  public void setBcc(List<String> bcc) {
    if (Objects.equals(this.bcc, bcc)) {
      return;
    }
    ((D3EPersistanceList<String>) this.bcc).setAll(bcc);
  }

  public List<String> getCc() {
    return this.cc;
  }

  public void setCc(List<String> cc) {
    if (Objects.equals(this.cc, cc)) {
      return;
    }
    ((D3EPersistanceList<String>) this.cc).setAll(cc);
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

  public boolean isHtml() {
    _checkProxy();
    return this.html;
  }

  public void setHtml(boolean html) {
    _checkProxy();
    if (Objects.equals(this.html, html)) {
      return;
    }
    fieldChanged(_HTML, this.html, html);
    this.html = html;
  }

  public List<DFile> getInlineAttachments() {
    return this.inlineAttachments;
  }

  public void setInlineAttachments(List<DFile> inlineAttachments) {
    if (Objects.equals(this.inlineAttachments, inlineAttachments)) {
      return;
    }
    ((D3EPersistanceList<DFile>) this.inlineAttachments).setAll(inlineAttachments);
  }

  public List<DFile> getAttachments() {
    return this.attachments;
  }

  public void setAttachments(List<DFile> attachments) {
    if (Objects.equals(this.attachments, attachments)) {
      return;
    }
    ((D3EPersistanceList<DFile>) this.attachments).setAll(attachments);
  }

  public String displayName() {
    return "EmailMessage";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof EmailMessage && super.equals(a);
  }

  public EmailMessage deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    EmailMessage _obj = ((EmailMessage) dbObj);
    _obj.setFrom(from);
    _obj.setTo(to);
    _obj.setBody(body);
    _obj.setCreatedOn(createdOn);
    _obj.setBcc(bcc);
    _obj.setCc(cc);
    _obj.setSubject(subject);
    _obj.setHtml(html);
    _obj.setInlineAttachments(inlineAttachments);
    _obj.setAttachments(attachments);
  }

  public EmailMessage cloneInstance(EmailMessage cloneObj) {
    if (cloneObj == null) {
      cloneObj = new EmailMessage();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setFrom(this.getFrom());
    cloneObj.setTo(new ArrayList<>(this.getTo()));
    cloneObj.setBody(this.getBody());
    cloneObj.setCreatedOn(this.getCreatedOn());
    cloneObj.setBcc(new ArrayList<>(this.getBcc()));
    cloneObj.setCc(new ArrayList<>(this.getCc()));
    cloneObj.setSubject(this.getSubject());
    cloneObj.setHtml(this.isHtml());
    cloneObj.setInlineAttachments(new ArrayList<>(this.getInlineAttachments()));
    cloneObj.setAttachments(new ArrayList<>(this.getAttachments()));
    return cloneObj;
  }

  public boolean transientModel() {
    return true;
  }

  public EmailMessage createNewInstance() {
    return new EmailMessage();
  }

  public void collectCreatableReferences(List<Object> _refs) {
    super.collectCreatableReferences(_refs);
    _refs.addAll(this.inlineAttachments);
    _refs.addAll(this.attachments);
  }
}
