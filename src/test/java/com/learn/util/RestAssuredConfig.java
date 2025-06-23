package com.learn.util;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public class RestAssuredConfig {
    private static final RequestSpecification BASE_SPEC = new RequestSpecBuilder()
        .setBaseUri("http://qa-scooter.praktikum-services.ru")
        .setBasePath("/api/v1")
        .build();

    public static RequestSpecification getBaseSpec() {
        return BASE_SPEC;
    }
}