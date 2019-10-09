package hot.fhirowl.store.impl;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.semanticweb.owlapi.model.OWLOntology;

public class StatoTransfomerTest {
    @Mock
    OWLOntology ontoMock;

    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testOntology() {
//        StatoTransformer transformer = new StatoTransformer();
//        CodeSystem cs = transformer.transformToR4(ontoMock);
//        FhirContext ctxR4 = FhirContext.forR4();
//
//        String encoded = ctxR4.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
//        System.out.println(encoded);
    }

    @Test
    public void testCodeSystemFilter() {
        StatoTransformer transformer = new StatoTransformer();

    }
}
