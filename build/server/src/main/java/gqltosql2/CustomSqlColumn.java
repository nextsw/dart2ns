package gqltosql2;

import java.util.List;

import gqltosql.schema.IModelSchema;
import store.IEntityManager;

public class CustomSqlColumn extends SqlColumn {

	private int type;
	private long id;
	private String parentField;
	private String valueField;
	private String customFieldColumn;

	public CustomSqlColumn(int type, long id, String customFieldColumn, String parentField, String valueField,
			String column, String fieldName) {
		super(column, fieldName);
		this.type = type;
		this.id = id;
		this.customFieldColumn = customFieldColumn;
		this.parentField = parentField;
		this.valueField = valueField;
	}

	@Override
	public void addColumn(SqlTable table, SqlQueryContext ctx) {
		ctx.addCustomFieldSelection(type, id, customFieldColumn, parentField, valueField, ctx.getFrom() + '.' + column,
				getFieldName());
	}

	@Override
	public void extractDeepFields(IEntityManager em, IModelSchema schema, int type, List<OutObject> rows)
			throws Exception {
		for (OutObject row : rows) {
			Object val = row.get(customFieldColumn);
			row.remove(customFieldColumn);
			OutObjectList list = (OutObjectList) row.get(parentField);
			for (OutObject obj : list) {
				if (obj.getId() == id) {
					obj.add("field", val);
					break;
				}
			}
		}
	}
}
