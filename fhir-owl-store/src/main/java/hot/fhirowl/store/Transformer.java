package hot.fhirowl.store;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Transforms an OwlOntology to a CodeSystem
 */
public interface Transformer {
    public org.hl7.fhir.r4.model.CodeSystem transformToR4(OWLOntology ontology);
    public org.hl7.fhir.dstu3.model.CodeSystem transformToDstu3(OWLOntology ontology);
}
