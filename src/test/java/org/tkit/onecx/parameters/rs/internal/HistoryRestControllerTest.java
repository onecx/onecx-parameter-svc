package org.tkit.onecx.parameters.rs.internal;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import java.util.stream.Stream;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.parameters.rs.internal.controllers.HistoryRestController;
import org.tkit.onecx.parameters.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryCountCriteriaDTO;
import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryCriteriaDTO;
import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryDTO;
import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryPageResultDTO;
import gen.org.tkit.onecx.tenant.client.model.TenantId;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(HistoryRestController.class)
@WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-pa:read", "ocx-pa:write", "ocx-pa:delete", "ocx-pa:all" })
class HistoryRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @BeforeEach
    void resetExpectation() {
        clearExpectation(mockServerClient);
    }

    @Test
    void shouldFindAllParametersHistoryWithoutCriteria() {
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        var pageResultDTO = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .body(new HistoryCriteriaDTO())
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().body().as(HistoryPageResultDTO.class);

        Assertions.assertEquals(6, pageResultDTO.getStream().size());
        Assertions.assertEquals(Long.valueOf(6), pageResultDTO.getTotalElements());

    }

    static Stream<Arguments> findByCriteriaTestData() {
        return Stream.of(
                Arguments.of(new HistoryCriteriaDTO(), 6),
                Arguments.of(new HistoryCriteriaDTO().applicationId("abc").productName("").name(""), 0),
                Arguments.of(new HistoryCriteriaDTO().applicationId("app0").productName("p0").name("key0"), 0),
                Arguments.of(new HistoryCriteriaDTO().applicationId("access-mgmt")
                        .productName("access-mgmt-product"), 2),
                Arguments.of(new HistoryCriteriaDTO().applicationId("app0").productName("p0"), 0),
                Arguments.of(new HistoryCriteriaDTO().applicationId("app1").productName("p1"), 1),
                Arguments.of(new HistoryCriteriaDTO().applicationId("app2").productName("p2"), 3));
    }

    @ParameterizedTest
    @MethodSource("findByCriteriaTestData")
    void shouldFindParametersHistoryByCriteria(HistoryCriteriaDTO criteriaDTO, Integer expectedArraySize) {

        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        var pageResultDTO = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .body(criteriaDTO)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(HistoryPageResultDTO.class);
        Assertions.assertEquals(expectedArraySize, pageResultDTO.getStream().size());
    }

    static Stream<Arguments> findByCriteriaTestDataQueryLatest() {
        return Stream.of(
                Arguments.of(new HistoryCriteriaDTO(), 0),
                Arguments.of(new HistoryCriteriaDTO().applicationId("access-mgmt")
                        .productName("access-mgmt-product"), 0),
                Arguments.of(new HistoryCriteriaDTO().applicationId("").productName("").name(""), 0),
                Arguments.of(new HistoryCriteriaDTO().applicationId("").productName("").name("key1"), 0),
                Arguments.of(new HistoryCriteriaDTO().applicationId("").productName(""), 0),
                Arguments.of(new HistoryCriteriaDTO().applicationId("app0").productName("p0"), 0),
                Arguments.of(new HistoryCriteriaDTO().applicationId("app1").productName("p1"), 0),
                Arguments.of(new HistoryCriteriaDTO().applicationId("app2").productName("p2"), 0));
    }

    @ParameterizedTest
    @MethodSource("findByCriteriaTestDataQueryLatest")
    void shouldFindParametersHistoryByCriteriaQueryLatest(HistoryCriteriaDTO criteriaDTO,
            Integer expectedArraySize) {

        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        var pageResultDTO = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .body(criteriaDTO)
                .contentType(APPLICATION_JSON)
                .post("latest")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(HistoryPageResultDTO.class);
        Assertions.assertEquals(expectedArraySize, pageResultDTO.getStream().size());
    }

    @Test
    void getParametersHistoryByIdNoFoundTest() {
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .pathParam("id", "not-id")
                .get("{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    static Stream<Arguments> getParametersHistoryByIds() {
        return Stream.of(
                Arguments.of("1", "access-mgmt", "access-mgmt-product"),
                Arguments.of("2", "access-mgmt", "access-mgmt-product"),
                Arguments.of("h1", "app1", "p1"),
                Arguments.of("h2", "app2", "p2"));
    }

    @ParameterizedTest
    @MethodSource("getParametersHistoryByIds")
    void getParametersHistoryById(String id, String applicationId, String productName) {
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .pathParam("id", id)
                .get("{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(HistoryDTO.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(id, result.getId());
        Assertions.assertEquals(applicationId, result.getApplicationId());
        Assertions.assertEquals(productName, result.getProductName());

    }

    static Stream<Arguments> findCountByCriteriaTestData() {
        return Stream.of(
                Arguments.of(new HistoryCountCriteriaDTO(), 6),
                Arguments.of(new HistoryCountCriteriaDTO().applicationId("").productName("").name(""), 6),
                Arguments.of(new HistoryCountCriteriaDTO().applicationId("").productName("").name("key1"), 1),
                Arguments.of(
                        new HistoryCountCriteriaDTO().applicationId("access-mgmt").productName("access-mgmt-product"),
                        2),
                Arguments.of(new HistoryCountCriteriaDTO().applicationId("app0").productName("p0"), 0),
                Arguments.of(new HistoryCountCriteriaDTO().applicationId("app1").productName("p1"), 1),
                Arguments.of(new HistoryCountCriteriaDTO().applicationId("app2").productName("p2"), 3));
    }

    @ParameterizedTest
    @MethodSource("findCountByCriteriaTestData")
    void getCountsByCriteriaTest(HistoryCountCriteriaDTO criteria, Integer expectedArraySize) {
        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        var tmp = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .when()
                .body(criteria)
                .contentType(APPLICATION_JSON)
                .post("counts")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().jsonPath();

        Assertions.assertEquals(expectedArraySize, tmp.getList(".").size());
    }
}
