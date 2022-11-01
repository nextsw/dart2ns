package d3e.core;

public class BetweenCriterion extends SimpleExpression {
	private Object lo;
	private Object hi;

	public BetweenCriterion(PropertyExpression prop, Object lo, Object hi) {
		super(prop);
		this.lo = lo;
		this.hi = hi;
	}

	@Override
	public String toSql(Criteria criteria) {
		return prop.toSql(criteria) + " between " + createArgument(lo) + " and " + createArgument(hi);
	}
}
