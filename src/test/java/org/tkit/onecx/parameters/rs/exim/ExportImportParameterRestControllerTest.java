package org.tkit.onecx.parameters.rs.exim;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.HttpMethod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.parameters.rs.internal.controllers.ParameterRestController;
import org.tkit.onecx.parameters.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.parameters.rs.internal.model.*;
import gen.org.tkit.onecx.tenant.client.model.TenantId;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ParameterRestController.class)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-pa:read", "ocx-pa:write", "ocx-pa:all" })
public class ExportImportParameterRestControllerTest extends AbstractTest {
    @InjectMockServerClient
    MockServerClient mockServerClient;

    @BeforeEach
    void resetExpectation() {
        clearExpectation(mockServerClient);
    }

    @Test
    @WithDBData(value = { "data/parameters-eximdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void exportParametersTest() {
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));
        var request = new ExportParameterRequestDTO();

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ParameterSnapshotDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getProducts()).hasSize(2);

        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .body(new ExportParameterRequestDTO().productNames(null))
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ParameterSnapshotDTO.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getProducts()).hasSize(2);

        request.setProductNames(new HashSet<>());
        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ParameterSnapshotDTO.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getProducts()).hasSize(2);

        request.setProductNames(Set.of("import-product"));
        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ParameterSnapshotDTO.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getProducts()).hasSize(1);
    }

    @Test
    @WithDBData(value = { "data/parameters-eximdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void exportThemesWrongNamesTest() {

        var request = new ExportParameterRequestDTO();
        request.setProductNames(Set.of("does-not-exists"));
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    @WithDBData(value = { "data/parameters-eximdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void exportThemesEmptyBodyTest() {
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));
        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .post("/export")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");
        assertThat(exception.getDetail()).isEqualTo("exportParameters.exportParameterRequestDTO: must not be null");
    }

    @Test
    @WithDBData(value = { "data/parameters-eximdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void importParametersTest() {
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));
        var request = new ParameterSnapshotDTO();

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ParameterSnapshotDTO.class);
        assertThat(data).isNotNull();
        assertThat(data.getProducts()).hasSize(2);

        var importParameter = new EximParameterDTO();
        importParameter.setDescription("new theme description");
        importParameter.setName("test1");
        importParameter.setApplicationId("test1");
        importParameter.setProductName("test-product");
        data.getProducts().put("test-product", List.of(importParameter));
        // add new displayName to existing parameter
        data.getProducts().get("import-product2").get(0).setDisplayName("updatedDisplayName");

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .contentType(APPLICATION_JSON)
                .body(data)
                .post("/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportParameterResponseDTO.class);

        assertThat(dto.getParameters()).isNotNull().hasSize(4);
        assertThat(dto.getParameters().get("importParam2")).returns(ImportParameterResponseStatusDTO.UPDATE.toString(),
                from(ImportParameterResponseStatusDTO::toString));
        assertThat(dto.getParameters().get("test1")).returns(ImportParameterResponseStatusDTO.CREATED.toString(),
                from(ImportParameterResponseStatusDTO::toString));
    }
}
