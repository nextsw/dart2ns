package repository.jpa;

import d3e.core.SchemaConstants;
import models.BaseUserSession;
import org.springframework.stereotype.Service;

@Service
public class BaseUserSessionRepository extends AbstractD3ERepository<BaseUserSession> {
  public int getTypeIndex() {
    return SchemaConstants.BaseUserSession;
  }
}
