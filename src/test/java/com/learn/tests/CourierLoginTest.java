package com.learn.tests;

import com.learn.util.RestAssuredConfig;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CourierLoginTest {

    private static final String PASSWORD = "12345";
    private static final String FIRST_NAME = "John";
    private static final String PATH_COURIER = "/courier";
    private static final String PATH_COURIER_LOGIN = "/courier/login";

    private String login;
    private Integer courierId;

    @Before
    public void setup() {
        // Уникальный логин для каждого теста
        login = "courier_login_" + UUID.randomUUID();

        // Создание нового курьера
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\", \"password\":\"" + PASSWORD + "\", \"firstName\":\"" + FIRST_NAME + "\"}")
            .when()
            .post(PATH_COURIER)
            .then()
            .statusCode(201);

        // Получаем его ID
        Response loginResponse = given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\", \"password\":\"" + PASSWORD + "\"}")
            .when()
            .post(PATH_COURIER_LOGIN);

        if (loginResponse.statusCode() == 200) {
            courierId = loginResponse.jsonPath().getInt("id");
        }
    }

    @After
    public void tearDown() {
        if (courierId != null) {
            given()
                .spec(RestAssuredConfig.getBaseSpec())
                .delete(PATH_COURIER + "/" + courierId)
                .then()
                .statusCode(anyOf(is(200), is(404)));
        }
    }

    @Test
    @DisplayName("Courier can login successfully")
    public void courierCanLogin() {
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\", \"password\":\"" + PASSWORD + "\"}")
            .when()
            .post(PATH_COURIER_LOGIN)
            .then()
            .statusCode(200)
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("Login without password returns error")
    public void loginWithoutPasswordReturnsError() {
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\"}")
            .when()
            .post(PATH_COURIER_LOGIN)
            .then()
            .statusCode(400)
            .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Login with wrong password returns error")
    public void loginWithWrongPasswordReturnsError() {
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\", \"password\":\"wrongpass\"}")
            .when()
            .post(PATH_COURIER_LOGIN)
            .then()
            .statusCode(404)
            .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Login without login returns error")
    public void loginWithoutLoginReturnsError() {
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body("{\"password\":\"" + PASSWORD + "\"}")
            .when()
            .post(PATH_COURIER_LOGIN)
            .then()
            .statusCode(400)
            .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Login with non-existent courier returns error")
    public void loginNonExistentCourierReturnsError() {
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body("{\"login\":\"nonexistent_user_" + UUID.randomUUID() + "\", \"password\":\"pass\"}")
            .when()
            .post(PATH_COURIER_LOGIN)
            .then()
            .statusCode(404)
            .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Login returns courier ID")
    public void loginReturnsCourierId() {
        Response response = given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body("{\"login\":\"" + login + "\", \"password\":\"" + PASSWORD + "\"}")
            .when()
            .post(PATH_COURIER_LOGIN);

        response.then().statusCode(200);
        int id = response.jsonPath().getInt("id");
        Assert.assertTrue(id > 0);
    }
}