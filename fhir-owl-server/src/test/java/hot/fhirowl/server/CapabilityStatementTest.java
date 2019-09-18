package hot.fhirowl.server;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

public class CapabilityStatementTest {
    @Test
    public void getMetadata_whenSuccessOnGetsResponseAndJsonHashRequiredKV_thenCorrect() {
        get("/fhirowl/metadata").then().body("resourceType", equalTo("CapabilityStatement"));
    }
}
