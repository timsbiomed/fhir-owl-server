package hot.fhir.owl.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations;

import java.util.HashMap;
import java.util.Map;

public class BaseOWLCodeSystemResourceProvider implements IResourceProvider {
    protected BiMap<String, String> uriMap = HashBiMap.create();
    protected FhirContext fhirContext;
    public BaseOWLCodeSystemResourceProvider(FhirContext fhirContext) {
        this.fhirContext = fhirContext;

        //TODO: find a better way to load the map
        this.uriMap.put("stato", "http://purl.obolibrary.org/obo/stato.owl");
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return CodeSystem.class;
    }

    public CodeSystem createR4CodeSystem() {
        CodeSystem codeSystem = new CodeSystem();
        codeSystem.setStatus(Enumerations.PublicationStatus.ACTIVE)
                .setExperimental(true)
                .setHierarchyMeaning(CodeSystem.CodeSystemHierarchyMeaning.ISA)
                .setCompositional(true)
                .setVersionNeeded(false)
                .setContent(CodeSystem.CodeSystemContentMode.COMPLETE);
        // Add filter and property
        codeSystem.addProperty()
                .setCode("parent")
                .setDescription("Parent codes.")
                .setType(CodeSystem.PropertyType.CODE);
        codeSystem.addProperty()
                .setCode("imported")
                .setDescription("\"Indicates if the concept is imported from another code system.\"")
                .setType(CodeSystem.PropertyType.BOOLEAN);
        codeSystem.addProperty()
                .setCode("root")
                .setDescription("Indicates if this concept is a root concept (i.e. Thing is equivalent or a direct parent)")
                .setType(CodeSystem.PropertyType.BOOLEAN);
        codeSystem.addProperty()
                .setCode("deprecated")
                .setDescription("Indicates if this concept is deprecated.")
                .setType(CodeSystem.PropertyType.BOOLEAN);
        codeSystem.addProperty()
                .setCode("r_command")
                .setDescription("R Command")
                .setType(CodeSystem.PropertyType.STRING);
        codeSystem.addFilter()
                .setCode("root")
                .addOperator(CodeSystem.FilterOperator.EQUAL)
                .setValue("True or false.");
        codeSystem.addFilter()
                .setCode("deprecated")
                .addOperator(CodeSystem.FilterOperator.EQUAL)
                .setValue("True or false.");
        codeSystem.addFilter()
                .setCode("imported")
                .addOperator(CodeSystem.FilterOperator.EQUAL)
                .setValue("True or false.");
        return codeSystem;
    }
}
