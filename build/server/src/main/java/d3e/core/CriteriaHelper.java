package d3e.core;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gqltosql.schema.IModelSchema;
import store.D3EEntityManagerProvider;
import store.D3EQueryBuilder;

@Service
public class CriteriaHelper {
	@Autowired private IModelSchema schema;
	@Autowired private D3EEntityManagerProvider provider;
	@Autowired private D3EQueryBuilder builder;
	
	private static CriteriaHelper INS;
	
	@PostConstruct
	public void init() {
		INS = this;
	}
	
	public static CriteriaHelper get() {
		return INS;
	}
	
	public IModelSchema getSchema() {
		return schema;
	}

	public D3EEntityManagerProvider getProvider() {
		return provider;
	}

	public D3EQueryBuilder getQueryBuilder() {
		return builder;
	}
}
