package hot.fhir.owl.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public abstract class Neo4jResourceProvider implements IResourceProvider, AutoCloseable {
    protected final Driver driver;
    protected FhirContext fhirContext;

    public Neo4jResourceProvider(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
        String url = System.getenv("NEO4J_URL");
        String username = System.getenv("NEO4J_USERNAME");
        String password = System.getenv("NEO4J_PASSWORD");
        if (username != null && password != null) {
            this.driver = GraphDatabase.driver(url, AuthTokens.basic(username, password));
        } else {
            this.driver = GraphDatabase.driver(url);
        }
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }
}
