package server.lib.center;

import org.springframework.http.HttpMethod;
import server.Application;
import server.lib.model.FcmModel;
import server.lib.utils.Rest;
import server.lib.utils.TokenUtil;

import java.util.HashMap;

public class FCMCenter {

    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    public static String push(FcmModel model) {
        try {
            if (Application.ENABLE_FCM) {
                HashMap<String, String> header = new HashMap<>();
                header.put(TokenUtil.AUTHORIZATION_HEADER, "key=" + Application.FCM_KEY);
                return Rest.sync(FCM_URL, HttpMethod.POST, model, header);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}
