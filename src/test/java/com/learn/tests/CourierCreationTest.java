package com.learn.tests;

import com.learn.model.CourierModel;
import com.learn.service.CourierClientService;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.UUID;

import static org.hamcrest.Matchers.*;

@RunWith(JUnit4.class)
public class CourierCreationTest {

    private static final String PASSWORD  = "1234";
    private static final String FIRST_NAME = "John";
    private static final String NOT_ENOUGHT_DATA_TO_CREATE_ACOCUNT = "Недостаточно данных для создания учетной записи";
    private final CourierClientService courierClient = new CourierClientService();
    private String login;

    @Before
    public void setup() {
        login = "courier_creation_" + UUID.randomUUID();
    }

    @After
    public void cleanupCourier() {
        CourierModel credentials = new CourierModel(login, PASSWORD, null);
        Response loginResp = courierClient.loginCourier(credentials);

        if (loginResp.statusCode() == 200) {
            int id = loginResp.jsonPath().getInt("id");
            courierClient.deleteCourier(id)
                .then()
                .statusCode(anyOf(is(200), is(404)));
        }
    }

    @Test
    @DisplayName("Create courier with unique login")
    @Description("Verifies that a new courier can be successfully created using a unique login.")
    public void createCourierWithUniqueLoginTest() {
        CourierModel courier = new CourierModel(login, PASSWORD, FIRST_NAME);
        courierClient.createCourier(courier)
            .then()
            .statusCode(201);
    }

    @Test
    @DisplayName("Cannot create courier with duplicate login")
    @Description("Verifies that creating a courier with an already existing login returns a 409 Conflict status.")
    public void cannotCreateDuplicateCourierTest() {
        CourierModel courier = new CourierModel(login, PASSWORD, FIRST_NAME);
        courierClient.createCourier(courier).then().statusCode(201);
        courierClient.createCourier(courier).then().statusCode(409);
    }

    @Test
    @DisplayName("Cannot create courier without login")
    @Description("Verifies that a courier cannot be created when the login is missing. Should return 400 Bad Request.")
    public void cannotCreateWithoutLoginTest() {
        CourierModel courier = new CourierModel(null, PASSWORD, FIRST_NAME);
        courierClient.createCourier(courier)
            .then()
            .statusCode(400)
            .body("message", containsString(NOT_ENOUGHT_DATA_TO_CREATE_ACOCUNT));
    }

    @Test
    @DisplayName("Cannot create courier without password")
    @Description("Verifies that a courier cannot be created when the password is missing. Should return 400 Bad Request.")
    public void cannotCreateWithoutPasswordTest() {
        CourierModel courier = new CourierModel(login, null, FIRST_NAME);
        courierClient.createCourier(courier)
            .then()
            .statusCode(400)
            .body("message", containsString(NOT_ENOUGHT_DATA_TO_CREATE_ACOCUNT));
    }

    @Test
    @DisplayName("It is possible to create courier without first name")
    @Description("Verifies that a courier can be created without specifying a first name. Should return 201 Created.")
    public void possibleToCreateWithoutFirstNameTest() {
        CourierModel courier = new CourierModel(login, PASSWORD, null);
        courierClient.createCourier(courier)
            .then()
            .statusCode(201);
    }
}