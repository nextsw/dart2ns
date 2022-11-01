package helpers;

import models.VerificationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.VerificationDataRepository;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("VerificationData")
public class VerificationDataEntityHelper<T extends VerificationData> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private VerificationDataRepository verificationDataRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  public VerificationData newInstance() {
    return new VerificationData();
  }

  public void referenceFromValidations(T entity, EntityValidationContext validationContext) {}

  public void validateFieldMethod(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    String it = entity.getMethod();
    if (it == null) {
      validationContext.addFieldError("method", "method is required.");
      return;
    }
  }

  public void validateFieldContext(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    String it = entity.getContext();
    if (it == null) {
      validationContext.addFieldError("context", "context is required.");
      return;
    }
  }

  public void validateFieldBody(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    String it = entity.getBody();
    if (it == null) {
      validationContext.addFieldError("body", "body is required.");
      return;
    }
  }

  public void validateInternal(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    validateFieldMethod(entity, validationContext, onCreate, onUpdate);
    validateFieldContext(entity, validationContext, onCreate, onUpdate);
    validateFieldBody(entity, validationContext, onCreate, onUpdate);
  }

  public void validateOnCreate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, true, false);
  }

  public void validateOnUpdate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, false, true);
  }

  @Override
  public T clone(T entity) {
    return null;
  }

  @Override
  public T getById(long id) {
    return id == 0l ? null : ((T) verificationDataRepository.getOne(id));
  }

  @Override
  public void setDefaults(T entity) {}

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
