package org.tkit.onecx.parameters.rs.bff.v1;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.parameters.rs.bff.v1.controllers.ParameterRestControllerBffV1;
import org.tkit.onecx.parameters.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParametersBulkRequestBffDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParametersBulkResponseBffDTOV1;
import gen.org.tkit.onecx.tenant.client.model.TenantId;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ParameterRestControllerBffV1.class)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-pa-ext:read" })
@WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
class ParameterRestControllerBffV1Test extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @BeforeEach
    void resetExpectation() {
        clearExpectation(mockServerClient);
    }

    @Test
    void shouldGetParamatersByProductNamesAndAppIds() {
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        ParametersBulkRequestBffDTOV1 request = new ParametersBulkRequestBffDTOV1();

        given()
                .auth().oauth2(keycloakTestClient.getClientAccessToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        var data = new HashMap<>(Map.of("p1", Set.of("app1")));
        data.put("access-mgmt-product", Set.of("access-mgmt"));
        request.setProducts(data);

        var res = given()
                .auth().oauth2(keycloakTestClient.getClientAccessToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .body(request)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().body().as(ParametersBulkResponseBffDTOV1.class);

        Assertions.assertEquals(2, res.getProducts().size());
        Assertions.assertEquals(1, res.getProducts().get("p1").size());
        Assertions.assertEquals(5, res.getProducts().get("p1").get("app1").size());
        Assertions.assertEquals(1, res.getProducts().get("access-mgmt-product").size());
        Assertions.assertEquals(2, res.getProducts().get("access-mgmt-product").get("access-mgmt").size());
    }
}
