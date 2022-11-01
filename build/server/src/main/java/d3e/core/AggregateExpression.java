package d3e.core;

import gqltosql.schema.FieldPrimitiveType;
import gqltosql.schema.FieldType;

public class AggregateExpression extends PropertyExpression {

	private String agg;
	private PropertyExpression field;

	public AggregateExpression(String agg, PropertyExpression field) {
		super(null, null);
		this.agg = agg;
		this.field = field;
	}

	@Override
	public FieldType getType() {
		return FieldType.Primitive;
	}

	@Override
	public FieldPrimitiveType getPrimitiveType() {
		return agg.equals("count") ? FieldPrimitiveType.Integer : FieldPrimitiveType.Double;
	}

	@Override
	public String toSql(Criteria criteria) {
		return agg + '(' + (field == null ? '*' : field.select(criteria)) + ')';
	}
}
