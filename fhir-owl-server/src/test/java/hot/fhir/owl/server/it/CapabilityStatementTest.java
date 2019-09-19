package hot.fhir.owl.server.it;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Category(Integration.class)
public class CapabilityStatementTest {
    @Test
    public void getMetadata_whenSuccessOnGetsResponseAndJsonHashRequiredKV_thenCorrect() {
        given().get("/fhirowl/metadata").then().body("resourceType", equalTo("CapabilityStatement"));
    }
}
