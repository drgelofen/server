package server.model;

import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;
import server.lib.utils.StringUtil;

@Permission(permissionName = "کاربران موقت")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "user_temps" + SchemaUtil.TABLE_POSTFIX)
public class UserTemp extends DatabaseModel<UserTemp> {

    public static final Integer TYPE_REGISTER = 1;
    public static final Integer TYPE_LOGIN = 0;

    public static final String PHONE = "phone";
    public static final String EMAIL = "email";

    public static final String NAME_MESSAGE = "نام کاربری باید حداقل 3 حرف باشد.";
    public static final String PHONE_MESSAGE = "شماره تلفن همراه خود را به صورت 11 رقمی وارد نمایید.";
    public static final String INFO_MESSAGE = "لطفا فرم ثبت نام را تکمیل نمایید.";
    public static final String MAIL_MESSAGE = "ایمیل وارد شده معتبر نمیباشد.";
    public static final String PASSWORD_MESSAGE = "رمز عبور باید حداقل 4 حرف باشد.";
    public static final String CAPTCHA_MESSAGE = "تعداد دفعات وارد کردن کد تایید شما از حد مجاز گذشته است، لطفا مجددا مراحل را از ابتدا انجام دهید.";

    @DatabaseField(generatedId = true)
    private Long temp_id;

    @DatabaseField()
    private Integer temp_type;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer temp_attempts;

    @DatabaseField()
    private String phone;

    @DatabaseField()
    private String email;

    @DatabaseField()
    private String username;

    @DatabaseField()
    private String password;

    @DatabaseField()
    private String captcha;

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getTemp_id() {
        return temp_id;
    }

    public void setTemp_id(Long temp_id) {
        this.temp_id = temp_id;
    }

    public Integer getTemp_type() {
        return temp_type;
    }

    public void setTemp_type(Integer temp_type) {
        this.temp_type = temp_type;
    }

    public Integer getTemp_attempts() {
        return temp_attempts;
    }

    public void setTemp_attempts(Integer temp_attempts) {
        this.temp_attempts = temp_attempts;
    }

    public UserTemp() {
        super();
    }

    @Override
    public void merge(UserTemp model) {
        super.merge(model);
    }

    public String verify() {
        if (StringUtil.isEmpty(email) && StringUtil.isEmpty(phone)) {
            return INFO_MESSAGE;
        }
        username = StringUtil.format(username);
        password = StringUtil.format(password);
        email = StringUtil.format(email);
        phone = StringUtil.format(phone);
        if (StringUtil.isEmpty(username) || username.length() < 3) {
            return NAME_MESSAGE;
        }
        if (!StringUtil.isEmpty(phone)) {
            if (!phone.startsWith("09") || phone.length() != 11) {
                return PHONE_MESSAGE;
            }
        } else if (!StringUtil.isEmpty(email)) {
            if (!StringUtil.isEmail(email)) {
                return MAIL_MESSAGE;
            }
            if (StringUtil.isEmpty(password) || password.length() < 4) {
                return PASSWORD_MESSAGE;
            }
        }
        return null;
    }
}
