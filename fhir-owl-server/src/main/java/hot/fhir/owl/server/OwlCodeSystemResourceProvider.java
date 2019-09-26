package hot.fhir.owl.server;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import hot.fhirowl.loader.OWLLoader;
import hot.fhirowl.loader.Transformer;
import hot.fhirowl.loader.impl.StatoTransformer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.IdType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class OwlCodeSystemResourceProvider implements IResourceProvider {
    private Map<String, CodeSystem> codeSystems = new HashMap<>();


    public OwlCodeSystemResourceProvider() {
        // TODO: Change this to load dynamically
        String id = "stato";
        String url = "http://purl.obolibrary.org/obo/stato.owl";
        try {
            OWLOntology ontology = new OWLLoader().getOntology(new URI(url));
            Transformer transformer = new StatoTransformer();
            CodeSystem codeSystem = transformer.transformToR4(ontology);
            codeSystem.setId(id);
            codeSystem.setUrl(url);
            codeSystems.put(id, codeSystem);
        } catch (URISyntaxException | OWLOntologyCreationException usex) {
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
}
