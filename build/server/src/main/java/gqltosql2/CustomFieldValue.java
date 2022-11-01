package gqltosql2;

public class CustomFieldValue extends SimpleValue {

	private int type;
	private long id;
	private String parentField;
	private String valueField;

	public CustomFieldValue(int type, long id, String parentField, String valueField, String field, int index) {
		super(field, index);
		this.type = type;
		this.id = id;
		this.parentField = parentField;
		this.valueField = valueField;
	}

	@Override
	public Object read(Object[] row, OutObject obj) throws Exception {
		OutObjectList list = (OutObjectList) obj.get(parentField);
		if (list == null) {
			list = new OutObjectList();
			obj.add(parentField, list);
		}
		OutObject read = new OutObject();
		Object val = super.read(row, read);
		read.setId(id);
		read.add(valueField, val);
		read.remove(field);
		read.addType(type);
		list.add(read);
		return read;
	}
}
