package com.learn.tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.*;
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

    @BeforeClass
    public static void setUpClass() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
        RestAssured.basePath = "/api/v1";
    }

    @Before
    public void setup() {
        login = "courier_creation_" + UUID.randomUUID();
        courierId = null;
    }

    @Step("Create a courier with a unique login")
    private void createCourier(String login) {
        given()
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\", \"password\":\"" + PASSWORD + "\", \"firstName\":\"" + FIRST_NAME + "\"}")
            .when()
            .post("/courier")
            .then()
            .statusCode(anyOf(is(201), is(409)));

        // Сохраняем id для удаления
        Response loginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\", \"password\":\"" + PASSWORD + "\"}")
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
        createCourier(login);
        given()
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\", \"password\":\"" + PASSWORD + "\", \"firstName\":\"" + FIRST_NAME + "\"}")
            .when()
            .post("/courier")
            .then()
            .statusCode(409)
            .body("message", containsString("Этот логин уже используется"));
    }

    @Test
    @DisplayName("Cannot create courier without login")
    public void cannotCreateWithoutLogin() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"password\":\"" + PASSWORD + "\", \"firstName\":\"" + FIRST_NAME + "\"}")
            .when()
            .post("/courier")
            .then()
            .statusCode(400)
            .body("message", containsString(NOT_ENOUGHT_DATA_TO_CREATE_ACOCUNT));
    }

    @Test
    @DisplayName("Cannot create courier without password")
    public void cannotCreateWithoutPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\", \"firstName\":\"" + FIRST_NAME + "\"}")
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
        given()
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\", \"password\":\"" + PASSWORD + "\"}")
            .when()
            .post("/courier")
            .then()
            .statusCode(400)
            .body("message", containsString(NOT_ENOUGHT_DATA_TO_CREATE_ACOCUNT));
    }

    @AfterClass
    public static void resetBasePath() {
        RestAssured.basePath = "";
    }
}