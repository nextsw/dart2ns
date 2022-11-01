package rest.ws;

import d3e.core.QueryProvider;
import d3e.core.SchemaConstants;
import models.VerificationDataByTokenRequest;
import store.DBObject;

public class APIDataQueryUtil {
  public static DBObject get(int type, Object input) {
    switch (type) {
      case SchemaConstants.VerificationDataByToken:
        {
          return QueryProvider.get()
              .getVerificationDataByToken(((VerificationDataByTokenRequest) input));
        }
    }
    return null;
  }
}
