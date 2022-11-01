package d3e.core;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import store.D3EPrimaryCache;
import store.DatabaseObject;

public class RawResult {

	private CriteriaHelper helper;
	private Object[] row;
	private int i;

	public RawResult(CriteriaHelper helper, Object[] row) {
		this.row = row;
		this.helper = helper;
	}

	public Boolean readBoolean() {
		return (Boolean) row[i++];
	}

	public LocalDate readDate() {
		Object val = this.row[i++];
		if (val == null) {
			return null;
		}
		if (val instanceof LocalDate) {
			return (LocalDate) val;
		} else if (val instanceof Date) {
			return Instant.ofEpochMilli(((Date) val).getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
		}
		return ((Timestamp) val).toLocalDateTime().toLocalDate();
	}

	public LocalDateTime readDateTime() {
		Object val = this.row[i++];
		if (val == null) {
			return null;
		}
		if (val instanceof LocalDateTime) {
			return (LocalDateTime) val;
		}
		return ((Timestamp) val).toLocalDateTime();
	}

	public double readDouble() {
		Object val = this.row[i++];
		if (val == null) {
			return 0.0;
		}
		return (double) val;
	}

	public Duration readDuration() {
		long millis = (long) this.row[i++];
		return Duration.ofMillis(millis);
	}

	public String readString() {
		String str = (String) row[i++];
		if(str == null) {
			return "";
		}
		return str;
	}

	public long readInteger() {
		Object o = this.row[i++];
		if (o instanceof BigInteger) {
			return ((BigInteger) o).longValue();
		} else if (o instanceof Long) {
			return (long) o;
		}
		return 0l;
	}

	public Object readAny() {
		return row[i++];
	}

	public DatabaseObject reaObject() {
		if(this.row[i] == null) {
			i++;
			i++;
			return null;
		}
		long id = (long) this.row[i++];
		int refType = (int) this.row[i++];
		D3EPrimaryCache cache = (D3EPrimaryCache) helper.getProvider().get().getCache();
		return cache.getOrCreate(helper.getSchema().getType(refType), id);
	}

	public LocalTime readTime() {
		// TODO Auto-generated method stub
		return null;
	}

}
