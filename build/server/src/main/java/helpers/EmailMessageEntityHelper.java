package helpers;

import java.time.LocalDateTime;
import models.EmailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.DFileRepository;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("EmailMessage")
public class EmailMessageEntityHelper<T extends EmailMessage> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private DFileRepository dFileRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  public EmailMessage newInstance() {
    return new EmailMessage();
  }

  public void referenceFromValidations(T entity, EntityValidationContext validationContext) {}

  public void validateInternal(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {}

  public void validateOnCreate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, true, false);
  }

  public void validateOnUpdate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, false, true);
  }

  public void setDefaultCreatedOn(T entity) {
    if (entity.getCreatedOn() != null) {
      return;
    }
    entity.setCreatedOn(LocalDateTime.now());
  }

  @Override
  public T clone(T entity) {
    return null;
  }

  @Override
  public T getById(long id) {
    return null;
  }

  @Override
  public void setDefaults(T entity) {
    this.setDefaultCreatedOn(entity);
  }

  @Override
  public void compute(T entity) {}

  public Boolean onDelete(T entity, boolean internal, EntityValidationContext deletionContext) {
    return true;
  }

  @Override
  public Boolean onCreate(T entity, boolean internal) {
    return true;
  }

  @Override
  public Boolean onUpdate(T entity, boolean internal) {
    return true;
  }

  public T getOld(long id) {
    return ((T) getById(id).clone());
  }
}
