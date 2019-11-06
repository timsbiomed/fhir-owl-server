package hot.fhir.owl.server;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = {"/*"}, displayName = "FHIR OWL Server")
public class FhirOwlServlet extends RestfulServer {
    private static final long serialVersionUID = 1L;

    public FhirOwlServlet() {
        super(FhirContext.forR4());
    }

    @Override
    public void initialize() {
        List<IResourceProvider> providers = new ArrayList<>();
        // providers.add(new InMemoryOWLCodeSystemResourceProvider(getFhirContext()));
        providers.add(new GraphDBCodeSystemResourceProvider(getFhirContext()));
        providers.add(new ConceptMapResourceProvider(getFhirContext()));
        providers.add(new ValueSetResourceProvider(getFhirContext()));
        setResourceProviders(providers);

        INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
        getFhirContext().setNarrativeGenerator(narrativeGen);

        registerInterceptor(new ResponseHighlighterInterceptor());
    }
}
