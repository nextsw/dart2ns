package gqltosql2;

import gqltosql.schema.IModelSchema;

public class SqlInverseCollAstNode extends SqlAstNode {

	private String column;

	public SqlInverseCollAstNode(IModelSchema schema, String path, int type, String table, String column) {
		super(schema, path, type, table, false);
		this.column = column;
	}

	@Override
	public SqlQueryContext createCtx() {
		SqlQueryContext ctx = new SqlQueryContext(this, 1);		// Setting index as 1 because that's where the object id will be
		SqlQueryContext sub = ctx.subPrefix(String.valueOf(getType()));
		String from = sub.getFrom();
		sub.getQuery().setFrom(getTableName(), from);
		sub.addSelection(from + "." + column, "_parent");
		sub.addSelection(from + "._id", "_id");					// Adding object id. This will be read from index 1
		sub.getQuery().addWhere(from + "." + column + " in (:ids)");
		return ctx;
	}
}
