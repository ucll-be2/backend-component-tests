package be.ucll.backend2.component;

import be.ucll.backend2.model.Actor;
import be.ucll.backend2.repository.ActorRepository;
import be.ucll.backend2.repository.DbInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Sql("classpath:schema.sql") // schema.sql reset database bij elke test
public class ActorComponentTest {

    @Autowired
    private RestTestClient client; // Client om requests uit te voeren

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private DbInitializer dbInitializer;

    @BeforeEach
    public void addTestData() {
        // Zet voor elke test testdata in de DB
        dbInitializer.initialize();
    }

    @Test
    public void givenActorWithIdExists_whenDeleteActorIsCalled_thenActorIsDeleted() {
        // Kijk na dat we vooraf wél een acteur in de DB hebben
        final Optional<Actor> actorBefore = actorRepository.findById(1L);
        Assertions.assertTrue(actorBefore.isPresent());

        client.delete()
                .uri("/api/v1/actors/{id}", 1L)
                .exchange()
                .expectStatus().isNoContent();

        // Kijk na dat we nu geen acteur met ID 1 meer kunnen vinden
        final Optional<Actor> actorAfter = actorRepository.findById(1L);
        Assertions.assertTrue(actorAfter.isEmpty());
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
