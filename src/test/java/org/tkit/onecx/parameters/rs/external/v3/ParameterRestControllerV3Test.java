package org.tkit.onecx.parameters.rs.external.v3;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.parameters.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.parameters.rs.v3.ExternalApi;
import gen.org.tkit.onecx.parameters.rs.v3.model.ParameterInfoDTOV3;
import gen.org.tkit.onecx.parameters.rs.v3.model.ParametersBucketDTOV3;
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
                .pathParam("productName", "not-exist")
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
                .pathParam("productName", "import-product")
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
                .pathParam("productName", "access-mgmt-product")
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
                .pathParam("productName", "access-mgmt-product")
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
                .pathParam("productName", "new-product")
                .pathParam("appId", "new-application")
                .post("history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
        Map<String, String> applicationParameters = given()
                .when()
                .pathParam("productName", "new-product")
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
                .pathParam("productName", "access-mgmt-product")
                .pathParam("appId", "access-mgmt")
                .post("history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void bucketRequestEmptyDTO() {
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("productName", "test")
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
                .pathParam("productName", "test")
                .pathParam("appId", "test")
                .post("history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

}
