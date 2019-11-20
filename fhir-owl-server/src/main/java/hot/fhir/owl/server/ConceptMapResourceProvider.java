package hot.fhir.owl.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.model.util.JpaConstants;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.Read;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

public class ConceptMapResourceProvider extends Neo4jResourceProvider {
    private Logger logger = LoggerFactory.getLogger(ConceptMapResourceProvider.class);

    public ConceptMapResourceProvider(FhirContext context) {
        super(context);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ConceptMap.class;
    }

//    @Read
//    public ConceptMap read(@IdParam IdType theId) {
//        ConceptMap conceptMap = new ConceptMap();
//        return conceptMap;
//    }

    @Operation(name = JpaConstants.OPERATION_LOOKUP, idempotent = true, returnParameters = {
            @OperationParam(name="name", type= StringType.class, min=1),
            @OperationParam(name="version", type=StringType.class, min=0),
            @OperationParam(name="display", type=StringType.class, min=1),
            @OperationParam(name="abstract", type= BooleanType.class, min=1)
    })
    public ConceptMap lookup(HttpServletRequest servletRequest,
                             @OperationParam(name = "source-uri", min = 1, max = 1) UriType sourceUri ,
                             @OperationParam(name = "target-uri", min = 1, max = 1) UriType targetUri) {
        ConceptMap conceptMap = new ConceptMap();
        conceptMap.setSource(sourceUri).setTarget(targetUri);
        ConceptMap.ConceptMapGroupComponent group = conceptMap.addGroup();
        List<Record> mappings = listMapping(sourceUri.getValueAsString(), targetUri.getValueAsString());
        logger.debug("ConceptMap: " + conceptMap.toString());
        mappings.forEach(record -> {
            Map<String, Object> sourceCode = record.get("a").asMap();
            Map<String, Object> targetCode = record.get("b").asMap();
            ConceptMap.SourceElementComponent sourceElement = group.addElement();
            sourceElement.setCode((String)sourceCode.get("value")).setDisplay((String)sourceCode.get("short_name"));
            sourceElement.addTarget().setCode((String)targetCode.get("value")).setDisplay((String)targetCode.get("short_name"));
        });
        return conceptMap;
    }

    private List<Record> listMapping(String source, String target) {
        try (Session session = driver.session()) {
            List<Record> list = session.writeTransaction(new TransactionWork<List<Record>>() {
                @Override
                public List<Record> execute(Transaction transaction) {
                    return searchMapping(transaction, source, target);
                }
            });
            return list;
        }
    }

    private static List<Record> searchMapping(Transaction tx, String source, String target) {
        return tx.run("MATCH (v1:Vocabulary)<-[:PART_OF]-(a:Code)-[:MAP_TO]->(b:Code)-[:PART_OF]->(v2:Vocabulary) " +
                "WHERE v1.cdmh_id=$source AND v2.cdmh_id=$target " +
                "RETURN a, b " +
                "LIMIT 300", parameters("source", source, "target", target)).list();
    }
}
