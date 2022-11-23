package server.lib.model;

import com.google.gson.JsonObject;
import server.model.Message;

public class FcmModel {

    private JsonObject notification;
    private JsonObject data;

    private String[] registration_ids;
    private String to;

    public void setData(String title, String message) {
        this.setData(title, message, Message.TYPE_ACTION);
    }

    public void setData(String title, String message, int type) {
        JsonObject model = new JsonObject();
        model.addProperty("body", message);
        model.addProperty("title", title);
        model.addProperty("type", type);
        this.data = model;
    }

    public JsonObject getData() {
        return data;
    }

    public JsonObject getNotification() {
        return notification;
    }

    public void setNotification(JsonObject notification) {
        this.notification = notification;
    }

    public String[] getRegistration_ids() {
        return registration_ids;
    }

    public void setRegistration_ids(String[] registration_ids) {
        this.registration_ids = registration_ids;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setTopic(String to) {
        this.to = "/topics/" + to;
    }
}
