package d3e.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gqltosql.schema.DField;
import gqltosql.schema.DModel;
import gqltosql.schema.FieldType;
import gqltosql2.AliasGenerator;
import store.CustomFieldService;
import store.ICustomFieldProcessor;
import store.Query;

public class Criteria {

	private CriteriaHelper helper;
	private List<Criterion> wheres = new ArrayList<>();
	private List<PropertyExpression> selections = new ArrayList<>();
	private String type;
	private DModel<?> model;
	private String query;
	private AliasGenerator ag;
	private String alias;
	private List<String> joins = new ArrayList<>();
	private List<PropertyExpression> groupBys = new ArrayList<>();
	private List<Object> arguments = new ArrayList<>();
	private Map<String, String> aliases = new HashMap<>();

	public static Criteria on(String type) {
		return new Criteria(type);
	}

	private Criteria(String type) {
		this.type = type;
		this.helper = CriteriaHelper.get();
		this.model = helper.getSchema().getType(type);
		this.ag = new AliasGenerator();
		this.alias = ag.next();
	}

	public String getType() {
		return type;
	}

	public boolean hasConditions() {
		return !wheres.isEmpty();
	}

	public List<Criterion> getConditions() {
		return wheres;
	}

	public Criteria addCriterion(Criterion condition) {
		wheres.add(condition);
		return this;
	}

	public Criteria addGrouopBy(PropertyExpression condition) {
		groupBys.add(condition);
		return this;
	}

	public void addSelection(PropertyExpression field) {
		this.selections.add(field);
	}

	public PropertyExpression prop(String field) {
		DField<?, ?> df = model.getField(field);
		if (df == null) {
			ICustomFieldProcessor<?> processor = CustomFieldService.get().getProcessor(model.getType());
			if (processor != null) {
				df = processor.getDField(field);
			}
		}
		return new PropertyExpression(null, df);
	}

	public ParamExpression param(String param) {
		return new ParamExpression(param);
	}

	public List<RawResult> listRaw() {
		Query query = helper.getProvider().get().createNativeQuery(createQuery());
		for (int i = 0; i < arguments.size(); i++) {
			query.setParameter(i, arguments.get(i));
		}
		@SuppressWarnings("unchecked")
		Stream<RawResult> s = query.getResultStream().map(r -> new RawResult(helper, (Object[]) r));
		List<RawResult> rows = s.collect(Collectors.toList());
		return rows;
	}

	public <T> List<T> list(Function<RawResult, T> map) {
		return listRaw().stream().map(map).collect(Collectors.toList());
	}

	public String getAlias(PropertyExpression on) {
		if (on == null) {
			return alias;
		}
		PropertyExpression on2 = on.getOn();
		String a;
		if (on2 != null) {
			a = getAlias(on2);
		} else {
			a = alias;
		}
		DField<?, ?> df = on.getDf();
		String clm = a + "." + df.getColumnName();
		String alias = aliases.get(clm);
		if (alias == null) {
			FieldType ft = df.getType();
			String idColumn = "_id";
			switch (ft) {
			case InverseCollection:
				idColumn = df.getColumnName();
				clm = a + "._id";
				break;
			case PrimitiveCollection:
			case ReferenceCollection:
				String ca = ag.next();
				String coll = df.getCollTableName(null);
				String join = coll + " " + ca + " on " + ca + "." + df.declType().getTableName() + "_id = " + a
						+ "._id";
				joins.add(join);
				a = ca;
				clm = a + "." + df.getColumnName();
				break;
			case Primitive:
			case Reference:
				clm = a + "." + df.getColumnName();
				break;
			}
			if (ft == FieldType.PrimitiveCollection) {
				alias = a;
				aliases.put(clm, alias);
			} else {
				alias = ag.next();
				aliases.put(clm, alias);
				String join = df.getReference().getTableName() + " " + alias + " on " + alias + "." + idColumn + " = "
						+ clm;
				joins.add(join);
			}
		}
		return alias;
	}

	public String createRefColumn(DModel<?> model, String clm) {
		return helper.getQueryBuilder().createRefColumn(model, clm);
	}

	private String createQuery() {
		if (query != null) {
			return query;
		}
		List<String> selectedColumns = new ArrayList<>();
		if (selections.isEmpty()) {
			selectedColumns.add(createRefColumn(model, "_id"));
		} else {
			selections.forEach(s -> selectedColumns.add(s.select(this)));
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(ListExt.join(selectedColumns, ", "));
		sb.append(" from ").append(model.getTableName()).append(" ").append(alias);
		joins.forEach(j -> sb.append(" left join ").append(j));
		if (!wheres.isEmpty()) {
			List<String> conditions = new ArrayList<>();
			wheres.forEach(w -> conditions.add(w.toSql(this)));
			sb.append(" where ").append(ListExt.join(conditions, " and "));
		}
		if (!groupBys.isEmpty()) {
			List<String> groups = new ArrayList<>();
			groupBys.forEach(g -> groups.add(g.toSql(this)));
			sb.append(" group by ").append(ListExt.join(groups, ", "));
		}
		return query = sb.toString();
	}
}
