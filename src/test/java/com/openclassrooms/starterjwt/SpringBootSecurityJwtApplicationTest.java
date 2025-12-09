package com.openclassrooms.starterjwt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SpringBootSecurityJwtApplicationTest {

    @Test
    void contextLoads() {
        // Le contexte Spring a démarré sans erreur.
    }
}
