package hot.fhirowl.loader.impl;

import hot.fhirowl.loader.Transformer;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Meta;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.DublinCoreVocabulary;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class StatoTransformer implements Transformer {
    @Override
    public CodeSystem transformToR4(OWLOntology ontology) {
        CodeSystem cs = new CodeSystem();
        Meta meta = new Meta();

        ontology.annotations().forEach(ann -> {
            IRI propIRI = ann.getProperty().getIRI();
            OWLLiteral value = (OWLLiteral) ann.getValue();
            if (propIRI.equals(DublinCoreVocabulary.TITLE.getIRI())) {
                cs.setTitle(value.getLiteral());
            } else if (propIRI.equals(DublinCoreVocabulary.DESCRIPTION)) {
                cs.setDescription(value.getLiteral());
            } else if (propIRI.getIRIString().equals("http://purl.org/dc/terms/license")) {
                cs.setCopyright(value.getLiteral());
            } else if (propIRI.equals(OWLRDFVocabulary.OWL_VERSION_INFO.getIRI())) {
                meta.setVersionId(value.getLiteral());
            }
            cs.setMeta(meta);
            cs.setUrl("http://stato-ontology.org/");
        });
        return cs;
    }

    @Override
    public org.hl7.fhir.dstu3.model.CodeSystem transformToDstu3(OWLOntology ontology) {
        return null;
    }


}
