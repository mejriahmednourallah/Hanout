package com.hanout;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTest {

    @Test
    void verifyModularity() {
        ApplicationModules modules = ApplicationModules.of(HanoutApplication.class);
        modules.verify();
    }
}
