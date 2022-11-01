package d3e.core;

import java.util.List;

public class InCriterion extends SimpleExpression {

	private List<?> list;

	public InCriterion(PropertyExpression prop, List<?> list) {
		super(prop);
		this.list = list;
	}

	@Override
	public String toSql(Criteria criteria) {
		List<String> args = ListExt.map(list, a -> createArgument(a));
		return prop.toSql(criteria) + " in (" + ListExt.join(args, ",") + ")";
	}
}
