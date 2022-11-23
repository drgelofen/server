package server.lib.center;

import com.google.gson.JsonObject;
import org.springframework.http.HttpMethod;
import server.Application;
import server.lib.utils.Rest;

import java.util.HashMap;

public class MailCenter {

    private static final String URL = "http://RestfulSms.com/api";

    public static void send(String phoneNumber, String verificationCode) {
        if (!Application.ENABLE_OTP) {
            return;
        }
        String token = getToken();
        if (token == null) {
            return;
        }
        JsonObject object = new JsonObject();
        object.addProperty("Code", verificationCode);
        object.addProperty("MobileNumber", phoneNumber);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-sms-ir-secure-token", token);
        Rest.sync(URL + "/VerificationCode", HttpMethod.POST, object, headers);
    }

    private static String getToken() {
        JsonObject object = new JsonObject();
        object.addProperty("UserApiKey", Application.OTP_KEY);
        object.addProperty("SecretKey", Application.OTP_SECRET);
        String result = Rest.sync(URL + "/Token", HttpMethod.POST, object);
        JsonObject response = Application.GSON.fromJson(result, JsonObject.class);
        return response.get("TokenKey").getAsString();
    }
}
