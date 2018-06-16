package eu.wojciechzurek.example

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.test.StepVerifier

class IntegrationApplicationTest {

    private val client = WebTestClient.bindToServer().baseUrl("http://localhost:$PORT").build()

    @BeforeAll
    fun beforeAll() {
        app.run()
    }

    @Test
    fun `Get index HTML endpoint`() {
        client
                .get()
                .uri("/")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
    }

    @Test
    fun `Get hello endpoint`() {
        val result = client
                .get()
                .uri("/hello")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentType(MediaType.TEXT_PLAIN)
                .returnResult(String::class.java)

        StepVerifier
                .create(result.responseBody)
                .expectNext("Hello")
                .thenCancel()
                .verify()
    }

    @Test
    fun `Get hello event endpoint`() {
        val result = client
                .get()
                .uri("/hello-event")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String::class.java)

        StepVerifier
                .create(result.responseBody)
                .expectNext("Hello Mono Event")
                .thenCancel()
                .verify()
    }

    @Test
    fun `Get date endpoint`() {
        val result = client
                .get()
                .uri("/date")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String::class.java)

        StepVerifier
                .create(result.responseBody)
                .expectNextCount(5)
                .thenCancel()
                .verify()
    }

    @Test
    fun `Get all users endpoint`() {
        client
                .get()
                .uri("/api/user")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .expectBodyList<User>()
    }

    @Test
    fun `Get one user endpoint`() {
        val result = client
                .get()
                .uri("/api/user/5b24f75e5a86a170de98c1e7")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .returnResult(User::class.java)

        StepVerifier
                .create(result.responseBody)
                .expectNextMatches { it.login == "test" && it.age == 10 }
                .thenCancel()
                .verify()
    }

    @Test
    fun `Post new user endpoint`() {

        val user = User(login = "super-test", age = 99)

        val result = client
                .post()
                .uri("/api/user")
                .syncBody(user)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .expectHeader().exists(HttpHeaders.LOCATION)
                .returnResult(User::class.java)

        StepVerifier
                .create(result.responseBody)
                .expectNextMatches { it.login == user.login && it.age == user.age }
                .thenCancel()
                .verify()
    }

    @AfterAll
    fun afterAll() {
        app.stop()
    }
}