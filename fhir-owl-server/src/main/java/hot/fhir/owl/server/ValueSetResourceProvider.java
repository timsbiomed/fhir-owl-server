package hot.fhir.owl.server;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;

public class ValueSetResourceProvider extends Neo4jResourceProvider {

    public ValueSetResourceProvider(FhirContext context) {
        super(context);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ValueSet.class;
    }
}
