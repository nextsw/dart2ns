package gqltosql2;

import gqltosql.schema.IModelSchema;

public class SqlAstCustomFieldNode extends SqlAstNode {

	public SqlAstCustomFieldNode(IModelSchema schema, String path, int type, String table, String idColumn) {
		super(schema, path, type, table, false);
		getTables().values().forEach(t -> t.setIdColumn(idColumn));
	}
}
