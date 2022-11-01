package lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import store.D3EPersistanceList;
import store.DBObject;

public class TypeAndId {
	public TypeAndId(int type, long id) {
		this.type = type;
		this.id = id;
	}

	public int type;
	public long id;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeAndId) {
			TypeAndId other = (TypeAndId) obj;
			return other.type == type && other.id == id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.type ^ (int) this.id;
	}

	public static TypeAndId from(DBObject obj) {
		if (obj == null) {
			return null;
		}
		return new TypeAndId(obj._typeIdx(), obj.getId());
	}

	public static List<TypeAndId> from(List<?> obj) {
		List<TypeAndId> res = new ArrayList<>();
		obj.forEach(a -> res.add(from((DBObject) a)));
		return res;
	}

	public static List<TypeAndId> fromList(List<? extends DBObject> list, int idx, DBObject master) {
		D3EPersistanceList<TypeAndId> result = new D3EPersistanceList<>(master, idx);
		List<TypeAndId> mapped = list.stream().map(a -> new TypeAndId(a._typeIdx(), a.getId()))
				.collect(Collectors.toList());
		result.setAll(mapped);
		return result;
	}
}
