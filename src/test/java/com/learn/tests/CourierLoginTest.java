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

import io.qameta.allure.Description;

public class CourierLoginTest {

    private static final String PASSWORD = "12345";
    private static final String FIRST_NAME = "John";
    private static final String PATH_COURIER = "/courier";
    private static final String PATH_COURIER_LOGIN = "/courier/login";

    private String login;

    @Before
    public void setup() {
        login = "courier_login_" + UUID.randomUUID();
        CourierModel courier = new CourierModel(login, PASSWORD, FIRST_NAME);
        given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post(PATH_COURIER)
            .then()
            .statusCode(201);
    }

    @After
    public void cleanupCourier() {
        CourierModel credentials = new CourierModel(login, PASSWORD, null);
        Response loginResp = given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(credentials)
            .when()
            .post(PATH_COURIER_LOGIN);

        if (loginResp.statusCode() == 200) {
            int id = loginResp.jsonPath().getInt("id");
            given()
                .spec(RestAssuredConfig.getBaseSpec())
                .when()
                .delete(PATH_COURIER + id)
                .then()
                .statusCode(anyOf(is(200), is(404)));
        } else {
            System.out.printf("Cleanup skipped: courier '%s' not found (%d)%n",
                login, loginResp.statusCode());
        }
    }

    @Test
    @DisplayName("Courier can login successfully")
    @Description("Verifies that an existing courier can log in using correct credentials.")
    public void courierCanLoginTest() {
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
    @Description("Verifies that a courier cannot log in if the password field is missing. Expected status code is 400.")
    public void loginWithoutPasswordReturnsErrorTest() {
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
    @Description("Verifies that logging in with an incorrect password results in a 404 response.")
    public void loginWithWrongPasswordReturnsErrorTest() {
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
    @Description("Verifies that logging in without providing a login returns a 400 Bad Request response.")
    public void loginWithoutLoginReturnsErrorTest() {
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
    @Description("Verifies that login attempt with a non-existent user returns 404 and appropriate message.")
    public void loginNonExistentCourierReturnsErrorTest() {
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
    @Description("Verifies that a successful login returns a valid courier ID in the response body.")
    public void loginReturnsCourierIdTest() {
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