package com.learn.tests;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;

public class GetOrdersTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Get orders returns a list of orders")
    public void testGetOrdersReturnsListTest() {
        Response response = given()
            .when()
            .get("/api/v1/orders")
            .then()
            .statusCode(200) // Проверяем, что код ответа 200
            .body("orders", is(notNullValue())) // Проверяем, что поле orders существует
            .body("orders", is(not(empty()))) // И оно не пустое
            .extract().response();

        // Дополнительно: вывести в консоль, если нужно
        System.out.println("Orders response: " + response.asString());
    }
}