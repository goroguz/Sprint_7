package com.learn.tests;

import com.learn.service.OrderService;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class GetOrdersTest {
    private final OrderService orderService = new OrderService();

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Get orders returns a list of orders")
    @Description("Verifies that the GET /api/v1/orders endpoint returns a non-empty list of orders and status code 200.")
    public void testGetOrdersReturnsListTest() {
        Response response = orderService.getOrders()
            .then()
            .statusCode(200) // Ensure status is 200 OK
            .body("orders", is(notNullValue())) // Ensure 'orders' field exists
            .body("orders", is(not(empty())))   // Ensure it's not empty
            .extract().response();

        System.out.println("Orders response: " + response.asString());
    }
}