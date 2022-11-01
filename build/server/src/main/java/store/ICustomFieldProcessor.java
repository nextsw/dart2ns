package store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import gqltosql.schema.DField;
import gqltosql.schema.DModel;
import gqltosql2.AliasGenerator;
import gqltosql2.Field;
import gqltosql2.GqlToSql;
import gqltosql2.SqlAstNode;

public interface ICustomFieldProcessor<T> {

	void insert(D3EQuery query, DModel<T> type, T _this, List<String> cols, List<String> params, List<Object> args);

	void update(D3EQuery query, DModel<T> type, T _this, List<String> updates, List<Object> args);

	void selectAll(StringBuilder sb, DModel<T> type, long id, List selectedFields, List<String> joins,
			AliasGenerator ag, String alias);

	int readObject(ResultSet rs, int index, T obj, DField field, long customFieldId) throws SQLException;

	void addGqlToSqlFields(GqlToSql gql, SqlAstNode node, Field field, DField df);

	DField<?, ?> getDField(String field);
}
