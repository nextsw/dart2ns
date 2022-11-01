package d3e.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gqltosql.schema.DField;
import gqltosql.schema.DModel;
import gqltosql.schema.FieldType;
import gqltosql.schema.IModelSchema;
import store.DBObject;
import store.EntityHelper;
import store.EntityHelperService;

@Service
public class D3EJsonContext {
	@Autowired private IModelSchema schema;
	@Autowired private EntityHelperService helperService;
	private Map<Long, DBObject> localCache = new HashMap<>();
	
	/**
	 * Parses a single object from JSON
	 * @param <T>
	 * @param jsonString
	 * @param typeString
	 * @return
	 */
	public <T extends DBObject> T parse(String jsonString, String typeString) {
		if (jsonString == null || typeString == null) {
			return null;
		}
		
		localCache.clear();
		JSONObject json = new JSONObject(jsonString);
		DModel<?> model = schema.getType(typeString);
		
		return readObject(json, model);
	}
	
	/**
	 * Parses a collection from JSON
	 * @param <T>
	 * @param jsonString
	 * @param typeString
	 * @return
	 */
	public <T extends DBObject> List<T> parseColl(String jsonString, String typeString) {
		if (jsonString == null || typeString == null) {
			return null;
		}
		
		localCache.clear();
		JSONArray json = new JSONArray(jsonString);
		DModel<?> model = schema.getType(typeString);
		
		List<T> result = new ArrayList<>();
		int size = json.length();
		
		for (int i = 0; i < size; i++) {
			result.add(readObject(json.getJSONObject(i), model));
		}
		return result;
	}
	
	/**
	 * Reads a single object from Json, but reads it into the given object instead of creating a new one like parse
	 * @param <T>
	 * @param obj
	 * @param jsonString
	 * @param typeString
	 * @return
	 */
	public <T extends DBObject> T parseAndUpdate(T obj, String jsonString, String typeString) {
		if (jsonString == null || typeString == null) {
			return null;
		}
		
		localCache.clear();
		JSONObject json = new JSONObject(jsonString);
		DModel<?> model = schema.getType(typeString);
		
		readObjectProperties(obj, json, model);
		
		return obj;
	}
	
