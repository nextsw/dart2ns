package repository.jpa;

import d3e.core.SchemaConstants;
import models.VerificationData;
import org.springframework.stereotype.Service;

@Service
public class VerificationDataRepository extends AbstractD3ERepository<VerificationData> {
  public int getTypeIndex() {
    return SchemaConstants.VerificationData;
  }
}
