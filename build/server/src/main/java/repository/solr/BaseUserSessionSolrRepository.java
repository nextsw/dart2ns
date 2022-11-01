package repository.solr;

@org.springframework.stereotype.Repository
public interface BaseUserSessionSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<
        models.BaseUserSession, Long> {}
