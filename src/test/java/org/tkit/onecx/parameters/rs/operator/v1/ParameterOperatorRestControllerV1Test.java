package org.tkit.onecx.parameters.rs.operator.v1;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.parameters.rs.operator.v1.controllers.OperatorParameterRestControllerV1;
import org.tkit.onecx.parameters.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.parameters.rs.v1.operator.model.ParameterUpdateRequestOperatorDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.operator.model.ParametersUpdateRequestOperatorDTOV1;
import gen.org.tkit.onecx.tenant.client.model.TenantId;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(OperatorParameterRestControllerV1.class)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-pa-op:write" })
class ParameterOperatorRestControllerV1Test extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @BeforeEach
    void resetExpectation() {
        clearExpectation(mockServerClient);
    }

    @Test
    @WithDBData(value = { "data/operator-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldUpdateParameterTest() {
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        var requestBody = new ParametersUpdateRequestOperatorDTOV1()
                .addParametersItem(new ParameterUpdateRequestOperatorDTOV1().name("name1").value("value1"));

        given()
                .auth().oauth2(keycloakTestClient.getClientAccessToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("productName", "product1")
                .pathParam("applicationId", "app1")
                .body(requestBody)
                .put()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @WithDBData(value = { "data/operator-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldCreateParameterTest() {
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        var requestBody = new ParametersUpdateRequestOperatorDTOV1()
                .addParametersItem(new ParameterUpdateRequestOperatorDTOV1().name("name2").value("value2"))
                .addParametersItem(new ParameterUpdateRequestOperatorDTOV1().name("name3").value("value3"));

        given()
                .auth().oauth2(keycloakTestClient.getClientAccessToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("productName", "product2")
                .pathParam("applicationId", "app2")
                .body(requestBody)
                .put()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @WithDBData(value = { "data/operator-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void constraintViolationMissingFieldsTest() {
        keycloakTestClient.getClientAccessToken("testClient");
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        var requestBody = new ParametersUpdateRequestOperatorDTOV1()
                .addParametersItem(new ParameterUpdateRequestOperatorDTOV1().value("value2"));

        given()
                .auth().oauth2(keycloakTestClient.getClientAccessToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("productName", "product2")
                .pathParam("applicationId", "app2")
                .body(requestBody)
                .put()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        requestBody = new ParametersUpdateRequestOperatorDTOV1();

        given()
                .auth().oauth2(keycloakTestClient.getClientAccessToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("productName", "product2")
                .pathParam("applicationId", "app2")
                .body(requestBody)
                .put()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
