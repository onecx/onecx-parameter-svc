package org.tkit.parameters.rs.external.v3;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.parameters.rs.external.v3.models.ParameterInfoDTO;
import org.tkit.parameters.rs.external.v3.models.ParametersBucketDTO;
import org.tkit.parameters.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;

@QuarkusTest
@TestHTTPEndpoint(ParameterRestControllerV3.class)
class ParameterRestControllerV3Test extends AbstractTest {

    @Test
    void shouldNotFindParametersWithGivenApplicationId() {
        Map<String, String> applicationParameters = given()
                .when()
                .pathParam("appId", "not-exist")
                .get("{appId}/parameters")
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
                .get("{appId}/parameters")
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
                .get("{appId}/parameters")
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
                .get("{appId}/parameters")
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
        ParametersBucketDTO parametersBucketDTO = new ParametersBucketDTO();
        ParameterInfoDTO parameterInfoDTO1 = new ParameterInfoDTO();
        parameterInfoDTO1.setCount(1L);
        parameterInfoDTO1.setCurrentValue("DefaultValue");
        parameterInfoDTO1.setDefaultValue("DefaultValue");
        parameterInfoDTO1.setType("STRING");
        parametersBucketDTO.getParameters().put("testKey", parameterInfoDTO1);
        given()
                .contentType(APPLICATION_JSON)
                .body(parametersBucketDTO)
                .pathParam("appId", "new-application")
                .post("{appId}/history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
        Map<String, String> applicationParameters = given()
                .when()
                .pathParam("appId", "new-application")
                .get("{appId}/parameters")
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
        ParametersBucketDTO parametersBucketDTO = new ParametersBucketDTO();
        ParameterInfoDTO parameterInfoDTO1 = new ParameterInfoDTO();
        parameterInfoDTO1.setCount(2L);
        parameterInfoDTO1.setCurrentValue("10");
        parameterInfoDTO1.setDefaultValue("10");
        parameterInfoDTO1.setType("INTEGER");
        parametersBucketDTO.getParameters().put("COUNTER", parameterInfoDTO1);
        given()
                .contentType(APPLICATION_JSON)
                .body(parametersBucketDTO)
                .pathParam("appId", "access-mgmt")
                .post("{appId}/history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void bucketRequestEmptyDTO() {
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("appId", "test")
                .post("{appId}/history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void bucketRequestNoParametersDTO() {
        ParametersBucketDTO parametersBucketDTO = new ParametersBucketDTO();
        given()
                .contentType(APPLICATION_JSON)
                .body(parametersBucketDTO)
                .pathParam("appId", "test")
                .post("{appId}/history")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

}
