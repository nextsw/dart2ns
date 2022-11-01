package store;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import d3e.core.ListExt;

public class QueryImplUtil {

	public static void setIntegerParameter(Query query, String param, long value) {
		setParameter(query, param, value);
	}

	public static void setIntegerListParameter(Query query, String param, List<Long> values) {
		if (values.isEmpty()) {
			query.setParameter(param, ListExt.asList(0l));
		} else {
			query.setParameter(param, values);
		}
	}

	public static void setEnumParameter(Query query, String param, Enum<?> value) {
		setParameter(query, param, value);
	}

	public static void setEnumListParameter(Query query, String param, List<? extends Enum<?>> values) {
		if (values.isEmpty()) {
			query.setParameter(param, ListExt.asList(""));
		} else {
			List<String> names = values.stream().map(a -> a.name()).collect(Collectors.toList());
			query.setParameter(param, names);
		}
	}

	public static void setDateParameter(Query query, String param, LocalDate value) {
		setParameter(query, param,  value == null ? LocalDate.MIN: value);
	}

	public static void setDateListParameter(Query query, String param, List<LocalDate> values) {
		if (values.isEmpty()) {
			query.setParameter(param, ListExt.asList(""));
		} else {
			query.setParameter(param, values);
		}
	}

	public static void setDateTimeParameter(Query query, String param, LocalDateTime value) {
		setParameter(query, param, value);
	}

	public static void setDateTimeListParameter(Query query, String param, List<LocalDateTime> values) {
		if (values.isEmpty()) {
			query.setParameter(param, ListExt.asList(""));
		} else {
			query.setParameter(param, values);
		}
	}

	public static void setStringParameter(Query query, String param, String value) {
		setParameter(query, param, value);
	}

	public static void setStringListParameter(Query query, String param, List<String> values) {
		if (values.isEmpty()) {
			query.setParameter(param, ListExt.asList(""));
		} else {
			query.setParameter(param, values);
		}
	}

	public static void setBooleanParameter(Query query, String param, boolean value) {
		setParameter(query, param, value);
	}

	public static void setBooleanListParameter(Query query, String param, List<Boolean> values) {
		if (values.isEmpty()) {
			query.setParameter(param, ListExt.asList(false));
		} else {
			query.setParameter(param, values);
		}
	}

	public static void setDatabaseObjectParameter(Query query, String param, DatabaseObject value) {
		setParameter(query, param, value);
	}

	public static void setDatabaseObjectListParameter(Query query, String param,
			List<? extends DatabaseObject> values) {
		if (values.isEmpty()) {
			query.setParameter(param, ListExt.asList(0l));
		} else {
			List<Long> ids = values.stream().map(a -> a.getId()).collect(Collectors.toList());
			query.setParameter(param, ids);
		}
	}

	public static void setObjectListParameter(Query query, String param, List<? extends DatabaseObject> values) {
		if (values.isEmpty()) {
			query.setParameter(param, ListExt.asList(0l));
		} else {
			
			List<Long> ids = values.stream().map(a -> a.getId()).collect(Collectors.toList());
			query.setParameter(param, ids);
		}

	}

	public static void setParameter(Query query, String name, DatabaseObject value) {
		if (value == null) {
			query.setParameter(name, 0l);
		} else {
			query.setParameter(name, value.getId());
		}
	}

	public static void setParameter(Query query, String name, Enum<?> value) {
		if (value == null) {
			query.setParameter(name, "");
		} else {
			query.setParameter(name, value.name());
		}
	}

	public static void setParameter(Query query, String name, Object value) {
		if (value instanceof Duration) {
			value = ((Duration) value).getSeconds() * 1000;
		}
		query.setParameter(name, value);
	}

	public static void setParameter(Query query, String name, LocalDate value) {
		query.setParameter(name, value);
	}

	public static void setParameter(Query query, String name, LocalDateTime value) {
		query.setParameter(name, value);
	}

	public static void setParameter(Query query, String name, LocalTime value) {
		query.setParameter(name, value);
	}

	public static void setParameter(Query query, String name, String value) {
		if (value == null) {
			query.setParameter(name, "");
			return;
		}
		query.setParameter(name, value);
	}
}
