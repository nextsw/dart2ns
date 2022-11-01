package repository.solr;

@org.springframework.stereotype.Repository
public interface VerificationDataSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<
        models.VerificationData, Long> {}
