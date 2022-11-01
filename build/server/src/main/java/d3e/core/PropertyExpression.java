package d3e.core;

import gqltosql.schema.DField;
import gqltosql.schema.DModel;
import gqltosql.schema.FieldPrimitiveType;
import gqltosql.schema.FieldType;
import store.CustomFieldService;
import store.ICustomFieldProcessor;

public class PropertyExpression implements Criterion {

	private DField<?, ?> df;
	private PropertyExpression on;

	public PropertyExpression(PropertyExpression on, DField<?, ?> df) {
		this.on = on;
		this.df = df;
	}

	public PropertyExpression prop(String field) {
		DModel<?> ref = df.getReference();
		DField<?, ?> df = ref.getField(field);
		if (df == null) {
			ICustomFieldProcessor<?> processor = CustomFieldService.get().getProcessor(ref.getType());
			if (processor != null) {
				df = processor.getDField(field);
			}
		}
		return new PropertyExpression(this, df);
	}

	public PropertyExpression getOn() {
		return on;
	}

	public DField<?, ?> getDf() {
		return df;
	}

	public FieldType getType() {
		return df.getType();
	}

	public FieldPrimitiveType getPrimitiveType() {
		return df.getPrimitiveType();
	}

	@Override
	public String toSql(Criteria criteria) {
		String a;
		switch (df.getType()) {
		case InverseCollection:
		case PrimitiveCollection:
		case ReferenceCollection:
			a = criteria.getAlias(this);
			break;
		default:
			a = criteria.getAlias(on);
		}
		return a + "." + df.getColumnName();
	}

	public String select(Criteria criteria) {
		switch (getType()) {
		case InverseCollection:
		case Reference:
		case ReferenceCollection:
			return criteria.createRefColumn(df.getReference(), toSql(criteria));
		case Primitive:
		case PrimitiveCollection:
			return toSql(criteria);
		default:
			break;

		}
		return toSql(criteria);
	}
}
