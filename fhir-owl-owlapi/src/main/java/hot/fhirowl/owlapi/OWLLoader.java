package hot.fhirowl.owlapi;

import ca.uhn.fhir.context.FhirContext;
import hot.fhirowl.owlapi.impl.StatoTransformer;
import org.hl7.fhir.r4.model.CodeSystem;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.DublinCoreVocabulary;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.File;
import java.net.URI;
import java.util.Set;

public class OWLLoader {
    private OWLOntologyManager manager;
    private OWLReasonerFactory factory;
    private OWLDataFactory dataFactory;
    public OWLLoader() {
        this.manager = OWLManager.createOWLOntologyManager();
        this.factory = new ReasonerFactory();
        this.dataFactory = manager.getOWLDataFactory();
    }

    public OWLOntology getOntology(URI uri) throws OWLOntologyCreationException {
        IRI iri = IRI.create(uri);
        OWLOntology o = manager.loadOntology(iri);
        return o;
    }

    public OWLOntology loadOntologyFromFile(File file) throws OWLOntologyCreationException  {
        OWLOntology o = manager.loadOntologyFromOntologyDocument(file);
        return o;
    }

    public static void main(String[] args) throws Exception {
        URI uri = new URI("http://purl.obolibrary.org/obo/stato.owl");
        OWLOntology onto = null;
        OWLLoader loader = new OWLLoader();
        try {
            onto = loader.getOntology(uri);
        } catch (OWLOntologyCreationException ex) {
            System.err.println(ex.getMessage());
        } finally {
            if (onto == null) {
                ClassLoader classLoader = OWLLoader.class.getClassLoader();
                File file = new File(classLoader.getResource("stato.owl").getFile());
                onto = loader.loadOntologyFromFile(file);
            }
        }
        onto.annotations().forEach((ann) -> {
            IRI propIRI = ann.getProperty().getIRI();
            OWLLiteral value = (OWLLiteral) ann.getValue();
            if (propIRI.equals(DublinCoreVocabulary.TITLE.getIRI())) {
                System.out.println("Title: " + value.getLiteral());
            } else if (propIRI.equals(OWLRDFVocabulary.OWL_VERSION_INFO.getIRI())) {
                System.out.println("Version: " + value.getLiteral());
            }
        });

//        ReasonerFactory factory = new ReasonerFactory();
//        OWLReasoner reasoner = factory.createReasoner(onto);
//        OWLClass clazz = (OWLClass)onto.entitiesInSignature(IRI.create("http://purl.obolibrary.org/obo/STATO_0000033")).findFirst().get();
//        reasoner.getSuperClasses(clazz, true).entities().forEach(System.out::println);
//
//        System.out.println(clazz.getIRI().getNamespace());
//        System.out.println(clazz.getIRI().getScheme());

        Transformer transformer = new StatoTransformer();
        CodeSystem cs = transformer.transformToR4(onto);
        FhirContext ctxR4 = FhirContext.forR4();
        String encoded = ctxR4.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
        System.out.println(encoded);

//        onto.logicalAxioms().forEach(ax -> {
//            //            System.out.println(ax);
//        });
//        System.out.println("##########");
//        IRI iri = IRI.create("<http://purl.obolibrary.org/obo/");
//        ReasonerFactory factory = new ReasonerFactory();
//        OWLReasoner r = factory.createReasoner(onto);
//        Set<OWLClass> visited = new HashSet<>();
//        onto.classesInSignature().forEach(en -> {
//            printHierarchy(r, en, 0, visited);
//        });
    }

    public static void printHierarchy(OWLReasoner reasoner, OWLClass owlClass, int level, Set<OWLClass> visited) {
        if (!visited.contains(owlClass) && reasoner.isSatisfiable(owlClass)) {
            visited.add(owlClass);
            for (int i = 0; i < level * 4; i++) {
                System.out.print(" ");
            }
            System.out.println(labelFor(owlClass, reasoner.getRootOntology()));

            NodeSet<OWLClass> classNodeSet = reasoner.getSubClasses(owlClass, true);
            for (OWLClass child: classNodeSet.getFlattened()) {
                printHierarchy(reasoner, child, level+1, visited);
            }
        }
    }

    private static String labelFor(OWLClass clazz, OWLOntology o) {
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
        return EntitySearcher.getAnnotations(clazz, o)
                .map(anno -> anno.accept(visitor))
                .filter(value -> value != null)
                .findFirst()
                .orElse(clazz.getIRI().toString());
    }
}
