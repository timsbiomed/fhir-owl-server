package hot.fhir.owl.server;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.IdType;

import java.util.HashMap;
import java.util.Map;

public class OwlCodeSystemResourceProvider implements IResourceProvider {
    private Map<String, CodeSystem> codeSystems = new HashMap<>();

    public OwlCodeSystemResourceProvider() {
        CodeSystem cs = new CodeSystem();
        cs.setId("1");
        cs.addIdentifier().setSystem("").setValue("");
        codeSystems.put("1", cs);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return CodeSystem.class;
    }

    @Read
    public CodeSystem read(@IdParam IdType theId) {
        CodeSystem cs = codeSystems.get(theId.getIdPart());
        if (cs == null) {
            throw new ResourceNotFoundException(theId);
        }
        return cs;
    }
}
