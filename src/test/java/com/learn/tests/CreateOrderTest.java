package com.learn.tests;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;

@RunWith(Parameterized.class)
public class CreateOrderTest {

    private final List<String> color;
    private Integer createdTrack;

    public CreateOrderTest(List<String> color) {
        this.color = color;
    }

    @Parameterized.Parameters(name = "Color = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { Collections.singletonList("BLACK") },
            { Collections.singletonList("GREY") },
            { Arrays.asList("BLACK", "GREY") },
            { Collections.emptyList() }
        });
    }

    @Before
    public void setup() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
        RestAssured.basePath = "/api/v1";
        createdTrack = null;
    }

    @Test
    @DisplayName("Create order with color variants")
    public void testCreateOrderWithColorVariants() {
        Map<String, Object> order = new HashMap<>();
        order.put("firstName", "Test");
        order.put("lastName", "Testov");
        order.put("address", "Testovaya ul., 1");
        order.put("metroStation", "4");
        order.put("phone", "+79991234567");
        order.put("rentTime", 3);
        order.put("deliveryDate", "2025-06-20");
        order.put("comment", "Please be gentle");
        order.put("color", color);

        Response response = given()
            .contentType(ContentType.JSON)
            .body(order)
            .when()
            .post("/orders");

        response.then()
            .statusCode(201)
            .body("track", notNullValue());

        createdTrack = response.jsonPath().getInt("track");
    }

    @After
    public void tearDown() {
        if (createdTrack != null) {
            given()
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("track", createdTrack))
                .when()
                .put("/orders/cancel")
                .then()
                .statusCode(anyOf(is(200), is(400))); // 400 — если уже отменён вручную
        }
    }

    @AfterClass
    public static void resetBasePath() {
        RestAssured.basePath = "";
    }
}