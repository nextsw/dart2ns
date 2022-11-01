package d3e.core;

public class BinaryCriterion extends SimpleExpression {
	private String op;
	private Object rval;

	public BinaryCriterion(String op, PropertyExpression prop, Object rval) {
		super(prop);
		this.op = op;
		this.rval = rval;
	}

	@Override
	public String toSql(Criteria criteria) {
		return prop.toSql(criteria) + " " + op + " " + createArgument(rval);
	}
}
