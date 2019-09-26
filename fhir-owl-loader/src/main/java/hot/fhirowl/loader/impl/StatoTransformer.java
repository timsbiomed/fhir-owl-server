package hot.fhirowl.loader.impl;

import hot.fhirowl.loader.Transformer;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StringType;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.DublinCoreVocabulary;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.HashSet;
import java.util.Set;

public class StatoTransformer implements Transformer {
    @Override
    public CodeSystem transformToR4(OWLOntology ontology) {
        CodeSystem cs = createCodeSystemR4(ontology);
        return cs;
    }

    private CodeSystem createCodeSystemR4(OWLOntology ontology) {
        CodeSystem cs = new CodeSystem();
        cs.setName("http://purl.obolibrary.org/obo/stato.owl");

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
                cs.setVersion(value.getLiteral());
            }
            cs.setUrl("http://stato-ontology.org/");
        });

        // Add filter and property
        cs.addProperty()
                .setCode("parent")
                .setDescription("Parent codes.")
                .setType(CodeSystem.PropertyType.CODE);
        cs.addProperty()
                .setCode("imported")
                .setDescription("\"Indicates if the concept is imported from another code system.\"")
                .setType(CodeSystem.PropertyType.BOOLEAN);
        cs.addProperty()
                .setCode("root")
                .setDescription("Indicates if this concept is a root concept (i.e. Thing is equivalent or a direct parent)")
                .setType(CodeSystem.PropertyType.BOOLEAN);
        cs.addProperty()
                .setCode("deprecated")
                .setDescription("Indicates if this concept is deprecated.")
                .setType(CodeSystem.PropertyType.BOOLEAN);
        cs.addFilter()
                .setCode("root")
                .addOperator(CodeSystem.FilterOperator.EQUAL)
                .setValue("True or false.");
        cs.addFilter()
                .setCode("deprecated")
                .addOperator(CodeSystem.FilterOperator.EQUAL)
                .setValue("True or false.");
        cs.addFilter()
                .setCode("imported")
                .addOperator(CodeSystem.FilterOperator.EQUAL)
                .setValue("True or false.");
        return addConceptsR4(cs, ontology);
    }

    private CodeSystem addConceptsR4(CodeSystem codeSystem, OWLOntology ontology) {
        ReasonerFactory factory = new ReasonerFactory();
        OWLReasoner reasoner = factory.createReasoner(ontology);
        Set<OWLClass> visited = new HashSet<>();
        ontology.classesInSignature().forEach(clazz -> visitClass(reasoner, clazz, visited, ontology, codeSystem));
        return codeSystem;
    }

    private CodeSystem visitClass(OWLReasoner reasoner, OWLClass clazz, Set<OWLClass> visited, OWLOntology ontology, CodeSystem codeSystem) {
        if (!visited.contains(clazz) && reasoner.isSatisfiable(clazz)) {
            visited.add(clazz);
            CodeSystem.ConceptDefinitionComponent concept = codeSystem.addConcept();

            String code = clazz.getIRI().getRemainder().get();

            concept.setCode(code);
            concept.setDisplay(labelFor(clazz, ontology));

            if (code.startsWith("STATO_")) {
                concept.addProperty().setCode("imported").setValue(new BooleanType(false));
            } else {
                concept.addProperty().setCode("imported").setValue(new BooleanType(true));
            }

            NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(clazz, true);
            superClasses.entities().forEach(parent -> {
                concept.addProperty().setCode("parent").setValue(new StringType(parent.getIRI().getRemainder().get()));
            });
            // TODO: implement the next two lines for all cases
            concept.addProperty().setCode("root").setValue(new BooleanType(false));
            concept.addProperty().setCode("deprecated").setValue(new BooleanType(false));
        }
        return codeSystem;
    }

    private String labelFor(OWLClass clazz, OWLOntology ontology) {
        OWLAnnotationObjectVisitorEx<String> visitor = new OWLAnnotationObjectVisitorEx<String>() {
            String value;

            @Override
            public String visit(OWLAnnotation node) {
                if (node.getProperty().isLabel()) {
                    return ((OWLLiteral) node.getValue()).getLiteral();
                }
                return null;
            }
        };
        return EntitySearcher.getAnnotations(clazz, ontology)
                .map(anno -> anno.accept(visitor))
                .filter(value -> value != null)
                .findFirst()
                .orElse(clazz.getIRI().toString());
    }


    @Override
    public org.hl7.fhir.dstu3.model.CodeSystem transformToDstu3(OWLOntology ontology) {
        return null;
    }


}
