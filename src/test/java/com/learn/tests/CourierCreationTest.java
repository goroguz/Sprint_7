package com.learn.tests;

import com.learn.model.CourierModel;
import com.learn.util.RestAssuredConfig;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(JUnit4.class)
public class CourierCreationTest {

    static final String PASSWORD = "1234";
    static final String FIRST_NAME = "John";
    public static final String NOT_ENOUGHT_DATA_TO_CREATE_ACOCUNT = "Недостаточно данных для создания учетной записи";
    static String login;
    static Integer courierId;

    @Before
    public void setup() {
        login = "courier_creation_" + UUID.randomUUID();
        courierId = null;
    }

    @Step("Create a courier with a unique login")
    private void createCourier(String login) {
        CourierModel courier = new CourierModel(login, PASSWORD, FIRST_NAME);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post("/courier")
            .then()
            .statusCode(anyOf(is(201), is(409)));

        courier = new CourierModel(login, PASSWORD, null);
        // Сохраняем id для удаления
        Response loginResponse = given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post("/courier/login");

        if (loginResponse.statusCode() == 200) {
            courierId = loginResponse.jsonPath().getInt("id");
        }
    }

    @After
    public void cleanupCourier() {
        if (courierId != null) {
            given()
                .spec(RestAssuredConfig.getBaseSpec())
                .when()
                .delete("/courier/" + courierId)
                .then()
                .statusCode(anyOf(is(200), is(404)));
        }
    }

    @Test
    @DisplayName("Create courier with unique login")
    public void createCourierWithUniqueLogin() {
        createCourier(login);
    }

    @Test
    @DisplayName("Cannot create courier with duplicate login")
    public void cannotCreateDuplicateCourier() {
        CourierModel courier = new CourierModel(login, PASSWORD, FIRST_NAME);
        createCourier(login);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post("/courier")
            .then()
            .statusCode(409)
            .body("message", containsString("Этот логин уже используется"));
    }

    @Test
    @DisplayName("Cannot create courier without login")
    public void cannotCreateWithoutLogin() {
        CourierModel courier = new CourierModel(null, PASSWORD, FIRST_NAME);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post("/courier")
            .then()
            .statusCode(400)
            .body("message", containsString(NOT_ENOUGHT_DATA_TO_CREATE_ACOCUNT));
    }

    @Test
    @DisplayName("Cannot create courier without password")
    public void cannotCreateWithoutPassword() {
        CourierModel courier = new CourierModel(login, null, FIRST_NAME);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post("/courier")
            .then()
            .statusCode(400)
            .body("message", containsString(NOT_ENOUGHT_DATA_TO_CREATE_ACOCUNT));
    }

    @Test
    @DisplayName("Cannot create courier without first name")
    //according to the API, first name is not stated as optional, but API ignores it
    public void cannotCreateWithoutFirstName() {
        CourierModel courier = new CourierModel(login, PASSWORD, null);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post("/courier")
            .then()
            .statusCode(400)
            .body("message", containsString(NOT_ENOUGHT_DATA_TO_CREATE_ACOCUNT));
    }
}