package org.tkit.parameters.rs.internal;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Map;
import java.util.stream.Stream;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.parameters.rs.internal.dtos.ApplicationParameterHistoryDTO;
import org.tkit.parameters.rs.internal.dtos.PageResultDTO;
import org.tkit.parameters.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ApplicationParameterHistoryRestController.class)
@WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
class ApplicationParameterHistoryRestControllerTest extends AbstractTest {

    @Test
    void shouldFindAllParametersHistoryWithoutCriteria() {
        var pageResultDTO = given()
                .get()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().body().as(PageResultDTO.class);

        Assertions.assertEquals(6, pageResultDTO.getStream().size());
        Assertions.assertEquals(Long.valueOf(6), pageResultDTO.getTotalElements());

    }

    static Stream<Arguments> findByCriteriaTestData() {
        return Stream.of(
                Arguments.of(Map.of(), 6),
                Arguments.of(Map.of("applicationId", "", "key", "", "type", ""), 0),
                Arguments.of(Map.of("applicationId", "app0", "key", "key0", "type", "type0"), 0),
                Arguments.of(Map.of("applicationId", "access-mgmt"), 2),
                Arguments.of(Map.of("applicationId", "app0"), 0),
                Arguments.of(Map.of("applicationId", "app1"), 1),
                Arguments.of(Map.of("applicationId", "app2"), 3));
    }

    @ParameterizedTest
    @MethodSource("findByCriteriaTestData")
    void shouldFindParametersHistoryByCriteria(Map<String, String> queryParams, Integer expectedArraySize) {
        var pageResultDTO = given()
                .when()
                .queryParams(queryParams)
                .get()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(PageResultDTO.class);
        Assertions.assertEquals(expectedArraySize, pageResultDTO.getStream().size());
    }

    static Stream<Arguments> findByCriteriaTestDataQueryLatest() {
        return Stream.of(
                Arguments.of(Map.of(), 0),
                Arguments.of(Map.of("applicationId", "access-mgmt"), 0),
                Arguments.of(Map.of("applicationId", "", "key", ""), 0),
                Arguments.of(Map.of("applicationId", "", "key", "key1"), 0),
                Arguments.of(Map.of("applicationId", ""), 0),
                Arguments.of(Map.of("applicationId", "app0"), 0),
                Arguments.of(Map.of("applicationId", "app1"), 0),
                Arguments.of(Map.of("applicationId", "app2"), 0));
    }

    @ParameterizedTest
    @MethodSource("findByCriteriaTestDataQueryLatest")
    void shouldFindParametersHistoryByCriteriaQueryLatest(Map<String, String> queryParams, Integer expectedArraySize) {
        var pageResultDTO = given()
                .when()
                .queryParams(queryParams)
                .get("latest")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(PageResultDTO.class);
        Assertions.assertEquals(expectedArraySize, pageResultDTO.getStream().size());
    }

    @Test
    void getApplicationParametersHistoryByIdNoFoundTest() {
        given()
                .when()
                .pathParam("id", "not-id")
                .get("{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    static Stream<Arguments> getApplicationParametersHistoryByIds() {
        return Stream.of(
                Arguments.of("1", "access-mgmt"),
                Arguments.of("2", "access-mgmt"),
                Arguments.of("h1", "app1"),
                Arguments.of("h2", "app2"));
    }

    @ParameterizedTest
    @MethodSource("getApplicationParametersHistoryByIds")
    void getApplicationParametersHistoryById(String id, String applicationId) {
        var result = given()
                .when()
                .pathParam("id", id)
                .get("{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ApplicationParameterHistoryDTO.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(id, result.getId());
        Assertions.assertEquals(applicationId, result.getApplicationId());
    }

    static Stream<Arguments> findCountByCriteriaTestData() {
        return Stream.of(
                Arguments.of(Map.of(), 6),
                Arguments.of(Map.of("applicationId", "", "key", ""), 6),
                Arguments.of(Map.of("applicationId", "", "key", "key1"), 1),
                Arguments.of(Map.of("applicationId", "access-mgmt"), 2),
                Arguments.of(Map.of("applicationId", "app0"), 0),
                Arguments.of(Map.of("applicationId", "app1"), 1),
                Arguments.of(Map.of("applicationId", "app2"), 3));
    }

    @ParameterizedTest
    @MethodSource("findCountByCriteriaTestData")
    void getCountsByCriteriaTest(Map<String, String> queryParams, Integer expectedArraySize) {
        var tmp = given()
                .when()
                .queryParams(queryParams)
                .get("counts")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().jsonPath();

        Assertions.assertEquals(expectedArraySize, tmp.getList(".").size());
    }
}
