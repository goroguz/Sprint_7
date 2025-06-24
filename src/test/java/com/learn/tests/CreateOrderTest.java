package com.learn.tests;

import com.learn.service.OrderService;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;

@RunWith(Parameterized.class)
public class CreateOrderTest {

    private final List<String> color;
    private Integer createdTrack;
    private final OrderService orderService = new OrderService();

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
        createdTrack = null;
    }

    @Test
    @DisplayName("Create order with color variants")
    @Description("Verifies that an order can be successfully created with different color options, including no color.")
    public void testCreateOrderWithColorVariantsTest() {
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

        Response response = orderService.createOrder(order);

        response.then()
            .statusCode(201)
            .body("track", notNullValue());

        createdTrack = response.jsonPath().getInt("track");
    }

    @After
    public void tearDown() {
        if (createdTrack != null) {
            orderService.getOrderTrack(createdTrack)
                .then()
                .statusCode(anyOf(is(200), is(400))); // 400 — if already canceled
        }
    }
}