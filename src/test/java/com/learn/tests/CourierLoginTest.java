package com.learn.tests;

import com.learn.model.CourierModel;
import com.learn.service.CourierClientService;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.*;

public class CourierLoginTest {

    private static final String PASSWORD = "12345";
    private static final String FIRST_NAME = "John";
    private final CourierClientService courierClient = new CourierClientService();
    private String login;

    @Before
    public void setup() {
        login = "courier_login_" + UUID.randomUUID();
        CourierModel courier = new CourierModel(login, PASSWORD, FIRST_NAME);
        courierClient.createCourier(courier)
            .then()
            .statusCode(201);
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
        } else {
            System.out.printf("Cleanup skipped: courier '%s' not found (%d)%n",
                login, loginResp.statusCode());
        }
    }

    @Test
    @DisplayName("Courier can login successfully")
    @Description("Verifies that an existing courier can log in using correct credentials.")
    public void courierCanLoginTest() {
        CourierModel credentials = new CourierModel(login, PASSWORD, null);
        courierClient.loginCourier(credentials)
            .then()
            .statusCode(200)
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("Login without password returns error")
    @Description("Verifies that a courier cannot log in if the password field is missing. Expected status code is 400.")
    public void loginWithoutPasswordReturnsErrorTest() {
        CourierModel credentials = new CourierModel(login, null, null);
        courierClient.loginCourier(credentials)
            .then()
            .statusCode(400)
            .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Login with wrong password returns error")
    @Description("Verifies that logging in with an incorrect password results in a 404 response.")
    public void loginWithWrongPasswordReturnsErrorTest() {
        CourierModel credentials = new CourierModel(login, "wrongpass", null);
        courierClient.loginCourier(credentials)
            .then()
            .statusCode(404)
            .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Login without login returns error")
    @Description("Verifies that logging in without providing a login returns a 400 Bad Request response.")
    public void loginWithoutLoginReturnsErrorTest() {
        CourierModel credentials = new CourierModel(null, PASSWORD, null);
        courierClient.loginCourier(credentials)
            .then()
            .statusCode(400)
            .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Login with non-existent courier returns error")
    @Description("Verifies that login attempt with a non-existent user returns 404 and appropriate message.")
    public void loginNonExistentCourierReturnsErrorTest() {
        CourierModel credentials = new CourierModel("nonexistent_user_" + UUID.randomUUID(), PASSWORD, null);
        courierClient.loginCourier(credentials)
            .then()
            .statusCode(404)
            .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Login returns courier ID")
    @Description("Verifies that a successful login returns a valid courier ID in the response body.")
    public void loginReturnsCourierIdTest() {
        CourierModel credentials = new CourierModel(login, PASSWORD, null);
        Response response = courierClient.loginCourier(credentials);
        response.then().statusCode(200);

        int id = response.jsonPath().getInt("id");
        Assert.assertTrue(id > 0);
    }
}