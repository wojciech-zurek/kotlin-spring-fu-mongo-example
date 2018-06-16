package eu.wojciechzurek.example

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.core.env.get
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.fu.application
import org.springframework.fu.module.data.mongodb.mongodb
import org.springframework.fu.module.jackson.jackson
import org.springframework.fu.module.logging.*
import org.springframework.fu.module.mustache.mustache
import org.springframework.fu.module.webflux.netty.netty
import org.springframework.fu.module.webflux.routes
import org.springframework.fu.module.webflux.server.handler
import org.springframework.fu.module.webflux.webflux
import org.springframework.fu.ref
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.io.File
import java.net.URI
import java.time.Duration

const val PORT: Int = 8080
const val MONGO_DB_URI: String = "mongodb://localhost/spring-fu-test"

fun main(args: Array<String>) = app.run(await = true)

val app = application {
    bean<UserRepository>()
    bean<SimpleHandler>()
    bean<UserHandler>()
    bean<InitRunner>()

//    listener<ContextRefreshedEvent> {
//        ref<UserRepository>().init()
//    }

    logging {
        level(LogLevel.INFO)
        level("org.springframework", LogLevel.DEBUG)
        level<DefaultListableBeanFactory>(LogLevel.WARN)

        logback {
            debug(true)
            consoleAppender()
            rollingFileAppender(File(System.getProperty("java.io.tmpdir"), "log.txt"))
        }
    }

    webflux {
        server(netty(PORT)) {
            mustache()
            codecs { jackson() }
            routes(ref = ::routes)
        }
    }

    mongodb(env["MONGO_DB_URI"] ?: MONGO_DB_URI)
}

fun routes() = routes {
    val simpleHandler = ref<SimpleHandler>()
    val userHandler = ref<UserHandler>()

    GET("/", ref = simpleHandler::main)
    GET("/hello", ref = simpleHandler::hello)
    GET("/hello-event", ref = simpleHandler::helloEvent)
    GET("/date", ref = simpleHandler::date)

    GET("/api/user", userHandler::findAll)
    POST("/api/user", userHandler::new)
    GET("/api/user/{id}", userHandler::findById)
    PUT("/api/user/{id}", userHandler::update)
    DELETE("/api/user/{id}", userHandler::delete)
}

class InitRunner(private val userRepository: UserRepository) : InitializingBean {
    override fun afterPropertiesSet() = userRepository.init()
}

class SimpleHandler {
    fun main(request: ServerRequest) = handler { ok().render("index") }

    fun hello(request: ServerRequest) = handler { ok().contentType(MediaType.TEXT_PLAIN).syncBody("Hello") }

    fun helloEvent(request: ServerRequest) = handler { ok().contentType(MediaType.TEXT_EVENT_STREAM).body(Mono.just("Hello Mono Event")) }

    fun date(request: ServerRequest) = handler { ok().contentType(MediaType.TEXT_EVENT_STREAM).body(fluxSink()) }

    private fun fluxSink() = Flux.create { sink: FluxSink<Long> ->
        repeat(5) {
            sink.next(System.currentTimeMillis())
            Thread.sleep(Duration.ofSeconds(1).toMillis())
        }
        sink.complete()
    }
}

class UserHandler(private val userRepository: UserRepository) {
    fun findAll(request: ServerRequest) = handler { ok().body(userRepository.findAll()) }

    fun findById(request: ServerRequest) = handler {
        userRepository
                .findById(request.pathVariable("id"))
                .flatMap { ok().syncBody(it) }
    }
            .switchIfEmpty(notFound().build())

    fun new(request: ServerRequest) = handler {
        request
                .bodyToMono<UserRequest>()
                .map { User(login = it.login, age = it.age) }
                .flatMap { userRepository.save(it) }
                .flatMap { created(URI.create("/api/user/${it.id}")).syncBody(it) }

    }

    fun update(request: ServerRequest) = handler {

        request
                .bodyToMono<UserRequest>()
                .zipWith(userRepository.findById(request.pathVariable("id")))
                .map { User(it.t2.id, it.t1.login, it.t1.age) }
                .flatMap { userRepository.save(it) }
                .flatMap { ok().syncBody(it) }
                .switchIfEmpty(notFound().build())
    }

    fun delete(request: ServerRequest) = handler {
        userRepository
                .findById(request.pathVariable("id"))
                .flatMap { userRepository.delete(it).then(noContent().build()) }
                .switchIfEmpty(notFound().build())
    }
}


data class UserRequest(
        val login: String,
        val age: Int
)

data class User(
        @Id val id: String? = null,
        val login: String,
        val age: Int
)

class UserRepository(private val reactiveMongoTemplate: ReactiveMongoTemplate) {


    fun init() {
        reactiveMongoTemplate
                .dropCollection<User>()
                .thenMany(Flux.just(
                        User("5b24f75e5a86a170de98c1e7", "test", 10),
                        User("5b2558319be7a1320e574b1d", "ala", 18),
                        User("5b255fe19be7a138f539c02c", "admin", 60)
                ))
                .flatMap {
                    reactiveMongoTemplate.save(it)
                }
                .thenMany(reactiveMongoTemplate.findAll<User>())
                .subscribe {
                    println(it)
                }
    }

    fun findAll() = reactiveMongoTemplate.findAll<User>()

    fun findById(id: String) = reactiveMongoTemplate.findById<User>(id)

    fun save(user: User) = reactiveMongoTemplate.save(user)

    fun delete(user: User) = reactiveMongoTemplate.remove(user)
}
