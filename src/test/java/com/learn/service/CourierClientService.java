package com.learn.service;

import com.learn.model.CourierModel;
import com.learn.util.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class CourierClientService {
    public Response createCourier(CourierModel courier) {
        return given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(courier)
            .when()
            .post("/courier");
    }

    public Response loginCourier(CourierModel credentials) {
        return given()
            .spec(RestAssuredConfig.getBaseSpec())
            .contentType(ContentType.JSON)
            .body(credentials)
            .when()
            .post("/courier/login");
    }

    public Response deleteCourier(int courierId) {
        return given()
            .spec(RestAssuredConfig.getBaseSpec())
            .when()
            .delete("/courier/" + courierId);
    }
}
