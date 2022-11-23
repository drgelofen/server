package server.model;

import com.google.gson.JsonElement;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.ArrayPersister;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "مدیریت مشاوره", permissionBrief = "در این قسمت مشاوره کاربران و پزشکان را مشاهده نمایید")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "consults" + SchemaUtil.TABLE_POSTFIX)
public class Consult extends DatabaseModel<Consult> {

    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;

    public static final int TYPE_CHARACTER = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_AUDIO = 2;
    public static final int TYPE_VIDEO = 3;
    public static final int TYPE_SUGGEST = 4;

    public static final String DOCTOR = "doctor_id";
    public static final String TYPE = "post_type";
    public static final String USER = "user_id";
    public static final String OWNER = "post_owner";

    public static final int OWNER_DOCTOR = 0;
    public static final int OWNER_USER = 1;
    public static final String MESSAGE = "کاربر گرامی لطفا متن و یا فایل مربوط به پیام را بررسی کنید";

    @DatabaseField(generatedId = true)
    private Long post_id;

    @DatabaseField(dataType = DataType.LONG_STRING, canBeNull = false)
    private String post_message;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement post_attachment;

    @DatabaseField(foreign = true, canBeNull = false)
    private User user;

    @DatabaseField(foreign = true, canBeNull = false)
    private Doctor doctor;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer post_status;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer post_owner;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer post_type;

    @DatabaseField(persisterClass = ArrayPersister.class)
    private UUID[] post_suggests;

    private UUID doctor_id, user_id;
    private Long record_id;

    public Integer getPost_owner() {
        return post_owner;
    }

    public void setPost_owner(Integer post_owner) {
        this.post_owner = post_owner;
    }

    public Long getRecord_id() {
        return record_id;
    }

    public void setRecord_id(Long record_id) {
        this.record_id = record_id;
    }

    public UUID getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(UUID doctor_id) {
        this.doctor_id = doctor_id;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public Long getPost_id() {
        return post_id;
    }

    public void setPost_id(Long post_id) {
        this.post_id = post_id;
    }

    public String getPost_message() {
        return post_message;
    }

    public void setPost_message(String post_message) {
        this.post_message = post_message;
    }

    public JsonElement getPost_attachment() {
        return post_attachment;
    }

    public void setPost_attachment(JsonElement post_attachment) {
        this.post_attachment = post_attachment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Integer getPost_status() {
        return post_status;
    }

    public void setPost_status(Integer post_status) {
        this.post_status = post_status;
    }

    public Integer getPost_type() {
        return post_type;
    }

    public void setPost_type(Integer post_type) {
        this.post_type = post_type;
    }

    public UUID[] getPost_suggests() {
        return post_suggests;
    }

    public void setPost_suggests(UUID[] post_suggests) {
        this.post_suggests = post_suggests;
    }

    public Consult() {
        super();
    }

    @Override
    public void merge(Consult model) {
        super.merge(model);
    }

    @Override
    public Consult trim() {
        if (doctor != null) doctor.trim();
        if (user != null) user.trim();
        return super.trim();
    }
}
