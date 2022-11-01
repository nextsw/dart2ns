package lists;

import classes.VerificationDataByToken;
import classes.VerificationDataByTokenIn;
import d3e.core.SchemaConstants;
import gqltosql.GqlToSql;
import gqltosql.SqlRow;
import gqltosql2.OutObject;
import gqltosql2.OutObjectList;
import graphql.language.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import models.VerificationData;
import models.VerificationDataByTokenRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.AbstractQueryService;
import rest.ws.RocketQuery;
import store.D3EEntityManagerProvider;

@Service
public class VerificationDataByTokenImpl extends AbsDataQueryImpl {
  @Autowired private D3EEntityManagerProvider em;
  @Autowired private GqlToSql gqlToSql;
  @Autowired private gqltosql2.GqlToSql gqlToSql2;

  public VerificationDataByTokenRequest inputToRequest(VerificationDataByTokenIn inputs) {
    VerificationDataByTokenRequest request = new VerificationDataByTokenRequest();
    request.setToken(inputs.token);
    return request;
  }

  public VerificationDataByToken get(VerificationDataByTokenIn inputs) {
    VerificationDataByTokenRequest request = inputToRequest(inputs);
    return get(request);
  }

  public VerificationDataByToken get(VerificationDataByTokenRequest request) {
    List<NativeObj> rows = getNativeResult(request);
    return getAsStruct(rows);
  }

  public VerificationDataByToken getAsStruct(List<NativeObj> rows) {
    List<VerificationData> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em.get(), _r1.getRef(0), SchemaConstants.VerificationData));
    }
    VerificationDataByToken wrap = new VerificationDataByToken();
    wrap.setItems(result);
    return wrap;
  }

  public JSONObject getAsJson(Field field, VerificationDataByTokenIn inputs) throws Exception {
    VerificationDataByTokenRequest request = inputToRequest(inputs);
    return getAsJson(field, request);
  }

  public JSONObject getAsJson(Field field, VerificationDataByTokenRequest request)
      throws Exception {
    List<NativeObj> rows = getNativeResult(request);
    return getAsJson(field, rows);
  }

  public JSONObject getAsJson(Field field, List<NativeObj> rows) throws Exception {
    JSONArray array = new JSONArray();
    List<SqlRow> sqlDecl0 = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      array.put(NativeSqlUtil.getJSONObject(_r1, sqlDecl0));
    }
    gqlToSql.execute("VerificationData", AbstractQueryService.inspect(field, ""), sqlDecl0);
    JSONObject result = new JSONObject();
    result.put("items", array);
    return result;
  }

  public OutObject getAsJson(gqltosql2.Field field, VerificationDataByTokenRequest request)
      throws Exception {
    List<NativeObj> rows = getNativeResult(request);
    return getAsJson(field, rows);
  }

  public OutObject getAsJson(gqltosql2.Field field, List<NativeObj> rows) throws Exception {
    OutObjectList array = new OutObjectList();
    OutObjectList sqlDecl0 = new OutObjectList();
    for (NativeObj _r1 : rows) {
      array.add(NativeSqlUtil.getOutObject(_r1, SchemaConstants.VerificationData, sqlDecl0));
    }
    gqlToSql2.execute("VerificationData", RocketQuery.inspect2(field, ""), sqlDecl0);
    OutObject result = new OutObject();
    result.addType(SchemaConstants.VerificationDataByToken);
    result.add("items", array);
    return result;
  }

  public List<NativeObj> getNativeResult(VerificationDataByTokenRequest request) {
    String sql = "select a._id a0 from _verification_data a where a._token = :param_0";
    Query query = em.get().createNativeQuery(sql);
    setStringParameter(query, "param_0", request.getToken());
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 0);
    return result;
  }
}
