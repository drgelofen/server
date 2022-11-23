package server.model;

import com.google.gson.JsonElement;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.utils.SchemaUtil;
import server.lib.utils.StringUtil;

import java.util.UUID;

@Permission(permissionName = "مدیریت کاربران", permissionBrief = "در این بخش کاربران خود را مشاهده و عملیات مربوط به کاربران را انجام دهید", permissionPriority = 750)
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "users" + SchemaUtil.TABLE_POSTFIX)
public class User extends DatabaseModel<User> {

    public static final String SHARE_TOKEN = "user_share_id";

    public static final String PURCHASED_AMOUNT = "user_purchases_amount";
    public static final String SUBSCRIPTION = "user_subscription";
    public static final String INVITES = "user_invites";
    public static final String WALLET = "user_wallet";

    public static final String USERNAME = "username";
    public static final String SUSPEND = "suspend";
    public static final String LIMIT = "limit";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String ID = "user_id";

    public static final String SUSPEND_MESSAGE = "کاربر گرامی، پروفایل شما محدود شده است، جهت پیگیری با پشتیبانی تماس حاصل فرمایید.";
    public static final String LIMIT_MESSAGE = "کاربر گرامی، قابلیت نظردهی برای شما غیرفعال شده است، جهت اطلاعات بیشتر با پشتیبانی تماس حاصل فرمایید.";
    public static final String EXIST_MESSAGE = "کاربری با این مشخصات در سیستم ثبت نشده است، لطفا ابتدا ثبت نام کنید.";
    public static final String PASSWORD_MESSAGE = "رمز عبور قبلی وارد شده صحیح نمیباشد.";
    public static final String EMAIL_MESSAGE = "ایمیل وارد شده در سیستم وجود دارد.";
    public static final String PHONE_MESSAGE = "شماره وارد شده در سیستم وجود دارد.";
    public static final String CAPTCHA_MESSAGE = "کد تایید وارد شده صحیح نمیباشد.";
    public static final String AUTH_MESSAGE = "ایمیل و یا رمز عبور وارد شده صحیح نمیباشد.";
    public static final String COMMENT_MESSAGE = "کاربر گرامی، شما اجازه ارسال کامنت را ندارید، جهت اطلاعات بیشتر با پشتیبانی تماس حاصل فرمایید.";

    @DatabaseField(generatedId = true)
    private UUID user_id;

    @DatabaseField(unique = true)
    private String phone;

    @DatabaseField(unique = true)
    private String email;

    @DatabaseField(canBeNull = false)
    private String username;

    @DatabaseField()
    private String password;

    @DatabaseField()
    private String user_avatar;

    @DatabaseField()
    private String user_poster;

    @DatabaseField()
    private String user_cover;

    @DatabaseField()
    private String user_icon;

    @DatabaseField(canBeNull = false, unique = true)
    private String user_share_id;

    @DatabaseField()
    private String user_pushId;

    @DatabaseField(defaultValue = "0")
    private Integer user_wallet;

    @DatabaseField(defaultValue = "0")
    private Integer user_score;

    @DatabaseField(defaultValue = "0")
    private Integer user_invite_score;

    @DatabaseField(defaultValue = "0")
    private Integer user_invite_wallet;

    @DatabaseField(defaultValue = "0")
    private Integer user_purchased_invites;

    @DatabaseField(defaultValue = "0")
    private Integer user_invites;

    @DatabaseField(defaultValue = "0")
    private Integer user_purchases;

    @DatabaseField(defaultValue = "0")
    private Long user_purchases_amount;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement user_profile;

    @DatabaseField(canBeNull = false, defaultValue = "false")
    private Boolean suspend;

    @DatabaseField(canBeNull = false, defaultValue = "false")
    private Boolean limit;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long auth_version;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long user_subscription;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long latest_activity;

    @DatabaseField
    private String temp_id;

    private Long remainSubscription;
    private Integer unreadMessages;
    private Integer unreadTickets;
    private Boolean hasCoach;
    private String oldPassword;
    private JsonElement condition;

    public Boolean getHasCoach() {
        return hasCoach;
    }

    public void setHasCoach(Boolean hasCoach) {
        this.hasCoach = hasCoach;
    }

    public Long getLatest_activity() {
        return latest_activity;
    }

    public void setLatest_activity(Long latest_activity) {
        this.latest_activity = latest_activity;
    }

    public String getTemp_id() {
        return temp_id;
    }

    public void setTemp_id(String temp_id) {
        this.temp_id = temp_id;
    }

    public Long getUser_subscription() {
        return user_subscription;
    }

    public void setUser_subscription(Long user_subscription) {
        this.user_subscription = user_subscription;
    }

    public Long getAuth_version() {
        return auth_version;
    }

    public void setAuth_version(Long auth_version) {
        this.auth_version = auth_version;
    }

    public Integer getUser_purchases() {
        return user_purchases;
    }

