package hot.fhir.owl.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ValueSet;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import java.util.List;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

public class ValueSetResourceProvider extends Neo4jResourceProvider {

    public ValueSetResourceProvider(FhirContext context) {
        super(context);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ValueSet.class;
    }

    @Read
    public ValueSet read(@IdParam IdType idType) {
        ValueSet valueSet = new ValueSet();
        List<Record> records = listCodesInVocabulary(idType.getIdPart());
        ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
        ValueSet.ConceptSetComponent conceptSet = new ValueSet.ConceptSetComponent();
        records.forEach(record -> {
            Map<String, Object> code = record.get(0).asMap();
            conceptSet.addConcept().setCode((String)code.get("value")).setDisplay((String)code.get("short_name"));
        });
        compose.addInclude(conceptSet);
        valueSet.setCompose(compose);
        return valueSet;
    }

    private List<Record> listCodesInVocabulary(String id) {
        try (Session session = driver.session()) {
            List<Record> records = session.readTransaction(new TransactionWork<List<Record>>() {
                @Override
                public List<Record> execute(Transaction transaction) {
                    return transaction.run("MATCH (n:Code)-[:PARTOF]->(v:Vocabulary) " +
                            "WHERE v.cdmh_id=$id " +
                            "RETURN n", parameters("id", Integer.valueOf(id))).list();
                }
            });
            return records;
        }
    }
}
