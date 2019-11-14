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
        Record record = listCodesInVocabulary(idType.getIdPart());
        ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
        ValueSet.ConceptSetComponent conceptSet = new ValueSet.ConceptSetComponent();
        Map<String, Object> vocMap = record.get("v").asMap();
        valueSet.setName((String)vocMap.get("name"));
        valueSet.setId((String)vocMap.get("cdmh_id"));
        record.get("codes").asList(code -> {
            Map<String, Object> codeMap = code.asMap();
            conceptSet.addConcept().setCode((String)codeMap.get("value")).setDisplay((String)codeMap.get("short_name"));
            return null;
        });
        compose.addInclude(conceptSet);
        valueSet.setCompose(compose);
        return valueSet;
    }

    private Record listCodesInVocabulary(String id) {
        try (Session session = driver.session()) {
            Record record = session.readTransaction(new TransactionWork<Record>() {
                @Override
                public Record execute(Transaction transaction) {
                    return transaction.run("MATCH (n:Code)-[:PART_OF]->(v:Vocabulary) " +
                            "WHERE v.cdmh_id=$id " +
                            "RETURN v, COLLECT(n)[0..30] as codes", parameters("id", id)).single();
                }
            });
            return record;
        }
    }
}
