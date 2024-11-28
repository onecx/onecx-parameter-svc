package org.tkit.onecx.parameters.rs.internal;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import java.util.Map;
import java.util.stream.Stream;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.onecx.parameters.rs.internal.controllers.ApplicationParameterRestController;
import org.tkit.onecx.parameters.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.parameters.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ApplicationParameterRestController.class)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-pa:read", "ocx-pa:write", "ocx-pa:delete", "ocx-pa:all" })
class ApplicationParameterRestControllerTest extends AbstractTest {

    static final String PATH_PARAM_ID = "id";
    static final String PATH_PARAM_ID_PATH = "{" + PATH_PARAM_ID + "}";

    @Test
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldFindAllParametersWithoutCriteria() {
        ApplicationParameterPageResultDTO pageResultDTO = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(new ParameterSearchCriteriaDTO())
                .contentType(APPLICATION_JSON)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().body().as(ApplicationParameterPageResultDTO.class);

        Assertions.assertEquals(9, pageResultDTO.getStream().size());
        Assertions.assertEquals(Long.valueOf(9), pageResultDTO.getTotalElements());

    }

    @Test
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void searchAllApplicationsTest() {
        var output = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .get("applications")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ProductDTO[].class);
        Assertions.assertEquals(4, output.length);
    }

    static Stream<Arguments> findAllKeys() {
        return Stream.of(
                Arguments.of(Map.of(), 9),
                Arguments.of(Map.of("applicationId", "", "productName", ""), 9),
                Arguments.of(Map.of("applicationId", "app1", "productName", "p1"), 5));
    }

    @ParameterizedTest
    @MethodSource("findAllKeys")
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void searchAllKeysTest(Map<String, String> queryParams, int expectedArraySize) {
        var pageResultDTO = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .queryParams(queryParams)
                .get("keys")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(KeysPageResultDTO.class);
        Assertions.assertEquals(expectedArraySize, pageResultDTO.getStream().size());
    }

    static Stream<Arguments> findByCriteriaTestData() {
        return Stream.of(
                Arguments.of(new ParameterSearchCriteriaDTO().applicationId("").productName(""), 9),
                Arguments.of(new ParameterSearchCriteriaDTO().applicationId("access-mgmt").productName("access-mgmt-product"),
                        2),
                Arguments.of(new ParameterSearchCriteriaDTO().applicationId("incorrect_app").productName("incorrect-product"),
                        0),
                Arguments.of(new ParameterSearchCriteriaDTO().applicationId("incorrect_app").productName("incorrect-product")
                        .key("").name(""), 0),
                Arguments.of(new ParameterSearchCriteriaDTO().name("custom"), 0),
                Arguments.of(new ParameterSearchCriteriaDTO().key("ENGINE"), 1),
                Arguments.of(new ParameterSearchCriteriaDTO().key("incorrect_key"), 0));
    }

    @ParameterizedTest
    @MethodSource("findByCriteriaTestData")
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldFindParametersByCriteria(ParameterSearchCriteriaDTO criteriaDTO, Integer expectedArraySize) {
        ApplicationParameterPageResultDTO pageResultDTO = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .body(criteriaDTO)
                .contentType(APPLICATION_JSON)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ApplicationParameterPageResultDTO.class);
        Assertions.assertEquals(expectedArraySize, pageResultDTO.getStream().size());
    }

    @Test
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldFindParameterById() {
        ApplicationParameterDTO applicationParameterDTO = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .pathParam(PATH_PARAM_ID, "111")
                .get(PATH_PARAM_ID_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ApplicationParameterDTO.class);
        Assertions.assertNotNull(applicationParameterDTO);
        Assertions.assertEquals("access-mgmt", applicationParameterDTO.getApplicationId());
        Assertions.assertEquals("access-mgmt-product", applicationParameterDTO.getProductName());
        Assertions.assertEquals("ENGINE", applicationParameterDTO.getKey());
        Assertions.assertEquals("KOGITO", applicationParameterDTO.getSetValue());
        Assertions.assertEquals("Engine", applicationParameterDTO.getName());
        Assertions.assertNull(applicationParameterDTO.getDescription());
    }

    @Test
    void shouldNotFindParameterById() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .pathParam(PATH_PARAM_ID, "150")
                .get(PATH_PARAM_ID_PATH)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void shouldNotFindUpdateParameterById() {
        ApplicationParameterUpdateDTO applicationParameterUpdateDTO = new ApplicationParameterUpdateDTO();
        applicationParameterUpdateDTO.setValue("JBPM");
        applicationParameterUpdateDTO.setDescription("Test description");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(applicationParameterUpdateDTO)
                .pathParam(PATH_PARAM_ID, "150")
                .put(PATH_PARAM_ID_PATH)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    static Stream<Arguments> updateParameterTestInput() {
        return Stream.of(
                Arguments.of("access-mgmt", "Test description", "111", "JBPM", null, null, null, null),
                Arguments.of("access-mgmt", "Test description", "111", "JBPM", "", null, null, null),
                Arguments.of("access-mgmt", "Test description", "111", "JBPM", " ", null, null, null));
    }

    @ParameterizedTest
    @MethodSource("updateParameterTestInput")
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldUpdateParameterTest(String appId, String desc, String id, String value, String unit, Integer from, Integer to,
            String checkUnit) {
        var request = new ApplicationParameterUpdateDTO();
        request.setValue(value);
        request.setDescription(desc);
        request.setUnit(unit);
        request.setRangeTo(to);
        request.setRangeFrom(from);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(request)
                .contentType(APPLICATION_JSON)
                .when()
                .pathParam(PATH_PARAM_ID, id)
                .put(PATH_PARAM_ID_PATH)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .pathParam(PATH_PARAM_ID, id)
                .get(PATH_PARAM_ID_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(ApplicationParameterDTO.class);
        Assertions.assertNotNull(dto);
        Assertions.assertEquals(appId, dto.getApplicationId());
        Assertions.assertEquals(value, dto.getSetValue());
        Assertions.assertEquals(desc, dto.getDescription());
        Assertions.assertEquals(checkUnit, dto.getUnit());
        Assertions.assertEquals(from, dto.getRangeFrom());
        Assertions.assertEquals(to, dto.getRangeTo());
    }

    @Test
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void update_without_body_test() {

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .when()
                .pathParam(PATH_PARAM_ID, "GUID1")
                .put(PATH_PARAM_ID_PATH)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    static Stream<Arguments> createParameterTestInput() {
        return Stream.of(
                Arguments.of("app_10", "p10", "description", "key_10", "value_10", null, null, null, null),
                Arguments.of("app_10", "p10", "description", "key_11", "value_10", "", null, null, null),
                Arguments.of("app_10", "p10", "description", "key_12", "value_10", " ", null, null, null));
    }

    @ParameterizedTest
    @MethodSource("createParameterTestInput")
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void createParameterTest(String appId, String productName, String desc, String key, String value, String unit, Integer from,
            Integer to,
            String checkUnit) {
        ApplicationParameterCreateDTO dto = new ApplicationParameterCreateDTO();
        dto.setApplicationId(appId);
        dto.setProductName(productName);
        dto.setDescription(desc);
        dto.setKey(key);
        dto.setValue(value);
        dto.setUnit(unit);
        dto.setRangeFrom(from);
        dto.setRangeTo(to);

        String uri = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(dto)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().header(HttpHeaders.LOCATION);

        ApplicationParameterDTO dto2 = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get(uri)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(ApplicationParameterDTO.class);

        Assertions.assertNotNull(dto2);
        Assertions.assertEquals(dto.getApplicationId(), dto2.getApplicationId());
        Assertions.assertEquals(dto.getProductName(), dto2.getProductName());
        Assertions.assertEquals(dto.getDescription(), dto2.getDescription());
        Assertions.assertEquals(dto.getKey(), dto2.getKey());
        Assertions.assertEquals(dto.getValue(), dto2.getSetValue());
        Assertions.assertEquals(checkUnit, dto2.getUnit());
        Assertions.assertEquals(dto.getRangeFrom(), dto2.getRangeFrom());
        Assertions.assertEquals(dto.getRangeTo(), dto2.getRangeTo());

    }

    @Test
    void createTwice_Bad_Request_Test() {
        ApplicationParameterCreateDTO dto = new ApplicationParameterCreateDTO();
        dto.setApplicationId("app1");
        dto.setProductName("productName1");
        dto.setKey("key1");
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(dto)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(dto)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    static Stream<Arguments> deleteParameterTestInput() {
        return Stream.of(
                Arguments.of("GUID1"),
                Arguments.of("GUID2"),
                Arguments.of("GUID3"),
                Arguments.of("GUID4"),
                Arguments.of("GUID5"));
    }

    @ParameterizedTest
    @MethodSource("deleteParameterTestInput")
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void deleteParameterTest(String id) {

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam(PATH_PARAM_ID, id)
                .get(PATH_PARAM_ID_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam(PATH_PARAM_ID, id)
                .delete(PATH_PARAM_ID_PATH)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam(PATH_PARAM_ID, id)
                .get(PATH_PARAM_ID_PATH)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    void deleteNoneExistsParameterTest() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam(PATH_PARAM_ID, "NONE_EXISTS")
                .delete(PATH_PARAM_ID_PATH)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }
}
