# Kotlin + Reactive Spring Fu (Functional) + Reactive Mongo DB example

More info: [https://github.com/spring-projects/spring-fu](https://github.com/spring-projects/spring-fu)

## Status

[![Build Status](https://travis-ci.org/wojciech-zurek/kotlin-spring-fu-mongo-example.svg?branch=master)](https://travis-ci.org/wojciech-zurek/kotlin-spring-fu-mongo-example)

## Endpoints

Server: [http://localhost:8080](http://localhost:8080)

```kotlin
    GET("/", simpleHandler::main)
    GET("/hello", simpleHandler::hello)
    GET("/hello-event", simpleHandler::helloEvent)
    GET("/date", simpleHandler::date)

    GET("/api/user", userHandler::findAll)
    POST("/api/user", userHandler::new)
    GET("/api/user/{id}", userHandler::findById)
    PUT("/api/user/{id}", userHandler::update)
    DELETE("/api/user/{id}", userHandler::delete)
```

## Download

```bash
    git clone https://github.com/wojciech-zurek/kotlin-spring-fu-mongo-example.git
```

## Run with gradle

```bash
    cd kotlin-spring-fu-mongo-example/
    ./gradlew run
```

## Run as jar file

```bash
    cd kotlin-spring-fu-mongo-example/
    ./gradlew shadowJar
    java -jar build/libs/kotlin-spring-fu-mongo-example-all.jar
```

## Test

```bash
    cd kotlin-spring-fu-mongo-example/
    ./gradlew cleanTest test
```