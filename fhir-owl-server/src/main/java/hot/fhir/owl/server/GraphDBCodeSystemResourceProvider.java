package hot.fhir.owl.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IContextValidationSupport;
import ca.uhn.fhir.jpa.model.util.JpaConstants;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class GraphDBCodeSystemResourceProvider extends BaseOWLCodeSystemResourceProvider {
    private FhirContext fhirContext;
    HTTPRepository repository;

    public GraphDBCodeSystemResourceProvider(FhirContext fhirContext) {
        super(fhirContext);
        String repoUrl = System.getenv("GRAPHDB_REPO_URL");
        this.repository = new HTTPRepository(repoUrl);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return CodeSystem.class;
    }

    @Read
    public CodeSystem read(@IdParam IdType theId) {
        String id = theId.getIdPart();
        UrlValidator validator = new UrlValidator();
        String url;
        if (validator.isValid(id)) {  // id is url
            url = id;
            id = uriMap.inverse().get(url);
        } else {
            url = uriMap.get(id);
        }
        CodeSystem cs = getR4CodeSystem(url);
        if (cs == null) {
            throw new ResourceNotFoundException(theId);
        }
        cs.setName(id).setId(id);
        return cs;
    }

    public CodeSystem getR4CodeSystem(String uri) {
        CodeSystem codeSystem = createR4CodeSystem();
        RepositoryConnection connection = repository.getConnection();
        try {
            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,
                    "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                            "PREFIX obo: <http://purl.obolibrary.org/obo/>\n" +
                            "PREFIX bfo: <http://purl.obolibrary.org/obo/>\n" +
                            "select * where { \n" +
                            "obo:stato.owl ?p ?o .\n" +
                            "} limit 100 ");
            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
            while (tupleQueryResult.hasNext()) {
                BindingSet bindings = tupleQueryResult.next();
                String predicate = bindings.getValue("p").stringValue();
                String object = bindings.getValue("o").stringValue();
                switch (predicate) {
                    case "http://purl.org/dc/terms/license":
                        codeSystem.setCopyright(object);
                        break;
                    case "http://purl.org/dc/elements/1.1/description":
                        codeSystem.setDescription(object);
                        break;
                    case "http://purl.org/dc/elements/1.1/title":
                        codeSystem.setTitle(object);
                        break;
                    case "http://purl.org/dc/elements/1.1/subject":
                        codeSystem.setPurpose(object);
                        break;
                    default:
                        break;
                }
            }
            tupleQueryResult.close();
        } finally {
            connection.close();
        }
        codeSystem.setUrl(uri);
        return codeSystem;
    }


    @Operation(name = JpaConstants.OPERATION_LOOKUP, idempotent = true, returnParameters = {
            @OperationParam(name="name", type= StringType.class, min=1),
            @OperationParam(name="version", type=StringType.class, min=0),
            @OperationParam(name="display", type=StringType.class, min=1),
            @OperationParam(name="abstract", type= BooleanType.class, min=1)
    })
    public Parameters lookup(HttpServletRequest servletRequest,
                             @OperationParam(name = "code", min = 0, max = 1) CodeType code,
                             @OperationParam(name = "system", min = 0, max = 1) UriType system,
                             @OperationParam(name = "coding", min = 0, max = 1) Coding coding,
                             @OperationParam(name = "property", min = 0, max = OperationParam.MAX_UNLIMITED) List<CodeType> properties) {
        Parameters parameters = new Parameters();
        parameters.addParameter().setName("name").setValue(new CodeType("STATO"));
        IContextValidationSupport.LookupCodeResult result = new IContextValidationSupport.LookupCodeResult();
        RepositoryConnection connection = repository.getConnection();
        try {
            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,
            "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX obo: <http://purl.obolibrary.org/obo/>\n" +
                    "PREFIX bfo: <http://purl.obolibrary.org/obo/>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "select * where { \n" +
                    "<" + code.getCode() + ">" +
                    " ?p ?o .\n" +
                    "    FILTER (!isBlank(?o))\n" +
                    "} ");

            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
            boolean isRoot = false;
            boolean isImported = false;
            while (tupleQueryResult.hasNext()) {
                BindingSet bindings = tupleQueryResult.next();
                String predicate = bindings.getValue("p").stringValue();
                String object = bindings.getValue("o").stringValue();
                switch (predicate) {
                    case "http://purl.obolibrary.org/obo/IAO_0000115":
                        parameters.addParameter().setName("definition").setValue(new StringType(object));
                        break;
                    case "http://www.w3.org/2000/01/rdf-schema#label":
                        parameters.addParameter().setName("display").setValue(new StringType(object));
                        break;
                    case "http://www.w3.org/2000/01/rdf-schema#subClassOf":
                        addParameter(parameters,"parent", new CodeType(object));
                        if (object.equals("http://www.w3.org/2002/07/owl#Thing")) {
                            isRoot = true;
                        }
                        break;
                    case "http://purl.obolibrary.org/obo/STATO_0000041":
                        addParameter(parameters, "r_command", new StringType(object));
                        break;
                    case "http://purl.obolibrary.org/obo/IAO_0000412":
                        isImported = true;
                    default:
                        break;
                }
            }
            tupleQueryResult.close();
            addParameter(parameters, "root", new BooleanType(isRoot));
            addParameter(parameters, "deprecated", new BooleanType(false));
            addParameter(parameters, "imported", new BooleanType(isImported));
        } finally {
            connection.close();
        }
        return parameters;
    }

    private void addParameter(Parameters parameters, String code, Type value) {
        Parameters.ParametersParameterComponent codeParam = new Parameters.ParametersParameterComponent();
        codeParam.setName("code").setValue(new CodeType(code));
        Parameters.ParametersParameterComponent valueParam = new Parameters.ParametersParameterComponent();
        valueParam.setName("value").setValue(value);
        parameters.addParameter().setName("property").addPart(codeParam).addPart(valueParam);
    }


    public static void main(String[] args) {
        HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/stato");
        RepositoryConnection connection = repository.getConnection();
        try {
            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,
                    "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                            "PREFIX obo: <http://purl.obolibrary.org/obo/>\n" +
                            "PREFIX bfo: <http://purl.obolibrary.org/obo/>\n" +
                            "select * where { \n" +
                            "obo:stato.owl ?p ?o .\n" +
                            "} limit 100 ");
            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
            while (tupleQueryResult.hasNext()) {
                BindingSet bindings = tupleQueryResult.next();
                for (Binding binding: bindings) {
                    String name = binding.getName();
                    Value value = binding.getValue();
                    System.out.println(name + " = " + value);
                }
            }
            tupleQueryResult.close();
        } finally {
            connection.close();
        }
    }
}
