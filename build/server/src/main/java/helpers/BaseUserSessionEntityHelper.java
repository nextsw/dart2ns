package helpers;

import models.BaseUserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.BaseUserSessionRepository;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("BaseUserSession")
public class BaseUserSessionEntityHelper<T extends BaseUserSession> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private BaseUserSessionRepository baseUserSessionRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  public void referenceFromValidations(T entity, EntityValidationContext validationContext) {}

  public void validateFieldUserSessionId(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    String it = entity.getUserSessionId();
    if (it == null) {
      validationContext.addFieldError("userSessionId", "userSessionId is required.");
      return;
    }
  }

  public void validateInternal(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    validateFieldUserSessionId(entity, validationContext, onCreate, onUpdate);
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
    return id == 0l ? null : ((T) baseUserSessionRepository.getOne(id));
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
