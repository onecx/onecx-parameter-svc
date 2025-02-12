package org.tkit.onecx.parameters.test;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import org.eclipse.microprofile.jwt.Claims;
import org.mockserver.client.MockServerClient;
import org.mockserver.mock.Expectation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.util.KeyUtils;

@QuarkusTestResource(MockServerTestResource.class)
public abstract class AbstractTest {

    private static final List<String> MOCK_IDS = new ArrayList<>();

    protected static final String HEADER_APM_TOKEN = "apm-principal-token";

    protected KeycloakTestClient keycloakTestClient = new KeycloakTestClient();

    static {
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(
                        (cls, charset) -> {
                            ObjectMapper objectMapper = new ObjectMapper();
                            objectMapper.registerModule(new JavaTimeModule());
                            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                            return objectMapper;
                        }));
    }

    protected void addExpectation(Expectation[] exceptions) {
        for (Expectation e : List.of(exceptions)) {
            MOCK_IDS.add(e.getId());
        }

    }

    protected void clearExpectation(MockServerClient client) {
        MOCK_IDS.forEach(x -> {
            try {
                client.clear(x);
            } catch (Exception ex) {
                //  mockId not existing
            }
        });
        MOCK_IDS.clear();
    }

    public static String createToken(String orgId) {
        JsonObjectBuilder claims = Json.createObjectBuilder();
        claims.add(Claims.preferred_username.name(), "test");
        claims.add(Claims.sub.name(), "test");
        claims.add("orgId", orgId);
        return Jwt.claims(claims.build()).sign(KEY);
    }

    static final PrivateKey KEY = createKey();

    static PrivateKey createKey() {
        try {
            return KeyUtils.generateKeyPair(2048).getPrivate();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Error generate private key", ex);
        }
    }

}
