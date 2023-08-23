package io.github.onecx.parameters.rs.external.v3;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.parameters.rs.v3.ExternalApi;
import gen.io.github.onecx.parameters.rs.v3.model.ParameterInfoDTOV3;
import gen.io.github.onecx.parameters.rs.v3.model.ParametersBucketDTOV3;
import io.github.onecx.parameters.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;

@QuarkusTest
@TestHTTPEndpoint(ExternalApi.class)
class ParameterRestControllerV3Test extends AbstractTest {

    @Test
    void shouldNotFindParametersWithGivenApplicationId() {
        Map<String, String> applicationParameters = given()
                .when()
                .pathParam("appId", "not-exist")
                .get("parameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().jsonPath().getMap(".", String.class, String.class);
        Assertions.assertTrue(applicationParameters.isEmpty());
    }

    @Test
    @WithDBData(value = { "data/parameters-importdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldReturnImportValueParameter() {
        Map<String, String> applicationParameters = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("appId", "import-app")
                .get("parameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().jsonPath().getMap(".", String.class, String.class);
        Assertions.assertEquals(1, applicationParameters.size());
        Assertions.assertEquals("import-value", applicationParameters.get("importParam"));
    }

    @Test
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldReturnParameter() {
        Map<String, String> applicationParameters = given()
                .when()
                .pathParam("appId", "access-mgmt")
                .get("parameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().jsonPath().getMap(".", String.class, String.class);
        Assertions.assertEquals(1, applicationParameters.size());
        Assertions.assertEquals("KOGITO", applicationParameters.get("ENGINE"));
    }

    @Test
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldNotReturnParameterWithNullSetValue() {
        JsonPath applicationParameters = given()
                .when()
                .pathParam("appId", "access-mgmt")
                .get("parameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath();
        Assertions.assertNull(applicationParameters.get("COUNTER"));
    }

    @Test
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldCreateNewParameter() {
        ParametersBucketDTOV3 parametersBucketDTO = new ParametersBucketDTOV3();
        ParameterInfoDTOV3 parameterInfoDTO1 = new ParameterInfoDTOV3();
        parameterInfoDTO1.setCount(1L);
        parameterInfoDTO1.setCurrentValue("DefaultValue");
        parameterInfoDTO1.setDefaultValue("DefaultValue");
        parameterInfoDTO1.setType("STRING");
        parametersBucketDTO.getParameters().put("testKey", parameterInfoDTO1);
        given()
                .contentType(APPLICATION_JSON)
                .body(parametersBucketDTO)
                .pathParam("appId", "new-application")
                .post("history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
        Map<String, String> applicationParameters = given()
                .when()
                .pathParam("appId", "new-application")
                .get("parameters")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().jsonPath().getMap(".", String.class, String.class);
        Assertions.assertEquals(0, applicationParameters.size());
    }

    @Test
    @WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void shouldUpdateParameters() {
        ParametersBucketDTOV3 parametersBucketDTO = new ParametersBucketDTOV3();
        ParameterInfoDTOV3 parameterInfoDTO1 = new ParameterInfoDTOV3();
        parameterInfoDTO1.setCount(2L);
        parameterInfoDTO1.setCurrentValue("10");
        parameterInfoDTO1.setDefaultValue("10");
        parameterInfoDTO1.setType("INTEGER");
        parametersBucketDTO.getParameters().put("COUNTER", parameterInfoDTO1);
        given()
                .contentType(APPLICATION_JSON)
                .body(parametersBucketDTO)
                .pathParam("appId", "access-mgmt")
                .post("history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void bucketRequestEmptyDTO() {
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("appId", "test")
                .post("history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void bucketRequestNoParametersDTO() {
        ParametersBucketDTOV3 parametersBucketDTO = new ParametersBucketDTOV3();
        given()
                .contentType(APPLICATION_JSON)
                .body(parametersBucketDTO)
                .pathParam("appId", "test")
                .post("history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

}
