package d3e.core;

public abstract class SimpleExpression implements Criterion {

	protected PropertyExpression prop;

	protected SimpleExpression(PropertyExpression prop) {
		this.prop = prop;
	}

	protected String createArgument(Object arg) {
		if(arg == null) {
			return "null";
		}
		if(arg instanceof String) {
			return "'" + arg + "'";
		}
		return arg.toString();
	}
}
