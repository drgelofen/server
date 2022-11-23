package server.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.Application;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "پیام رسانی", permissionBrief = "برای ارسال پیام به کاربران و مدیریت پیام ها از این بخش استفاده کنید", permissionPriority = 150)
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "messages" + SchemaUtil.TABLE_POSTFIX)
public class Message extends DatabaseModel<Message> {

    public static final String MESSAGE_IDENTITY = "message_identity";
    public static final String MESSAGE_STATE = "message_state";
    public static final String DOCTOR_ID = "doctor_id";
    public static final String USER_ID = "user_id";

    public static final int STATE_UNREAD = 0;
    public static final int STATE_READ = 1;

    public static final int CATEGORY_INDIVIDUAL = 0;
    public static final int CATEGORY_PUBLIC = 1;

    public static final int TYPE_PUSH_USERS = 0;
    public static final int TYPE_PUSH_DOCTORS = 1;
    public static final int TYPE_ACTION = 2;

    @DatabaseField(generatedId = true)
    private Long message_id;

    @DatabaseField(canBeNull = false)
    private String message_title;

    @DatabaseField(canBeNull = false, dataType = DataType.LONG_STRING)
    private String message_body;

    @DatabaseField(defaultValue = "0", canBeNull = false)
    private Integer message_type;

    @DatabaseField(defaultValue = "0", canBeNull = false)
    private Integer message_category;

    @DatabaseField(defaultValue = "0", canBeNull = false)
    private Integer message_state;

    @DatabaseField(canBeNull = false)
    private String message_identity;

    @DatabaseField(foreign = true)
    private Doctor doctor;

    @DatabaseField(foreign = true)
    private User user;

    @DatabaseField
    private String message_topic;

    @DatabaseField
    private String temp_id;

    @DatabaseField
    private String temp_user;

    private UUID user_id, doctor_id;
    private UUID[] audience;

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public UUID getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(UUID doctor_id) {
        this.doctor_id = doctor_id;
    }

    public String getTemp_user() {
        return temp_user;
    }

    public void setTemp_user(String temp_user) {
        this.temp_user = temp_user;
    }

    public void setTemp_id(String temp_id) {
        this.temp_id = temp_id;
    }

    public String getTemp_id() {
        return temp_id;
    }

    public String getMessage_identity() {
        return message_identity;
    }

    public void setMessage_identity(String message_identity) {
        this.message_identity = message_identity;
    }

    public String getMessage_topic() {
        return message_topic;
    }

    public void setMessage_topic(String message_topic) {
        this.message_topic = message_topic;
    }

    public Integer getMessage_category() {
        return message_category;
    }

    public void setMessage_category(Integer message_category) {
        this.message_category = message_category;
    }

    public Long getMessage_id() {
        return message_id;
    }

    public void setMessage_id(Long message_id) {
        this.message_id = message_id;
    }

    public String getMessage_title() {
        return message_title;
    }

    public void setMessage_title(String message_title) {
        this.message_title = message_title;
    }

    public String getMessage_body() {
        return message_body;
    }

    public void setMessage_body(String message_body) {
        this.message_body = message_body;
    }

    public Integer getMessage_type() {
        return message_type;
    }

    public void setMessage_type(Integer message_type) {
        this.message_type = message_type;
    }

    public Integer getMessage_state() {
        return message_state;
    }

    public void setMessage_state(Integer message_state) {
        this.message_state = message_state;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public UUID[] getAudience() {
        return audience;
    }

    public void setAudience(UUID[] audience) {
        this.audience = audience;
    }

    public Message() {
        super();
    }

    @Override
    public void merge(Message model) {
        super.merge(model);
    }

    @Override
    public JsonObject toJsonObject() {
        Message message = new Message();
        message.setMessage_category(message_category);
        message.setMessage_title(message_title);
        message.setMessage_body(message_body);
        message.setMessage_type(message_type);

        String data = Application.GSON.toJson(message);
        return JsonParser.parseString(data).getAsJsonObject();
    }

    @Override
    public Message trim() {
        message_identity = null;
        message_topic = null;
        message_category = null;
        message_type = null;
        return super.trim();
    }
}
