package server.model;

import com.google.gson.JsonElement;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "مدیریت نظرات و پیشنهادات", permissionBrief = "در این قسمت در قالب تیکت با کاربران و پزشکان ارتباط برقرار نمایید")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "tickets" + SchemaUtil.TABLE_POSTFIX)
public class Ticket extends DatabaseModel<Ticket> {

    public static final int STATE_DOCUMENT = 5;
    public static final int STATE_REJECTED = 4;
    public static final int STATE_ANSWERED = 2;
    public static final int STATE_PENDING = 1;
    public static final int STATE_CLOSED = 3;
    public static final int STATE_INIT = 0;

    public static final int OWNER_SYSTEM = 0;
    public static final int OWNER_USER = 1;
    public static final int OWNER_DOCTOR = 2;

    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;

    public static final String DESCRIPTION = "ticket_description";
    public static final String STATUS = "ticket_status";
    public static final String TITLE = "ticket_title";
    public static final String OWNER = "ticket_owner";
    public static final String PARENT = "parent_id";
    public static final String USER = "user_id";
    public static final String DOCTOR = "doctor_id";
    public static final String ID = "ticket_id";
    public static final String STATE = "ticket_state";
    public static final String ADMIN = "admin_id";

    public static final String LATEST = "ticket_latest";
    public static final String COUNT = "ticket_count";

    public static final String UNREAD_SYSTEM = "ticket_unread_system";
    public static final String UNREAD_USER = "ticket_unread_user";
    public static final String UNREAD_DOCTOR = "ticket_unread_doctor";

    @DatabaseField(generatedId = true, unique = true)
    private UUID ticket_id;

    @DatabaseField()
    private String ticket_title;

    @DatabaseField(dataType = DataType.LONG_STRING, canBeNull = false)
    private String ticket_description;

    @DatabaseField(foreign = true)
    private Ticket parent;

    @DatabaseField(foreign = true)
    private User user;

    @DatabaseField(foreign = true)
    private Doctor doctor;

    @DatabaseField(foreign = true)
    private Admin admin;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer ticket_state;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer ticket_status;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer ticket_owner;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement ticket_attachments;

    @DatabaseField(defaultValue = "0")
    private Long ticket_count;

    @DatabaseField(defaultValue = "0")
    private Long ticket_latest;

    @DatabaseField(defaultValue = "0")
    private Long ticket_unread_user;

    @DatabaseField(defaultValue = "0")
    private Long ticket_unread_doctor;

    @DatabaseField(defaultValue = "0")
    private Long ticket_unread_system;

    private UUID user_id, admin_id, parent_id, doctor_id;

    public Long getTicket_unread_doctor() {
        return ticket_unread_doctor;
    }

    public void setTicket_unread_doctor(Long ticket_unread_doctor) {
        this.ticket_unread_doctor = ticket_unread_doctor;
    }

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

    public Long getTicket_count() {
        return ticket_count;
    }

    public void setTicket_count(Long ticket_count) {
        this.ticket_count = ticket_count;
    }

    public Long getTicket_unread_user() {
        return ticket_unread_user;
    }

    public void setTicket_unread_user(Long ticket_unread_user) {
        this.ticket_unread_user = ticket_unread_user;
    }

    public Long getTicket_unread_system() {
        return ticket_unread_system;
    }

    public void setTicket_unread_system(Long ticket_unread_system) {
        this.ticket_unread_system = ticket_unread_system;
    }

    public Long getTicket_latest() {
        return ticket_latest;
    }

    public void setTicket_latest(Long ticket_latest) {
        this.ticket_latest = ticket_latest;
    }

    public UUID getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(UUID admin_id) {
        this.admin_id = admin_id;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UUID getTicket_id() {
        return ticket_id;
    }

    public void setTicket_id(UUID ticket_id) {
        this.ticket_id = ticket_id;
    }

    public String getTicket_title() {
        return ticket_title;
    }

    public void setTicket_title(String ticket_title) {
        this.ticket_title = ticket_title;
    }

    public String getTicket_description() {
        return ticket_description;
    }

    public void setTicket_description(String ticket_description) {
        this.ticket_description = ticket_description;
    }

    public User getUser() {
        return user;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public UUID getParent_id() {
        return parent_id;
    }

    public void setParent_id(UUID parent_id) {
        this.parent_id = parent_id;
    }

    public Ticket getParent() {
        return parent;
    }

    public void setParent(Ticket parent) {
        this.parent = parent;
    }

    public Integer getTicket_state() {
        return ticket_state;
    }

    public void setTicket_state(Integer ticket_state) {
        this.ticket_state = ticket_state;
    }

    public Integer getTicket_status() {
        return ticket_status;
    }

    public void setTicket_status(Integer ticket_status) {
        this.ticket_status = ticket_status;
    }

    public Integer getTicket_owner() {
        return ticket_owner;
    }

    public JsonElement getTicket_attachments() {
        return ticket_attachments;
    }

    public void setTicket_attachments(JsonElement ticket_attachments) {
        this.ticket_attachments = ticket_attachments;
    }

    public void setTicket_owner(Integer ticket_owner) {
        this.ticket_owner = ticket_owner;
    }

    public Ticket() {
        super();
    }

    @Override
    public void merge(Ticket model) {
        super.merge(model);
        if (model.ticket_title != null) ticket_title = model.ticket_title;
        if (model.ticket_description != null) ticket_description = model.ticket_description;
        if (model.ticket_attachments != null) ticket_attachments = model.ticket_attachments;
    }
}