	private <T extends DBObject> T readObject(JSONObject json, DModel<?> type) {
		// Called from multiple places, so separate method needed
		return readObject(json, type, false);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends DBObject> T readObject(JSONObject json, DModel<?> type, boolean child) {
		Object obj;
		// read the object from id
		obj = readRef(json, type, child);
		
		readObjectProperties(obj, json, type);
		return (T) obj;
	}
	
	@SuppressWarnings("rawtypes")
	private Object readRef(JSONObject json, DModel type) {
		// Called from multiple places, so separate method needed
		return readRef(json, type, false);
	}

	@SuppressWarnings("rawtypes")
	private Object readRef(JSONObject json, DModel type, boolean child) {
		Object obj;
		long id = 0;
		if (json.has("id")) {
			id = json.getLong("id");
		} else if (!child) {
			// id may not be provided in child, but is required in ref
			throw new RuntimeException("Expected id in JSON");
		}
		if (id <= 0) {
			// TODO
			obj = localCache.get(id);
			if (obj == null) {
				obj = type.newInstance();
				if (obj instanceof DBObject) {
					DBObject dbobj = (DBObject) obj;
					if (id < 0) {
						localCache.put(id, dbobj);
					}
					dbobj.setLocalId(id);
				}
			}
		} else {
			EntityHelper<?> entity = helperService.get(type.getType());
			obj = entity.getById(id);
		}
		return obj;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void readObjectProperties(Object obj, JSONObject json, DModel<?> type) {
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			DField df = type.getField(key);
			if (df == null || !df.canReceive()) {
				continue;
			}
			
			FieldType ft = df.getType();
			switch (ft) {
			case Primitive:
				readPrimitive(df, obj, key, json);
				break;
			case PrimitiveCollection:
				readPrimitiveCollection(df, obj, key, json);
				break;
			case Reference:
				if (df.getReference().isEmbedded()) {
					readEmbedded(df.getValue(obj), key, json, df.getReference());
				} else if (df.getReference().getType().equals("DFile")) {
					JSONObject dfileJson = json.getJSONObject(key);
					df.setValue(obj, readDFile(dfileJson));
				} else if (df.isChild()) {
					// Child
					JSONObject childJson = json.getJSONObject(key);
					df.setValue(obj, readObject(childJson, df.getReference(), true));
				} else {
					// Reference
					JSONObject refJson = json.getJSONObject(key);
					df.setValue(obj, readRef(refJson, df.getReference()));
				}
				break;
			case ReferenceCollection:
				JSONArray value = json.getJSONArray(key);
				List colls = readReferenceCollection((List) df.getValue(obj), value, df.getReference());
				df.setValue(obj, colls);
				break;
			case InverseCollection:
				throw new RuntimeException("Can not read InverseCollection: " + df.getName());
			default:
				break;
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void readPrimitive(DField df, Object _this, String key, JSONObject json) {
		Object converted;
		switch (df.getPrimitiveType()) {
		case Boolean:
			converted = json.getBoolean(key);
			break;
		case DFile: {
			JSONObject dfileObj = json.getJSONObject(key);
			converted = readDFile(dfileObj);
			break;
		}
		case Date: {
			JSONObject dateObj = json.getJSONObject(key);
			converted = readDate(dateObj);
			break;
		}
		case DateTime: {
			long second = json.getLong(key);
			converted = readDateTime(second);
			break;
		}
		case Double:
			converted = json.getDouble(key);
			break;
		case Duration:
			converted = null;
			break;
		case Enum:
			int field = json.getInt(key);
			converted = readEnum(df.getEnumType(), field);
			break;
		case Integer:
			converted = json.getLong(key);
			break;
		case String:
			converted = json.getString(key);
			break;
		case Time: {
			long second = json.getLong(key);
			converted = readTime(second);
			break;
		}
		default:
			throw new RuntimeException("Unsupported type. " + df.getPrimitiveType());
		}
		df.setValue(_this, converted);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List readReferenceCollection(List old, JSONArray arr, DModel type) {
		// TODO
		int size = arr.length();
		// D3ELogger.info("r coll: " + size);
		
		List result = new ArrayList();
		for (int i = 0; i < size; i++) {
			Object obj = readRef(arr.getJSONObject(i), type);
			result.add(obj);
		}
		return result;
		
//		if (size < 0) {
//			old = new ArrayList<>(old);
//			size = -size;
//			for (int i = 0; i < size; i++) {
//				int idx = msg.readInt();
//				if (idx < 0) {
//					idx = -idx;
//					idx--;
//					old.remove(idx);
//				} else {
//					idx--;
//					Object obj = readObject(arr.getJSONObject(i), type);
//					if (idx >= old.size()) {
//						old.add(obj);
//					} else {
//						old.add(idx, obj);
//					}
//				}
//			}
//			return old;
//		} else {
//			List colls = new ArrayList<>();
//			for (int i = 0; i < size; i++) {
//				colls.add(readObject(arr.getJSONObject(i), type));
//			}
//			return colls;
//		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void readPrimitiveCollection(DField df, Object _this, String key, JSONObject json) {
		JSONArray coll = json.getJSONArray(key);
		List result = new ArrayList();
		int size = coll.length();
		
		for (int i = 0; i < size; i++) {
			switch (df.getPrimitiveType()) {
			case Boolean:
				result.add(coll.getBoolean(i));
				break;
			case Double:
				result.add(coll.getDouble(i));
				break;
			case Integer:
				result.add(coll.getLong(i));
				break;
			case String:
				result.add(coll.getString(i));
				break;
			case Enum:
				// TODO
				result.add(readEnum(df.getEnumType(), coll.getInt(i)));
				break;
			case DFile:
				result.add(readDFile(coll.getJSONObject(i)));
				break;
			case Date:
				result.add(readDate(coll.getJSONObject(i)));
				break;
			case DateTime:
				result.add(readDateTime(coll.getLong(i)));
				break;
			case Duration:
				break;
			case Time:
				result.add(readTime(coll.getLong(i)));
				break;
			default:
				break;
			}
		}
		
		df.setValue(_this, result);
	}
	
	private LocalDateTime readDateTime(long second) {
		if (second == -1) {
			return null;
		}
		return LocalDateTime.ofEpochSecond(second, 0, ZoneOffset.UTC);
	}
	
	private Object readTime(long second) {
		if (second == -1) {
			return null;
		}
		return LocalDateTime.ofEpochSecond(second, 0, ZoneOffset.UTC).toLocalTime();
	}
	
	private Object readDate(JSONObject json) {
		int year = json.getInt("year");
		if (year == -1) {
			return null;
		}
		int month = json.getInt("month");
		int dayOfMonth = json.getInt("dayOfMonth");
		return LocalDate.of(year, month, dayOfMonth);
	}
	
	private Object readEnum(int enumType, int field) {
		DModel<?> et = schema.getType(enumType);
		DField<?, ?>[] fields = et.getFields();
		return fields[field].getValue(null);
	}
	
	private Object readDFile(JSONObject json) {
		String str = json.getString("id");
		if (str == null) {
			return null;
		}
		DFile file = new DFile();
		file.setId(str);
		file.setName(json.getString("name"));
		file.setSize(json.getLong("size"));
		file.setMimeType(json.getString("mimeType"));
		return file;
	}
	
	@SuppressWarnings("rawtypes")
	private void readEmbedded(Object obj, String key, JSONObject parent, DModel type) {
		if (type == null) {
			// D3ELogger.info("r emb: null");
			return;
		}
		// D3ELogger.info("r emb: " + tt.getModel().getType());
		JSONObject json = parent.getJSONObject(key);
		if (json == null) {
			return;
		}
		readObjectProperties(obj, json, type);
	}

	public String toJsonString(String type, DBObject value) {
		JSONObject jsonObject = toJsonObject(type, value);
		return jsonObject.toString();
	}
	
	@SuppressWarnings({ "rawtypes" })
	private JSONObject toJsonObject(String type, DBObject obj) {
		DModel model = schema.getType(type);
		
		JSONObject result = writeObject(obj, model);
		return result;
	}

	@SuppressWarnings("rawtypes")
	private JSONObject writeObject(DBObject obj, DModel model) {
		JSONObject result = writeReference(obj, model);
		writeObjectProperties(obj, model, result);
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeObjectProperties(Object obj, DModel model, JSONObject result) {
		DField[] fields = model.getFields();
		for (DField df : fields) {
			if (!df.canSend()) {
				continue;
			}
			
			FieldType ft = df.getType();
			String fieldName = df.getName();
			
			switch (ft) {
			case Primitive:
				result.put(fieldName, df.getValue(obj));
				break;
			case PrimitiveCollection:
				List coll = (List) df.getValue(obj);
				JSONArray array = writePrimitiveColl(coll);
				result.put(fieldName, array);
				break;
			case Reference:
				if (df.getReference().isEmbedded()) {
					Object emb = df.getValue(obj);
					DModel embRef = df.getReference();
					result.put(fieldName, writeEmbedded(emb, embRef));
				} else if (df.getReference().getType().equals("DFile")) {
					DFile file = (DFile) df.getValue(obj);
					result.put(fieldName, writeDFile(file));
				} else if (df.isChild()) {
					Object child = df.getValue(obj);
					result.put(fieldName, writeObject((DBObject) child, df.getReference()));
				} else {
					Object ref = df.getValue(obj);
					result.put(fieldName, writeReference((DBObject) ref, df.getReference()));
				}
				break;
			case ReferenceCollection:
				List list = (List) df.getValue(obj);
				JSONArray arr = writeReferenceColl(list, df.getReference());
				result.put(fieldName, arr);
				break;
			case InverseCollection:
				// TODO
			default:
				break;
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private JSONObject writeReference(DBObject obj, DModel model) {
		if (obj == null) {
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		long id = 0;
		if (obj != null && !model.isEmbedded()) {
			id = obj.getId();
		}
		jsonObject.put("id", id);
		String display = obj.displayName();
		if (!Objects.equals(display, obj._type())) {
			// Indicates that there is a specific display in the Model.
			jsonObject.put("display", display);
		}
		return jsonObject;
	}
	
	@SuppressWarnings("rawtypes")
	private JSONArray writeReferenceColl(List list, DModel model) {
		if (list == null) {
			return null;
		}
		JSONArray array = new JSONArray();
		for (Object one : list) {
			DBObject dbObj = (DBObject) one;
			array.put(writeReference(dbObj, model));
		}
		return array;
	}
	
	@SuppressWarnings("rawtypes")
	private JSONArray writePrimitiveColl(List list) {
		if (list == null) {
			return null;
		}
		JSONArray arr = new JSONArray(list);
		return arr;
	}
	
	@SuppressWarnings("rawtypes")
	private JSONObject writeEmbedded(Object obj, DModel model) {
		JSONObject jsonObject = new JSONObject();
		writeObjectProperties(obj, model, jsonObject);
		return jsonObject;
	}
	
	private JSONObject writeDFile(DFile file) {
		if (file == null) {
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", file.getName());
		jsonObject.put("size", file.getSize());
		jsonObject.put("mimeType", file.getMimeType());
		return jsonObject;
	}
}
