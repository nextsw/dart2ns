package d3e.core;

public class DateNowExpression implements Criterion {

	@Override
	public String toSql(Criteria criteria) {
		return "now()";
	}
}
