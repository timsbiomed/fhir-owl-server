package hot.fhir.owl.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IContextValidationSupport;
import ca.uhn.fhir.jpa.model.util.JpaConstants;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import hot.fhirowl.owlapi.OWLLoader;
import hot.fhirowl.owlapi.impl.StatoTransformer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class InMemoryOWLCodeSystemResourceProvider extends BaseOWLCodeSystemResourceProvider {
    private Map<String, CodeSystem> codeSystems = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(InMemoryOWLCodeSystemResourceProvider.class);
    private FhirContext fhirContext;

    public InMemoryOWLCodeSystemResourceProvider(FhirContext fhirContext) {
        super(fhirContext);

        // TODO: Change this to load dynamically
        String id = "stato";
        String url = "http://purl.obolibrary.org/obo/stato.owl";
        try {
            Instant start = Instant.now();
            OWLOntology ontology = new OWLLoader().getOntology(new URI(url));
            Instant step1 = Instant.now();
            logger.info("Total execution time for loading ontology: {}", Duration.between(start, step1).toMillis());
            StatoTransformer transformer = new StatoTransformer();
            CodeSystem codeSystem = transformer.transformToR4(ontology);
            codeSystem.setId(id);
            codeSystem.setUrl(url);
            codeSystems.put(id, codeSystem);
            Instant finish = Instant.now();
            logger.info("Total execution time for transforming ontology: {}", Duration.between(step1, finish).toMillis());
        } catch (OWLOntologyCreationException | URISyntaxException ex) {
            System.err.println("Failed to load ontology");
        }
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return CodeSystem.class;
    }

    @Read
    public CodeSystem read(@IdParam IdType theId) {
        CodeSystem cs = codeSystems.get(theId.getIdPart());
        if (cs == null) {
            throw new ResourceNotFoundException(theId);
        }
        return cs;
    }

    @Search
    public List<CodeSystem> findCodeSystemsByTitle(@RequiredParam(name = "title") StringDt title) {
        LinkedList<CodeSystem> list = new LinkedList<>();
        for (CodeSystem codeSystem: codeSystems.values()) {
            if (codeSystem.getTitle().toLowerCase().contains(title.toString().toLowerCase())) {
                list.add(codeSystem);
            }
        }
        return list;
    }

    @Search
    public List<CodeSystem> findAllCodeSystems() {
        return new ArrayList<>(codeSystems.values());
    }

    @Operation(name = JpaConstants.OPERATION_LOOKUP, idempotent = true, returnParameters = {
            @OperationParam(name="name", type=StringType.class, min=1),
            @OperationParam(name="version", type=StringType.class, min=0),
            @OperationParam(name="display", type=StringType.class, min=1),
            @OperationParam(name="abstract", type=BooleanType.class, min=1)
    })
    public Parameters lookup(HttpServletRequest servletRequest,
                             @OperationParam(name = "code", min = 0, max = 1) CodeType code,
                             @OperationParam(name = "system", min = 0, max = 1) UriType system,
                             @OperationParam(name = "codeing", min = 0, max = 1) Coding coding,
                             @OperationParam(name = "property", min = 0, max = OperationParam.MAX_UNLIMITED) List<CodeType> properties) {
        Parameters parameters = new Parameters();
        IContextValidationSupport.LookupCodeResult result = new IContextValidationSupport.LookupCodeResult();
        if (uriMap.containsKey(system.getValue())) {
            CodeSystem cs = codeSystems.get(uriMap.get(system.getValue()));
            for (CodeSystem.ConceptDefinitionComponent concept: cs.getConcept()) {
                if (concept.getCode().equals(code.getCode())) {
                    parameters.addParameter().setName("display").setValue(concept.getDisplayElement());
                    parameters.addParameter().setName("name").setValue(cs.getNameElement());
                    concept.getProperty().forEach(property -> {
                        Parameters.ParametersParameterComponent codeParam = new Parameters.ParametersParameterComponent();
                        codeParam.setName("code").setValue(property.getCodeElement());
                        Parameters.ParametersParameterComponent valueParam = new Parameters.ParametersParameterComponent();
                        valueParam.setName("value").setValue(property.getValue());
                        parameters.addParameter().setName("property").addPart(codeParam).addPart(valueParam);
                    });
                }
            }
        }

        return parameters;
    }
}
