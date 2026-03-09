package com.faust0z.BookLibraryAPI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("test")
class BookLibraryAPIApplicationTests extends BaseIntegrationTest {

    @Test
    void contextLoads() {
    }

}
