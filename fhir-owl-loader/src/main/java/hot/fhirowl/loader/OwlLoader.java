package hot.fhirowl.loader;

import ca.uhn.fhir.context.FhirContext;
import hot.fhirowl.loader.impl.StatoTransformer;
import org.hl7.fhir.r4.model.CodeSystem;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.DublinCoreVocabulary;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.net.URI;

public class OwlLoader {
    private OWLOntologyManager manager;
    public OwlLoader() {
        this.manager = OWLManager.createOWLOntologyManager();
    }

    public OWLOntology getOntology(URI uri) throws OWLOntologyCreationException {
        IRI iri = IRI.create(uri);
        OWLOntology o = manager.loadOntology(iri);
        return o;
    }

    public static void main(String[] args) throws Exception {
        URI uri = new URI("http://purl.obolibrary.org/obo/stato.owl");
        OWLOntology onto = new OwlLoader().getOntology(uri);
        onto.annotations().forEach((ann) -> {
            IRI propIRI = ann.getProperty().getIRI();
            OWLLiteral value = (OWLLiteral) ann.getValue();
            if (propIRI.equals(DublinCoreVocabulary.TITLE.getIRI())) {
                System.out.println("Title: " + value.getLiteral());
            } else if (propIRI.equals(OWLRDFVocabulary.OWL_VERSION_INFO.getIRI())) {
                System.out.println("Version: " + value.getLiteral());
            }
        });

        Transformer transformer = new StatoTransformer();
        CodeSystem cs = transformer.transformToR4(onto);
        FhirContext ctxR4 = FhirContext.forR4();

        String encoded = ctxR4.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
        System.out.println(encoded);
    }
}
