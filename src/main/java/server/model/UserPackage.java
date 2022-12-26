package server.model;

import com.google.gson.JsonElement;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.time.Instant;

@Permission(permissionName = "اشتراک مشاوره کاربران")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "user_packages" + SchemaUtil.TABLE_POSTFIX)
public class UserPackage extends DatabaseModel<UserPackage> {

    public static final String CONDITION = "user_condition";
    public static final String PACKAGE = "sub_package_id";
    public static final String DOCTOR = "doctor_id";
    public static final String USER = "user_id";

    public static final String COMMENT = "comment";
    public static final String RATE = "rate";

    @DatabaseField(generatedId = true)
    private Long record_id;

    @DatabaseField(foreign = true, canBeNull = false)
    private SubPackage sub_package;

    @DatabaseField(foreign = true, canBeNull = false)
    private Doctor doctor;

    @DatabaseField(foreign = true, canBeNull = false)
    private User user;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String comment;

    @DatabaseField(defaultValue = "0")
    private Integer rate;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement user_condition;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer package_character;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer package_video;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer package_voice;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer package_image;

    @DatabaseField()
    private Long comment_date;

    private Long package_remain;

    @DatabaseField(defaultValue = "false")
    private Boolean isAccepted = false;

    public Long getComment_date() {
        return comment_date;
    }

    public void setComment_date(Long comment_date) {
        this.comment_date = comment_date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Long getPackage_remain() {
        return package_remain;
    }

    public void setPackage_remain(Long package_remain) {
        this.package_remain = package_remain;
    }

    public JsonElement getUser_condition() {
        return user_condition;
    }

    public void setUser_condition(JsonElement user_condition) {
        this.user_condition = user_condition;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Integer getPackage_character() {
        return package_character;
    }

    public void setPackage_character(Integer package_character) {
        this.package_character = package_character;
    }

    public Integer getPackage_video() {
        return package_video;
    }

    public void setPackage_video(Integer package_video) {
        this.package_video = package_video;
    }

    public Integer getPackage_voice() {
        return package_voice;
    }

    public void setPackage_voice(Integer package_voice) {
        this.package_voice = package_voice;
    }

    public Integer getPackage_image() {
        return package_image;
    }

    public void setPackage_image(Integer package_image) {
        this.package_image = package_image;
    }

    public Long getRecord_id() {
        return record_id;
    }

    public void setRecord_id(Long record_id) {
        this.record_id = record_id;
    }

    public SubPackage getSub_package() {
        return sub_package;
    }

    public void setSub_package(SubPackage sub_package) {
        this.sub_package = sub_package;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserPackage() {
        super();
    }

    @Override
    public void merge(UserPackage info) {
        super.merge(info);
        comment_date = Instant.now().toEpochMilli();
        comment = info.comment;
        rate = info.rate;
    }

    @Override
    public UserPackage trim() {
        if (sub_package != null) sub_package.trim();
        if (doctor != null) doctor.trim();
        if (user != null) user.trim();
        return super.trim();
    }

    public Boolean getAccepted() {
        return isAccepted;
    }

    public void setAccepted(Boolean accepted) {
        isAccepted = accepted;
    }
}
