package hot.fhirowl.owlapi.impl;

import hot.fhirowl.owlapi.Transformer;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations;
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
import java.util.concurrent.atomic.AtomicInteger;

public class StatoTransformer implements Transformer {
    @Override
    public CodeSystem transformToR4(OWLOntology ontology) {
        CodeSystem cs = createCodeSystemR4(ontology);
        return cs;
    }

    private CodeSystem createCodeSystemR4(OWLOntology ontology) {
        CodeSystem cs = new CodeSystem();

        // For notes on mapping, see
        // https://github.com/hot-fhir/fhir-owl/wiki/STATO-OWL-to-FHIR-CodeSystem-mappings
        cs.setName("STATO");
        ontology.annotations().forEach(annotation -> {
            IRI propIRI = annotation.getProperty().getIRI();
            OWLLiteral value = (OWLLiteral) annotation.getValue();
            if (propIRI.equals(DublinCoreVocabulary.TITLE.getIRI())) { //dc:title
                cs.setTitle(value.getLiteral());
            } else if (propIRI.equals(DublinCoreVocabulary.DESCRIPTION.getIRI())) {  // dc:description
                cs.setDescription(value.getLiteral());
            } else if (propIRI.getIRIString().equals("http://purl.org/dc/terms/license")) { // terms:license
                cs.setCopyright(value.getLiteral());
            } else if (propIRI.equals(OWLRDFVocabulary.OWL_VERSION_INFO.getIRI())) { // owl:versioninfo
                cs.setVersion(value.getLiteral());
            } else if (propIRI.equals(DublinCoreVocabulary.SUBJECT.getIRI())) {  // dc:subject
                cs.setPurpose(value.getLiteral());
            }
            cs.setStatus(Enumerations.PublicationStatus.ACTIVE)
                    .setExperimental(true)
                    .setHierarchyMeaning(CodeSystem.CodeSystemHierarchyMeaning.ISA)
                    .setCompositional(true)
                    .setVersionNeeded(false)
                    .setContent(CodeSystem.CodeSystemContentMode.COMPLETE);
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
        cs.addProperty()
                .setCode("r_command")
                .setDescription("R Command")
                .setType(CodeSystem.PropertyType.STRING);
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
        AtomicInteger counter = new AtomicInteger(0);
        ontology.classesInSignature()
                .filter(clazz -> !clazz.getIRI().equals(IRI.create("http://www.w3.org/2002/07/owl#Thing")))
                .forEach(clazz -> {
                    visitClass(reasoner, clazz, visited, ontology, codeSystem);
                    counter.getAndIncrement();
                });
        codeSystem.setCount(counter.intValue());
        return codeSystem;
    }

    private CodeSystem visitClass(OWLReasoner reasoner, OWLClass clazz, Set<OWLClass> visited, OWLOntology ontology, CodeSystem codeSystem) {
        if (!visited.contains(clazz) && reasoner.isSatisfiable(clazz)) {
            visited.add(clazz);
            CodeSystem.ConceptDefinitionComponent concept = codeSystem.addConcept();

            String code = clazz.getIRI().getRemainder().get();
            concept.setCode(code);

            StatoAnnotationVisitor visitor = new StatoAnnotationVisitor(concept);
            EntitySearcher.getAnnotations(clazz, ontology).forEach(annotation -> annotation.accept(visitor));

            NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(clazz, true);
            if (superClasses.isSingleton()) {
                OWLClass parent = superClasses.entities().findFirst().get();
                if (parent.getIRI().equals(IRI.create("http://www.w3.org/2002/07/owl#Thing"))) {
                    concept.addProperty().setCode("root").setValue(new BooleanType(true));
                } else {
                    concept.addProperty().setCode("root").setValue(new BooleanType(false));
                    concept.addProperty().setCode("parent").setValue(new StringType(parent.getIRI().getRemainder().get()));
                }
            } else {
                concept.addProperty().setCode("root").setValue(new BooleanType(true));
                superClasses
                        .entities()
                        .filter(parent -> !parent.getIRI().equals(IRI.create("http://www.w3.org/2002/07/owl#Thing")))
                        .forEach(parent -> concept.addProperty().setCode("parent").setValue(new StringType(parent.getIRI().getRemainder().get())));

            }
            if (!concept.getProperty().stream().anyMatch(property -> property.getCode().equals("imported"))) {
                concept.addProperty().setCode("imported").setValue(new BooleanType(false));
            }
            // TODO: implement the next line for all cases
            concept.addProperty().setCode("deprecated").setValue(new BooleanType(false));
        }
        return codeSystem;
    }

    @Override
    public org.hl7.fhir.dstu3.model.CodeSystem transformToDstu3(OWLOntology ontology) {
        return null;
    }

    class StatoAnnotationVisitor implements OWLAnnotationObjectVisitor {
        private CodeSystem.ConceptDefinitionComponent concept;

        public StatoAnnotationVisitor(CodeSystem.ConceptDefinitionComponent concept) {
            this.concept = concept;
        }

        @Override
        public void visit(OWLAnnotation node) {
            if (node.getProperty().isLabel()) {
                concept.setDisplay(getAnnotationLiteral(node));
            } else {
                IRI propIRI = node.getProperty().getIRI();
                if (propIRI.equals(IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"))) {
                    concept.setDefinition(getAnnotationLiteral(node));
                } else if (propIRI.equals(IRI.create("http://purl.obolibrary.org/obo/IAO_0000118"))) {
                    concept.setDisplay(getAnnotationLiteral(node));
                } else if (propIRI.equals(IRI.create("http://purl.obolibrary.org/obo/STATO_0000041"))) {
                    concept.addProperty().setCode("r_command").setValue(new StringType(getAnnotationLiteral(node)));
                } else if (propIRI.equals(IRI.create("http://purl.obolibrary.org/obo/IAO_0000412"))) {
                    concept.addProperty().setCode("imported").setValue(new BooleanType(true));
                }
            }
        }
    }

    private String getAnnotationLiteral(OWLAnnotation annotation) {
        OWLAnnotationValue value = annotation.getValue();
        if (value.isLiteral()) {
            return value.literalValue().get().getLiteral();
        } else {
            return null;
        }
    }
}
