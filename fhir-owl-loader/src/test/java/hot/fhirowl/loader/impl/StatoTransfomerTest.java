package hot.fhirowl.loader.impl;

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
        StatoTransformer transformer = new StatoTransformer();
        transformer.transformToR4(ontoMock);

    }
}
