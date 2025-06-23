package com.learn.tests;

import com.learn.model.CourierModel;
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
        CourierModel courier = new CourierModel(login, PASSWORD, FIRST_NAME);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post(PATH_COURIER)
            .then()
            .statusCode(201);

        // Получаем его ID
        courier = new CourierModel(login, PASSWORD, null);
        Response loginResponse = given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
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
        CourierModel courier = new CourierModel(login, PASSWORD, null);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post(PATH_COURIER_LOGIN)
            .then()
            .statusCode(200)
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("Login without password returns error")
    public void loginWithoutPasswordReturnsError() {
        CourierModel courier = new CourierModel(login, null, null);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post(PATH_COURIER_LOGIN)
            .then()
            .statusCode(400)
            .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Login with wrong password returns error")
    public void loginWithWrongPasswordReturnsError() {
        CourierModel courier = new CourierModel(login, "wrongpass", null);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post(PATH_COURIER_LOGIN)
            .then()
            .statusCode(404)
            .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Login without login returns error")
    public void loginWithoutLoginReturnsError() {
        CourierModel courier = new CourierModel(null, PASSWORD, null);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post(PATH_COURIER_LOGIN)
            .then()
            .statusCode(400)
            .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Login with non-existent courier returns error")
    public void loginNonExistentCourierReturnsError() {
        CourierModel courier = new CourierModel("nonexistent_user_" + UUID.randomUUID(), PASSWORD, null);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post(PATH_COURIER_LOGIN)
            .then()
            .statusCode(404)
            .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Login returns courier ID")
    public void loginReturnsCourierId() {
        CourierModel courier = new CourierModel(login, PASSWORD, null);
        Response response = given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post(PATH_COURIER_LOGIN);

        response.then().statusCode(200);
        int id = response.jsonPath().getInt("id");
        Assert.assertTrue(id > 0);
    }
}