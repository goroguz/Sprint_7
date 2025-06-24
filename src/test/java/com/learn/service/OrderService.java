package com.learn.service;

import com.learn.util.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Collections;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class OrderService {
    public Response createOrder(Map<String, Object> order) {
        return given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(order)
            .when()
            .post("/orders");
    }

    public Response getOrderTrack(Integer createdTrack) {
        return given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(Collections.singletonMap("track", createdTrack))
            .when()
            .put("/orders/cancel");
    }

    public Response getOrders() {
        return given()
            .when()
            .get("/api/v1/orders");
    }
}
