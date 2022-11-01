package lists;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Query;

import d3e.core.D3ELogger;
import store.DatabaseObject;
import store.QueryImplUtil;

public abstract class AbsDataQueryImpl {

	protected void setIntegerParameter(Query query, String param, long value) {
		QueryImplUtil.setIntegerParameter(query, param, value);
	}

	protected void setIntegerListParameter(Query query, String param, List<Long> value) {
		QueryImplUtil.setIntegerListParameter(query, param, value);
	}

	protected void setEnumParameter(Query query, String param, Enum<?> value) {
		QueryImplUtil.setEnumParameter(query, param, value);
	}

	protected void setEnumListParameter(Query query, String param, List<? extends Enum<?>> value) {
		QueryImplUtil.setEnumListParameter(query, param, value);
	}

	protected void setDateParameter(Query query, String param, LocalDate value) {
		QueryImplUtil.setDateParameter(query, param, value);
	}

	protected void setDateListParameter(Query query, String param, List<LocalDate> value) {
		QueryImplUtil.setDateListParameter(query, param, value);
	}

	protected void setDateTimeParameter(Query query, String param, LocalDateTime value) {
		QueryImplUtil.setDateTimeParameter(query, param, value);
	}

	protected void setDateTimeListParameter(Query query, String param, List<LocalDateTime> value) {
		QueryImplUtil.setDateTimeListParameter(query, param, value);
	}

	protected void setStringParameter(Query query, String param, String value) {
		QueryImplUtil.setStringParameter(query, param, value);
	}

	protected void setStringListParameter(Query query, String param, List<String> value) {
		QueryImplUtil.setStringListParameter(query, param, value);
	}

	protected void setBooleanParameter(Query query, String param, boolean value) {
		QueryImplUtil.setBooleanParameter(query, param, value);
	}

	protected void setBooleanListParameter(Query query, String param, List<Boolean> value) {
		QueryImplUtil.setBooleanListParameter(query, param, value);
	}

	protected void setDatabaseObjectParameter(Query query, String param, DatabaseObject value) {
		QueryImplUtil.setDatabaseObjectParameter(query, param, value);
	}

	protected void setDatabaseObjectListParameter(Query query, String param, List<? extends DatabaseObject> value) {
		QueryImplUtil.setDatabaseObjectListParameter(query, param, value);
	}

	protected void setObjectListParameter(Query query, String param, List<? extends DatabaseObject> value) {
		QueryImplUtil.setObjectListParameter(query, param, value);
	}

	protected String like(String val) {
		if (val == null) {
			return "%%";
		} else {
			return "%" + val + "%";
		}
	}

	protected void assertLimitNotNegative(long limit) {
		if (limit < 0) {
			throw new RuntimeException("Limit is negative.");
		}
	}

	protected void logQuery(String sql, Query query) {
		D3ELogger.query(sql, query);
	}
	
	protected long getCountResult(Query query) {
		try {
			Object result = query.getSingleResult();
			return (long) result;
		} catch (RuntimeException e) {
			D3ELogger.printStackTrace(e);
			return 0;
		}
	}
}
