package gqltosql2;

import java.util.ArrayList;
import java.util.List;

public class SqlTable {

	private String table;
	private int type;
	private List<ISqlColumn> columns = new ArrayList<>();
	private boolean embedded;
	private String idColumn;

	public SqlTable(int type, String table, boolean embedded) {
		this.type = type;
		this.table = table;
		this.embedded = embedded;
	}

	public void addColumn(ISqlColumn column) {
		if (column.getFieldName().equals("id")) {
			return;
		}
		for (ISqlColumn c : columns) {
			if (c.getFieldName().equals(column.getFieldName())) {
				return;
			}
		}
		columns.add(column);
	}

	public String getTableName() {
		return table;
	}

	public int getType() {
		return type;
	}

	public List<ISqlColumn> getColumns() {
		return columns;
	}

	public void addSelections(SqlQueryContext ctx) {
		if (!embedded) {
			ctx.addSelection(ctx.getFrom() + "._id", "id");
		}
		getColumns().forEach(c -> c.addColumn(this, ctx));
	}

	@Override
	public String toString() {
		return table;
	}

	public ISqlColumn getColumn(String name) {
		for (ISqlColumn c : columns) {
			if (c.getFieldName().equals(name)) {
				return c;
			}
		}
		return null;
	}

	public String getIdColumn(String alias) {
		 return idColumn != null ? idColumn : (alias + "._id");
	}

	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}
}
