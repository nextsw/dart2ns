package d3e.core;

import java.util.List;

public class Restrictions {

	public static SimpleExpression eq(PropertyExpression prop, Object val) {
		return new BinaryCriterion("=", prop, val);
	}

	public static SimpleExpression lt(PropertyExpression prop, Object val) {
		return new BinaryCriterion("<", prop, val);
	}

	public static SimpleExpression le(PropertyExpression prop, Object val) {
		return new BinaryCriterion("<=", prop, val);
	}

	public static SimpleExpression gt(PropertyExpression prop, Object val) {
		return new BinaryCriterion(">", prop, val);
	}

	public static SimpleExpression ge(PropertyExpression prop, Object val) {
		return new BinaryCriterion(">=", prop, val);
	}

	public static Criterion between(PropertyExpression prop, Object lo, Object hi) {
		return new BetweenCriterion(prop, lo, hi);
	}

	public static Criterion in(PropertyExpression name, List<?> list) {
		return new InCriterion(name, list);
	}

	public static Criterion now() {
		return new DateNowExpression();
	}
}
