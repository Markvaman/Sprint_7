import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoginCourierErrorTests {
    private CourierCredentials courierCredentials;
    private CourierMethod courierMethod;
    private Courier courier;

    @Before
    public void setUp() {
        courierMethod = new CourierMethod();
        courier = new Courier("blake", "5325", "boyd");
        ValidatableResponse response = courierMethod.create(courier);
        int statusCode = response.extract().statusCode();
        assertEquals("Status code is invalid", SC_CREATED, statusCode);

    }

    @After
    public void deleteCourier() {
        ValidatableResponse response = courierMethod.login(CourierCredentials.from(courier));
        int statusCode = response.extract().statusCode();
        assertEquals("Status is invalid", SC_OK, statusCode );
        int courierId = response.extract().path("id");
        assertTrue("Courier Id is invalid", courierId > 0);
        courierMethod.delete(courierId);
    }

    @Test
    @DisplayName("Login error without login")
    @Description("Send post to api/v1/courier/login, status 400")
    public void courierCannotLoginWithoutPasswordTest() {
        courierCredentials = new CourierCredentials(courier.getPassword());
        ValidatableResponse LoginResponse = courierMethod.login(courierCredentials);
        int statusLoginCode = LoginResponse.extract().statusCode();
        assertEquals("Status code is invalid", SC_BAD_REQUEST, statusLoginCode);
        String message = LoginResponse.extract().path("message");
        assertEquals("Message is uncorrected", "Недостаточно данных для входа", message );
//тест проходит, если запрос делать только с паролем, но когда делаешь только с логином - падает с ошибкой 504 - с чем это связано, разобраться не смогла
    }
    @Test
    @DisplayName("Login error with non-existent account")
    @Description("Send post to api/v1/courier/login with non-existent login, status 404")
    public void cannotLoginUnExistingAccountTest() {
        courierCredentials = new CourierCredentials("larry", "4587");
        ValidatableResponse LoginResponse = courierMethod.login(courierCredentials);
        int statusLoginCode = LoginResponse.extract().statusCode();
        assertEquals("Status code is invalid", SC_NOT_FOUND, statusLoginCode);
        String message = LoginResponse.extract().path("message");
        assertEquals("Message is incorrect", "Учетная запись не найдена", message);
    }

    @Test
    @DisplayName("Login error with login mistake")
    @Description("Send post to api/v1/courier/login with mistake in login, status 404")
    public void errorIfInputLoginWithMistakeTest() {
        courierCredentials = new CourierCredentials("dlake", "5325");
        ValidatableResponse response = courierMethod.login(courierCredentials);
        int statusCode = response.extract().statusCode();
        assertEquals("Status is invalid", SC_NOT_FOUND, statusCode);
    }
}
