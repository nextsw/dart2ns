package repository.jpa;

import d3e.core.SchemaConstants;
import models.BaseUser;
import org.springframework.stereotype.Service;

@Service
public class BaseUserRepository extends AbstractD3ERepository<BaseUser> {
  public int getTypeIndex() {
    return SchemaConstants.BaseUser;
  }
}
