package server.model;

import com.google.gson.JsonElement;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.ArrayPersister;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.util.List;
import java.util.UUID;

@Permission(permissionName = "مدیریت پزشکان", permissionBrief = "در این بخش پزشکان را مشاهده و عملیات مربوط به پزشکان را انجام دهید", permissionPriority = 750)
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "doctors" + SchemaUtil.TABLE_POSTFIX)
public class Doctor extends DatabaseModel<Doctor> {

    public static final String SUSPEND_MESSAGE = "پزشک گرامی، پروفایل شما محدود شده است، جهت پیگیری با پشتیبانی تماس حاصل فرمایید.";
    public static final String CRED_MESSAGE = "نام کاربری و یا رمز ورود شما اشتباه است";

    public static final String CATEGORIES = "doctor_categories";
    public static final String VISIBILITY = "visibility";
    public static final String USERNAME = "username";
    public static final String PRIORITY = "priority";
    public static final String NAME = "doctor_name";
    public static final String ID = "doctor_id";

    @DatabaseField(generatedId = true)
    private UUID doctor_id;

    @DatabaseField(canBeNull = false)
    private String doctor_name;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String doctor_brief;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String doctor_share;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String doctor_description;

    @DatabaseField()
    private String doctor_credential;

    @DatabaseField(canBeNull = false, unique = true)
    private String username;

    @DatabaseField()
    private String doctor_gender;

    @DatabaseField(canBeNull = false, defaultValue = "true")
    private Boolean doctor_availability;

    @DatabaseField(persisterClass = ArrayPersister.class)
    private UUID[] doctor_categories;

    @DatabaseField(defaultValue = "5")
    private Float doctor_rate;

    @DatabaseField(defaultValue = "0")
    private Long doctor_views;

    @DatabaseField(defaultValue = "0")
    private Long doctor_patients;

    @DatabaseField(defaultValue = "0")
    private Long doctor_voters;

    @DatabaseField(canBeNull = false)
    private String password;

    @DatabaseField()
    private String doctor_avatar;

    @DatabaseField()
    private String doctor_poster;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private String[] doctor_posters;

    @DatabaseField()
    private String doctor_pushId;

    @DatabaseField()
    private String doctor_clinic;

    @DatabaseField()
    private String doctor_code;

    @DatabaseField(defaultValue = "0")
    private Integer doctor_income;

    @DatabaseField(defaultValue = "0")
    private Integer doctor_wallet;

    @DatabaseField(defaultValue = "0")
    private Integer doctor_commission;

    @DatabaseField(defaultValue = "0")
    private Integer doctor_score;

    @DatabaseField(defaultValue = "0")
    private Integer doctor_sells;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement doctor_profile;

    @DatabaseField(canBeNull = false, defaultValue = "false")
    private Boolean suspend;

    @DatabaseField(canBeNull = false, defaultValue = "false")
    private Boolean visibility;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long doctor_favorites;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer priority;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long auth_version;

    @DatabaseField()
    private String doctor_cv;

    @DatabaseField()
    private String doctor_website;

    @DatabaseField()
    private String doctor_worktable;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long latest_activity;

    private List<DoctorCategory> categories;

    private Integer unreadMessages;
    private UserDoctor history;
    private Integer unreadTickets;

    public Long getDoctor_voters() {
        return doctor_voters;
    }

    public void setDoctor_voters(Long doctor_voters) {
        this.doctor_voters = doctor_voters;
    }

    public void setHistory(UserDoctor history) {
        this.history = history;
    }

    public UserDoctor getHistory() {
        return history;
    }

    public Integer getDoctor_income() {
        return doctor_income;
    }

    public void setDoctor_income(Integer doctor_income) {
        this.doctor_income = doctor_income;
    }

    public Integer getDoctor_sells() {
        return doctor_sells;
    }