    public void setUser_purchases(Integer user_purchases) {
        this.user_purchases = user_purchases;
    }

    public Long getUser_purchases_amount() {
        return user_purchases_amount;
    }

    public void setUser_purchases_amount(Long user_purchases_amount) {
        this.user_purchases_amount = user_purchases_amount;
    }

    public Long getRemainSubscription() {
        return remainSubscription;
    }

    public void setRemainSubscription(Long remainSubscription) {
        this.remainSubscription = remainSubscription;
    }

    public Boolean getLimit() {
        return limit;
    }

    public void setLimit(Boolean limit) {
        this.limit = limit;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getSuspend() {
        return suspend;
    }

    public void setSuspend(Boolean suspend) {
        this.suspend = suspend;
    }

    public Integer getUser_invite_wallet() {
        return user_invite_wallet;
    }

    public void setUser_invite_wallet(Integer user_invite_wallet) {
        this.user_invite_wallet = user_invite_wallet;
    }

    public Integer getUser_invite_score() {
        return user_invite_score;
    }

    public void setUser_invite_score(Integer user_invite_score) {
        this.user_invite_score = user_invite_score;
    }

    public String getUser_share_id() {
        return user_share_id;
    }

    public void setUser_share_id(String user_share_id) {
        this.user_share_id = user_share_id;
    }

    public Integer getUser_score() {
        return user_score;
    }

    public void setUser_score(Integer user_score) {
        this.user_score = user_score;
    }

    public Integer getUnreadTickets() {
        return unreadTickets;
    }

    public void setUnreadTickets(Integer unreadTickets) {
        this.unreadTickets = unreadTickets;
    }

    public void setUnreadMessages(Integer unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public Integer getUnreadMessages() {
        return unreadMessages;
    }

    public Integer getUser_invites() {
        return user_invites;
    }

    public void setUser_invites(Integer user_invites) {
        this.user_invites = user_invites;
    }

    public Integer getUser_purchased_invites() {
        return user_purchased_invites;
    }

    public void setUser_purchased_invites(Integer user_purchased_invites) {
        this.user_purchased_invites = user_purchased_invites;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public Integer getUser_wallet() {
        return user_wallet;
    }

    public void setUser_wallet(Integer user_wallet) {
        this.user_wallet = user_wallet;
    }

    public String getUser_pushId() {
        return user_pushId;
    }

    public void setUser_pushId(String user_pushId) {
        this.user_pushId = user_pushId;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_poster() {
        return user_poster;
    }

    public void setUser_poster(String user_poster) {
        this.user_poster = user_poster;
    }

    public String getUser_avatar() {
        return user_avatar;
    }

    public void setUser_avatar(String user_avatar) {
        this.user_avatar = user_avatar;
    }

    public JsonElement getUser_profile() {
        return user_profile;
    }

    public void setCondition(JsonElement condition) {
        this.condition = condition;
    }

    public JsonElement getCondition() {
        return condition;
    }

    public void setUser_profile(JsonElement user_profile) {
        this.user_profile = user_profile;
    }

    public String getUser_cover() {
        return user_cover;
    }

    public void setUser_cover(String user_cover) {
        this.user_cover = user_cover;
    }

    public String getUser_icon() {
        return user_icon;
    }

    public void setUser_icon(String user_icon) {
        this.user_icon = user_icon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User() {
        super();
    }

    @Override
    public void merge(User info) {
        super.merge(info);
        if (info.user_profile != null) {
            user_profile = info.user_profile;
        }
        if (info.user_avatar != null) {
            user_avatar = info.user_avatar;
        }
        if (info.user_poster != null) {
            user_poster = info.user_poster;
        }
        if (info.username != null) {
            username = info.username;
        }
        if (info.user_cover != null) {
            user_cover = info.user_cover;
        }
        if (info.user_icon != null) {
            user_icon = info.user_icon;
        }
        if (info.user_pushId != null) {
            user_pushId = info.user_pushId;
        }
    }

    @Override
    public User trim() {
        auth_version = null;
        user_pushId = null;
        password = null;
        suspend = null;
        user_id = null;
        limit = null;
        return super.trim();
    }

    public String verify() {
        password = StringUtil.format(password);
        email = StringUtil.format(email);
        phone = StringUtil.format(phone);
        if (StringUtil.isEmpty(phone) && StringUtil.isEmpty(email)) {
            return UserTemp.INFO_MESSAGE;
        }
        if (!StringUtil.isEmpty(phone)) {
            if (!phone.startsWith("09") || phone.length() != 11) {
                return UserTemp.PHONE_MESSAGE;
            }
        } else if (!StringUtil.isEmpty(email)) {
            if (!StringUtil.isEmail(email)) {
                return UserTemp.MAIL_MESSAGE;
            }
        }
        if (!StringUtil.isEmpty(password)) {
            if (password.length() < 4) {
                return UserTemp.PASSWORD_MESSAGE;
            }
        }
        return null;
    }
}
