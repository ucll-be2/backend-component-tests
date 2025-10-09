package be.ucll.backend2.component;

import be.ucll.backend2.model.Actor;
import be.ucll.backend2.repository.DbInitializer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("classpath:schema.sql")
@ActiveProfiles("test")
public class ActorComponentTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private EntityManager em;

    @Autowired
    private DbInitializer dbInitializer;

    @BeforeEach
    public void addTestData() {
        dbInitializer.initialize();
    }

    @Test
    public void givenActorWithIdExists_whenDeleteActorIsCalled_thenActorIsDeleted() {
        client.delete()
                .uri("/api/v1/actors/{id}", 1L)
                .exchange()
                .expectStatus().isNoContent();

        final var actorInDB = em.find(Actor.class, 1L);
        Assertions.assertNull(actorInDB);
    }

    @Test
    public void givenActorWithIdDoesNotExist_whenDeleteActorIsCalled_then404IsReturned() {
        client.delete()
                .uri("/api/v1/actors/{id}", 100L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().json("""
                                   {
                                     "message": "Actor not found for id: 100"
                                   }
                                   """,
                        JsonCompareMode.STRICT);
    }

}
