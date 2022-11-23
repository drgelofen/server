package server.model;

import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "رابطه کاربران با پزشکان")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "user_doctors" + SchemaUtil.TABLE_POSTFIX)
public class UserDoctor extends DatabaseModel<UserDoctor> {

    public static final String USER = "user_id";
    public static final String DOCTOR = "doctor_id";
    public static final String COUNT = "record_count";
    public static final String LAST_TIMESTAMP = "record_last_stamp";
    public static final String UNREAD_USER = "record_unread_user";
    public static final String UNREAD_DOCTOR = "record_unread_doctor";

    public static final String MESSAGE_CANCEL = "متاسفانه مکان لغو این مشاوره را ندارید";
    public static final String MESSAGE_CANCEL_CONSULT = "این مشاوره لغو شده است، امکان ارسال پیام ندارید";

    public static final int SETTLE_TYPE_WALLET = 1;
    public static final int SETTLE_TYPE_TICKET = 2;

    @DatabaseField(generatedId = true)
    private Long record_id;

    @DatabaseField(foreign = true, canBeNull = false)
    private Doctor doctor;

    @DatabaseField(foreign = true, canBeNull = false)
    private User user;

    @DatabaseField(defaultValue = "0")
    private Long record_last_stamp;

    @DatabaseField(foreign = true)
    private Consult record_last;

    @DatabaseField(defaultValue = "0")
    private Long record_packages;

    @DatabaseField(defaultValue = "0")
    private Long record_count;

    @DatabaseField(defaultValue = "0")
    private Integer record_unread_user;

    @DatabaseField(defaultValue = "0")
    private Integer record_unread_doctor;

    @DatabaseField(canBeNull = false, defaultValue = "false")
    private Boolean record_canceled;

    @DatabaseField(canBeNull = false, defaultValue = "false")
    private Boolean record_settled;

    private UUID doctor_id, user_id;
    private Integer settle_type;

    private String settle_name, settle_account;
    private Long remain;

    public String getSettle_name() {
        return settle_name;
    }

    public void setSettle_name(String settle_name) {
        this.settle_name = settle_name;
    }

    public String getSettle_account() {
        return settle_account;
    }

    public void setSettle_account(String settle_account) {
        this.settle_account = settle_account;
    }

    public Integer getSettle_type() {
        return settle_type;
    }

    public void setSettle_type(Integer settle_type) {
        this.settle_type = settle_type;
    }

    public Boolean getRecord_settled() {
        return record_settled;
    }

    public void setRecord_settled(Boolean record_settled) {
        this.record_settled = record_settled;
    }

    public Boolean getRecord_canceled() {
        return record_canceled;
    }

    public void setRecord_canceled(Boolean record_canceled) {
        this.record_canceled = record_canceled;
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

    public Integer getRecord_unread_user() {
        return record_unread_user;
    }

    public void setRecord_unread_user(Integer record_unread_user) {
        this.record_unread_user = record_unread_user;
    }

    public Integer getRecord_unread_doctor() {
        return record_unread_doctor;
    }

    public void setRecord_unread_doctor(Integer record_unread_doctor) {
        this.record_unread_doctor = record_unread_doctor;
    }

    public Long getRecord_packages() {
        return record_packages;
    }

    public void setRecord_packages(Long record_packages) {
        this.record_packages = record_packages;
    }

    public Long getRecord_count() {
        return record_count;
    }

    public void setRecord_count(Long record_count) {
        this.record_count = record_count;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Long getRecord_id() {
        return record_id;
    }

    public void setRecord_id(Long record_id) {
        this.record_id = record_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getRecord_last_stamp() {
        return record_last_stamp;
    }

    public void setRecord_last_stamp(Long record_last_stamp) {
        this.record_last_stamp = record_last_stamp;
    }

    public Consult getRecord_last() {
        return record_last;
    }

    public void setRecord_last(Consult record_last) {
        this.record_last = record_last;
    }

    public void setRemain(Long remain) {
        this.remain = remain;
    }

    public Long getRemain() {
        return remain;
    }

    public UserDoctor() {
        super();
    }

    @Override
    public void merge(UserDoctor model) {
        super.merge(model);
    }

    @Override
    public UserDoctor trim() {
        if (record_last != null) record_last.trim();
        if (doctor != null) doctor.trim();
        if (user != null) user.trim();
        return super.trim();
    }
}
