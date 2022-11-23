package server.model;

import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "مدیریت بسته ها", permissionBrief = "در این قسمت بسته های اشتراکی را مدیریت کنید", permissionPriority = 300)
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "packages" + SchemaUtil.TABLE_POSTFIX)
public class SubPackage extends DatabaseModel<SubPackage> {

    public static final String VISIBILITY = "visibility";
    public static final String NAME = "package_name";
    public static final String DOCTOR = "doctor_id";
    public static final String ID = "package_id";
    public static final String MESSAGE = "کاربر گرامی بسته شما به اتمام رسیده است، برای ارسال پیام ابتدا میبایست پکیچ مرتبط را خریداری نمایید.";
    public static final String EXPIRE = "کاربر گرامی مدت زمان بسته شما به اتمام رسیده است، برای ارسال پیام ابتدا میبایست پکیچ مرتبط را خریداری نمایید.";

    @DatabaseField(generatedId = true)
    private UUID package_id;

    @DatabaseField(foreign = true, canBeNull = false)
    private Doctor doctor;

    @DatabaseField(canBeNull = false)
    private String package_name;

    @DatabaseField(canBeNull = false)
    private Integer package_price;

    @DatabaseField(canBeNull = false)
    private Integer package_discount;

    @DatabaseField(canBeNull = false)
    private Integer package_sell_price;

    @DatabaseField()
    private String package_icon;

    @DatabaseField()
    private String package_cover;

    @DatabaseField()
    private String package_poster;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String package_brief;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String package_description;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long package_views;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long package_purchase;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer priority;

    @DatabaseField(canBeNull = false, defaultValue = "true")
    private Boolean visibility;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer package_score;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer package_subscription;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer package_character;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer package_video;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer package_voice;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer package_image;

    private UUID doctor_id;

    public Integer getPackage_subscription() {
        return package_subscription;
    }

    public void setPackage_subscription(Integer package_subscription) {
        this.package_subscription = package_subscription;
    }

    public Long getPackage_purchase() {
        return package_purchase;
    }

    public void setPackage_purchase(Long package_purchase) {
        this.package_purchase = package_purchase;
    }

    public Long getPackage_views() {
        return package_views;
    }

    public void setPackage_views(Long package_views) {
        this.package_views = package_views;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public UUID getPackage_id() {
        return package_id;
    }

    public void setPackage_id(UUID package_id) {
        this.package_id = package_id;
    }

    public Integer getPackage_price() {
        return package_price;
    }

    public void setPackage_price(Integer package_price) {
        this.package_price = package_price;
    }

    public Integer getPackage_discount() {
        return package_discount;
    }

    public void setPackage_discount(Integer package_discount) {
        this.package_discount = package_discount;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getPackage_icon() {
        return package_icon;
    }

    public void setPackage_icon(String package_icon) {
        this.package_icon = package_icon;
    }

    public String getPackage_cover() {
        return package_cover;
    }

    public void setPackage_cover(String package_cover) {
        this.package_cover = package_cover;
    }

    public String getPackage_poster() {
        return package_poster;
    }

    public void setPackage_poster(String package_poster) {
        this.package_poster = package_poster;
    }

    public String getPackage_brief() {
        return package_brief;
    }

    public void setPackage_brief(String package_brief) {
        this.package_brief = package_brief;
    }

    public String getPackage_description() {
        return package_description;
    }

    public void setPackage_description(String package_description) {
        this.package_description = package_description;
    }

    public Integer getPackage_sell_price() {
        return package_sell_price;
    }

    public void setPackage_sell_price(Integer package_sell_price) {
        this.package_sell_price = package_sell_price;
    }

    public Integer getPackage_score() {
        return package_score;
    }

    public void setPackage_score(Integer package_score) {
        this.package_score = package_score;
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

    public UUID getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(UUID doctor_id) {
        this.doctor_id = doctor_id;
    }

    public SubPackage() {
        super();
    }

    @Override
    public void merge(SubPackage info) {
        super.merge(info);
        priority = info.priority;
        visibility = info.visibility;
        doctor = info.doctor;
        package_price = info.package_price;
        package_discount = info.package_discount;
        package_score = info.package_score;
        package_name = info.package_name;
        package_icon = info.package_icon;
        package_brief = info.package_brief;
        package_description = info.package_description;
        package_poster = info.package_poster;
        package_cover = info.package_cover;
        package_sell_price = info.package_sell_price;
        package_voice = info.package_voice;
        package_video = info.package_video;
        package_character = info.package_character;
        package_image = info.package_image;
    }

    @Override
    public SubPackage trim() {
        if (doctor != null) doctor.trim();
        package_description = null;
        priority = null;
        visibility = null;
        package_views = null;
        package_purchase = null;
        return super.trim();
    }
}
