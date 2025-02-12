package org.tkit.onecx.parameters.domain.timer;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.parameters.domain.daos.HistoryDAO;
import org.tkit.onecx.parameters.domain.daos.JobDAO;
import org.tkit.onecx.parameters.domain.models.History;
import org.tkit.onecx.parameters.test.AbstractTest;
import org.tkit.quarkus.jpa.tenant.ContextTenantResolverConfig;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryCriteriaDTO;
import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryPageResultDTO;
import gen.org.tkit.onecx.tenant.client.model.TenantId;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;

@QuarkusTest
@WithDBData(value = { "data/history-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
class MaintenanceHistoryServiceTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Inject
    MaintenanceHistoryService service;

    @Inject
    HistoryDAO dao;

    @Inject
    JobDAO jobDAO;

    @InjectMock
    ContextTenantResolverConfig tenantConfigResolver;

    @Inject
    Config config;

    public static class ConfigProducer {

        @Inject
        Config config;

        @Produces
        @ApplicationScoped
        @Mock
        ContextTenantResolverConfig config() {
            return config.unwrap(SmallRyeConfig.class).getConfigMapping(ContextTenantResolverConfig.class);
        }
    }

    void mockConfig(boolean enableRoot) {
        var tmp = config.unwrap(SmallRyeConfig.class).getConfigMapping(ContextTenantResolverConfig.class);

        Mockito.when(tenantConfigResolver.defaultTenantValue()).thenReturn("tenant-100");
        Mockito.when(tenantConfigResolver.root()).thenReturn(new ContextTenantResolverConfig.RootConfig() {

            @Override
            public boolean enabled() {
                return enableRoot;
            }

            @Override
            public String value() {
                return tmp.root().value();
            }
        });
    }

    @Test
    @Order(1)
    @GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-pa:read", "ocx-pa:write", "ocx-pa:delete",
            "ocx-pa:all" })
    void maintenanceHistoryWithoutRootTenantDataTest() {
        mockConfig(false);
        service.maintenanceHistoryData();
        List<History> result = dao.findAll().toList();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        var pageResultDTO = given()
                .auth().oauth2(keycloakTestClient.getClientAccessToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .body(new HistoryCriteriaDTO())
                .contentType(APPLICATION_JSON)
                .post("/histories")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().body().as(HistoryPageResultDTO.class);

        Assertions.assertEquals(2, pageResultDTO.getStream().size());

        clearExpectation(mockServerClient);

        apm = createToken("org2");

        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-500")))));

        pageResultDTO = given()
                .auth().oauth2(keycloakTestClient.getClientAccessToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .body(new HistoryCriteriaDTO())
                .contentType(APPLICATION_JSON)
                .post("/histories")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().body().as(HistoryPageResultDTO.class);

        Assertions.assertEquals(1, pageResultDTO.getStream().size());

        clearExpectation(mockServerClient);

    }

    @Test
    @Order(2)
    @GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-pa:read", "ocx-pa:write", "ocx-pa:delete",
            "ocx-pa:all" })
    void maintenanceHistoryWithRootTenantDataTest() {
        mockConfig(true);
        service.maintenanceHistoryData();
        List<History> result = dao.findAll().toList();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        var apm = createToken("org1");
        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100")))));

        var pageResultDTO = given()
                .auth().oauth2(keycloakTestClient.getClientAccessToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .body(new HistoryCriteriaDTO())
                .contentType(APPLICATION_JSON)
                .post("/histories")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().body().as(HistoryPageResultDTO.class);

        Assertions.assertEquals(2, pageResultDTO.getStream().size());

        clearExpectation(mockServerClient);

        apm = createToken("org2");

        addExpectation(mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-500")))));

        pageResultDTO = given()
                .auth().oauth2(keycloakTestClient.getClientAccessToken("testClient"))
                .header(HEADER_APM_TOKEN, apm)
                .body(new HistoryCriteriaDTO())
                .contentType(APPLICATION_JSON)
                .post("/histories")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().body().as(HistoryPageResultDTO.class);

        Assertions.assertEquals(0, pageResultDTO.getStream().size());

        clearExpectation(mockServerClient);

    }

    @Test
    @Order(3)
    void maintenanceHistoryNoDataTest() {
        mockConfig(false);
        var result = dao.findAll().toList();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.size());

        jobDAO.deleteQueryById(MaintenanceHistoryService.JOB_ID);
        service.maintenanceHistoryData();

        result = dao.findAll().toList();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.size());
    }

}
