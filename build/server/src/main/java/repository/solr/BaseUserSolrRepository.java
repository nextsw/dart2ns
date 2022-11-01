package repository.solr;

@org.springframework.stereotype.Repository
public interface BaseUserSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<models.BaseUser, Long> {}
