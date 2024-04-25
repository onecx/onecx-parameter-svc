package org.tkit.onecx.parameters.rs.external.v2;

import static io.restassured.RestAssured.given;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.onecx.parameters.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ParameterRestControllerV2.class)
class ParameterRestControllerV2Test extends AbstractTest {

    static Stream<Arguments> badQueryParameters() {
        return Stream.of(
                Arguments.of(Map.of()),
                Arguments.of(Map.of("applicationId", "")),
                Arguments.of(Map.of("applicationId", "", "parameterKey", "")),
                Arguments.of(Map.of("applicationId", "1")),
                Arguments.of(Map.of("applicationId", "1", "parameterKey", "")),
                Arguments.of(Map.of("parameterKey", "")),
                Arguments.of(Map.of("parameterKey", "1")),
                Arguments.of(Map.of("parameterKey", "1", "applicationId", "")));
    }

    @ParameterizedTest
    @MethodSource("badQueryParameters")
    void getParameterBadRequest(Map<String, String> parameters) {
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParams(parameters)
                .get("parameters")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    static Stream<Arguments> badQueryAllParameters() {
        return Stream.of(
                Arguments.of(Map.of(), null),
                Arguments.of(Map.of(), List.of()),
                Arguments.of(Map.of("applicationId", ""), null),
                Arguments.of(Map.of("applicationId", ""), List.of()),
                Arguments.of(Map.of("applicationId", ""), List.of("x")),
                Arguments.of(Map.of("applicationId", "1"), null),
                Arguments.of(Map.of("applicationId", "1"), List.of()));
    }

    @ParameterizedTest
    @MethodSource("badQueryAllParameters")
    void getAllParameterBadRequest(Map<String, String> parameters, List<String> body) {
        var tmp = given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParams(parameters);

        if (body != null) {
            tmp.body(body);
        }

        tmp.post("parameters")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    @WithDBData(value = {
            "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true, deleteAfterTest = true)
    void testParametersNotFound() {
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("applicationId", "applicationId")
                .queryParam("parameterKey", "parameterKey")
                .get("parameters")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @WithDBData(value = {
            "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true, deleteAfterTest = true)
    void getParameterData() {
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("applicationId", "app1")
                .queryParam("parameterKey", "param")
                .get("parameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @WithDBData(value = {
            "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true, deleteAfterTest = true)
    void getAllParametersData() {
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("applicationId", "app1")
                .body(List.of("param"))
                .post("parameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    static Stream<Arguments> badQueryStringParameters() {
        return Stream.of(
                Arguments.of(Map.of()),
                Arguments.of(Map.of("applicationId", "")),
                Arguments.of(Map.of("applicationId", "", "parameterKey", "")),
                Arguments.of(Map.of("applicationId", "1")),
                Arguments.of(Map.of("applicationId", "1", "parameterKey", "")),
                Arguments.of(Map.of("parameterKey", "")),
                Arguments.of(Map.of("parameterKey", "1")),
                Arguments.of(Map.of("parameterKey", "1", "applicationId", "")));
    }

    @ParameterizedTest
    @MethodSource("badQueryStringParameters")
    void getParameterStringDataBadRequest(Map<String, String> queryParams) {
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParams(queryParams)
                .get("stringParameters")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getParameterStringDataNotFound() {
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("applicationId", "app3")
                .queryParam("parameterKey", "string1")
                .get("stringParameters")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @WithDBData(value = {
            "data/parameters-testdata-v2.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true, deleteAfterTest = true)
    void getParameterStringData() {
        var tmp = given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("applicationId", "app3")
                .queryParam("parameterKey", "string")
                .get("stringParameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().jsonPath().getList(".").get(0);
        Assertions.assertEquals("string-value", tmp);
    }

    @Test
    @WithDBData(value = {
            "data/parameters-testdata-v2.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true, deleteAfterTest = true)
    void getParameterLongData() {
        var tmp = given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("applicationId", "app3")
                .queryParam("parameterKey", "long")
                .get("longParameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().jsonPath().getList(".").get(0);
        Assertions.assertEquals(2, tmp);
    }

    @Test
    @WithDBData(value = {
            "data/parameters-testdata-v2.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true, deleteAfterTest = true)
    void getParameterIntegerData() {
        var tmp = given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("applicationId", "app3")
                .queryParam("parameterKey", "integer")
                .get("integerParameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().jsonPath().getList(".").get(0);
        Assertions.assertEquals(2, tmp);
    }

    @Test
    @WithDBData(value = {
            "data/parameters-testdata-v2.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true, deleteAfterTest = true)
    void getParameterBooleanData() {
        var tmp = given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("applicationId", "app3")
                .queryParam("parameterKey", "boolean")
                .get("booleanParameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().jsonPath().getList(".").get(0);
        Assertions.assertEquals(true, tmp);
    }

    @Test
    @WithDBData(value = {
            "data/parameters-testdata-v2.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true, deleteAfterTest = true)
    void getParameterWrongTypeData() {
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("applicationId", "app3")
                .queryParam("parameterKey", "type")
                .get("integerParameters")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