    public void setDoctor_sells(Integer doctor_sells) {
        this.doctor_sells = doctor_sells;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getDoctor_commission() {
        return doctor_commission;
    }

    public void setDoctor_commission(Integer doctor_commission) {
        this.doctor_commission = doctor_commission;
    }

    public Long getLatest_activity() {
        return latest_activity;
    }

    public void setLatest_activity(Long latest_activity) {
        this.latest_activity = latest_activity;
    }

    public Long getDoctor_favorites() {
        return doctor_favorites;
    }

    public void setDoctor_favorites(Long doctor_favorites) {
        this.doctor_favorites = doctor_favorites;
    }

    public String getDoctor_poster() {
        return doctor_poster;
    }

    public void setDoctor_poster(String doctor_poster) {
        this.doctor_poster = doctor_poster;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public String getDoctor_clinic() {
        return doctor_clinic;
    }

    public void setDoctor_clinic(String doctor_clinic) {
        this.doctor_clinic = doctor_clinic;
    }

    public String getDoctor_code() {
        return doctor_code;
    }

    public void setDoctor_code(String doctor_code) {
        this.doctor_code = doctor_code;
    }

    public String getDoctor_cv() {
        return doctor_cv;
    }

    public void setDoctor_cv(String doctor_cv) {
        this.doctor_cv = doctor_cv;
    }

    public String getDoctor_website() {
        return doctor_website;
    }

    public void setDoctor_website(String doctor_website) {
        this.doctor_website = doctor_website;
    }

    public String getDoctor_worktable() {
        return doctor_worktable;
    }

    public void setDoctor_worktable(String doctor_worktable) {
        this.doctor_worktable = doctor_worktable;
    }

    public UUID getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(UUID doctor_id) {
        this.doctor_id = doctor_id;
    }

    public String getDoctor_name() {
        return doctor_name;
    }

    public void setDoctor_name(String doctor_name) {
        this.doctor_name = doctor_name;
    }

    public String getDoctor_brief() {
        return doctor_brief;
    }

    public void setDoctor_brief(String doctor_brief) {
        this.doctor_brief = doctor_brief;
    }

    public String getDoctor_description() {
        return doctor_description;
    }

    public void setDoctor_description(String doctor_description) {
        this.doctor_description = doctor_description;
    }

    public String getDoctor_credential() {
        return doctor_credential;
    }

    public void setDoctor_credential(String doctor_credential) {
        this.doctor_credential = doctor_credential;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDoctor_gender() {
        return doctor_gender;
    }

    public void setDoctor_gender(String doctor_gender) {
        this.doctor_gender = doctor_gender;
    }

    public Boolean getDoctor_availability() {
        return doctor_availability;
    }

    public void setDoctor_availability(Boolean doctor_availability) {
        this.doctor_availability = doctor_availability;
    }

    public UUID[] getDoctor_categories() {
        return doctor_categories;
    }

    public void setDoctor_categories(UUID[] doctor_categories) {
        this.doctor_categories = doctor_categories;
    }

    public Float getDoctor_rate() {
        return doctor_rate;
    }

    public void setDoctor_rate(Float doctor_rate) {
        this.doctor_rate = doctor_rate;
    }

    public Long getDoctor_views() {
        return doctor_views;
    }

    public void setDoctor_views(Long doctor_views) {
        this.doctor_views = doctor_views;
    }

    public Long getDoctor_patients() {
        return doctor_patients;
    }

    public void setDoctor_patients(Long doctor_patients) {
        this.doctor_patients = doctor_patients;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDoctor_avatar() {
        return doctor_avatar;
    }

    public void setDoctor_avatar(String doctor_avatar) {
        this.doctor_avatar = doctor_avatar;
    }

    public String[] getDoctor_posters() {
        return doctor_posters;
    }

    public void setDoctor_posters(String[] doctor_posters) {
        this.doctor_posters = doctor_posters;
    }

    public String getDoctor_pushId() {
        return doctor_pushId;
    }

    public void setDoctor_pushId(String doctor_pushId) {
        this.doctor_pushId = doctor_pushId;
    }

    public Integer getDoctor_wallet() {
        return doctor_wallet;
    }

    public void setDoctor_wallet(Integer doctor_wallet) {
        this.doctor_wallet = doctor_wallet;
    }

    public Integer getDoctor_score() {
        return doctor_score;
    }

    public void setDoctor_score(Integer doctor_score) {
        this.doctor_score = doctor_score;
    }

    public JsonElement getDoctor_profile() {
        return doctor_profile;
    }

    public void setDoctor_profile(JsonElement doctor_profile) {
        this.doctor_profile = doctor_profile;
    }

    public Boolean getSuspend() {
        return suspend;
    }

    public void setSuspend(Boolean suspend) {
        this.suspend = suspend;
    }

    public Long getAuth_version() {
        return auth_version;
    }

    public void setAuth_version(Long auth_version) {
        this.auth_version = auth_version;
    }

    public String getDoctor_share() {
        return doctor_share;
    }

    public void setDoctor_share(String doctor_share) {
        this.doctor_share = doctor_share;
    }

    public void setCategories(List<DoctorCategory> categories) {
        this.categories = categories;
    }

    public void setUnreadMessages(Integer unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public Integer getUnreadMessages() {
        return unreadMessages;
    }

    public List<DoctorCategory> getCategories() {
        return categories;
    }

    public void setUnreadTickets(Integer unreadTickets) {
        this.unreadTickets = unreadTickets;
    }

    public Integer getUnreadTickets() {
        return unreadTickets;
    }

    public Doctor() {
        super();
    }

    @Override
    public void merge(Doctor info) {
        super.merge(info);
        if (info.password != null) {
            password = info.password;
        }
        doctor_share = info.doctor_share;
        username = info.username;
        doctor_name = info.doctor_name;
        doctor_brief = info.doctor_brief;
        doctor_description = info.doctor_description;
        doctor_website = info.doctor_website;
        doctor_cv = info.doctor_cv;
        suspend = info.suspend;
        doctor_profile = info.doctor_profile;
        doctor_score = info.doctor_score;
        doctor_wallet = info.doctor_wallet;
        doctor_code = info.doctor_code;
        doctor_clinic = info.doctor_clinic;
        doctor_worktable = info.doctor_worktable;
        doctor_posters = info.doctor_posters;
        doctor_avatar = info.doctor_avatar;
        doctor_availability = info.doctor_availability;
        doctor_credential = info.doctor_credential;
        doctor_gender = info.doctor_gender;
        doctor_categories = info.doctor_categories;
        doctor_poster = info.doctor_poster;
        visibility = info.visibility;
        doctor_commission = info.doctor_commission;
        priority = info.priority;
        doctor_rate = info.doctor_rate;
    }

    @Override
    public Doctor trim() {
        hide();
        doctor_cv = null;
        doctor_website = null;
        doctor_profile = null;
        doctor_code = null;
        doctor_clinic = null;
        doctor_worktable = null;
        doctor_posters = null;
        doctor_description = null;
        doctor_categories = null;
        doctor_share = null;
        return super.trim();
    }

    public Doctor hide() {
        if (categories != null) for (DoctorCategory category : categories) if (category != null) category.trim();
        doctor_sells = null;
        doctor_income = null;
        doctor_commission = null;
        doctor_credential = null;
        doctor_wallet = null;
        doctor_score = null;
        auth_version = null;
        username = null;
        password = null;
        suspend = null;
        priority = null;
        return this;
    }
}
