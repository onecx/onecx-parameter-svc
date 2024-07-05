package org.tkit.onecx.parameters.test;

import java.util.List;

import org.tkit.quarkus.security.test.AbstractSecurityTest;
import org.tkit.quarkus.security.test.SecurityTestConfig;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SecurityTest extends AbstractSecurityTest {
    @Override
    public SecurityTestConfig getConfig() {
        SecurityTestConfig config = new SecurityTestConfig();
        config.addConfig("read", "/parameters/id", 404, List.of("ocx-pa:read"), "get");
        config.addConfig("write", "/parameters", 400, List.of("ocx-pa:write"), "post");
        return config;
    }
}
