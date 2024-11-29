package org.tkit.onecx.parameters.test;

import static io.restassured.RestAssured.given;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.tkit.onecx.parameters.test.AbstractTest.createToken;
import static org.tkit.quarkus.security.test.SecurityTestUtils.*;
import static org.tkit.quarkus.security.test.SecurityTestUtils.removeClientScopes;

import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.mockserver.client.MockServerClient;
import org.mockserver.mock.Expectation;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.quarkus.security.test.AbstractSecurityTest;
import org.tkit.quarkus.security.test.SecurityTestConfig;

import gen.org.tkit.onecx.tenant.client.model.TenantId;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
public class SecurityTest extends AbstractSecurityTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Override
    public SecurityTestConfig getConfig() {
        SecurityTestConfig config = new SecurityTestConfig();
        config.addConfig("read", "/parameters/id", 404, List.of("ocx-pa:read"), "get");
        config.addConfig("write", "/parameters", 400, List.of("ocx-pa:write"), "post");
        return config;
    }

    @Override
    public void default_security_test(String client, List<String> scopes, Integer expectation, String url, String method) {

        var apm = createToken("org1");
        var exceptions = mockServerClient
                .when(request().withPath("/v1/tenant").withMethod(HttpMethod.GET).withHeader("apm-principal-token", apm))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new TenantId().tenantId("tenant-100"))));

        addClient(client, scopes);
        var givenClient = given()
                .contentType("application/json")
                .auth().oauth2(getKeycloakClientToken(client))
                .header("apm-principal-token", apm)
                .when();

        var defaultClient = given()
                .contentType("application/json")
                .auth().oauth2(getKeycloakClientToken("quarkus-app"))
                .header("apm-principal-token", apm)
                .when();

        if (method.equalsIgnoreCase("get")) {
            givenClient
                    .get(url)
                    .then().statusCode(expectation);

            //client with missing scope => forbidden
            defaultClient
                    .get(url)
                    .then().statusCode(403);

        } else if (method.equalsIgnoreCase("post")) {
            givenClient
                    .post(url)
                    .then().statusCode(expectation);

            //client with missing scope => forbidden
            defaultClient
                    .post(url)
                    .then().statusCode(403);

        } else if (method.equalsIgnoreCase("delete")) {
            givenClient
                    .delete(url)
                    .then().statusCode(expectation);

            //client with missing scope => forbidden
            defaultClient
                    .delete(url)
                    .then().statusCode(403);

        } else if (method.equalsIgnoreCase("put")) {
            givenClient
                    .put(url)
                    .then().statusCode(expectation);

            //client with missing scope => forbidden
            defaultClient
                    .put(url)
                    .then().statusCode(403);
        }
        for (Expectation e : List.of(exceptions)) {
            try {
                mockServerClient.clear(e.getId());
            } catch (Exception ex) {
                //  mockId not existing
            }
        }
        removeClient(client);
        removeClientScopes(scopes);
    }

}
