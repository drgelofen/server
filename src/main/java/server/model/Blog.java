package server.model;

import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "مدیریت بلاگ", permissionBrief = "در این قسمت بلاگ و محتوای بخش بلاگ را مدیریت کنید", permissionPriority = 500)
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "blogs" + SchemaUtil.TABLE_POSTFIX)
public class Blog extends DatabaseModel<Blog> {

    public static final String VISIBILITY = "visibility";

    public static final String CATEGORY_ID = "category_id";
    public static final String PRIORITY = "priority";
    public static final String DOCTOR = "doctor_id";
    public static final String TITLE = "blog_title";
    public static final String CODE = "blog_code";
    public static final String ID = "blog_id";

    @DatabaseField(generatedId = true)
    private UUID blog_id;

    @DatabaseField(canBeNull = false)
    private String blog_title;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String blog_description;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String blog_brief;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String blog_share;

    @DatabaseField(canBeNull = false, dataType = DataType.LONG_STRING)
    private String blog_content;

    @DatabaseField()
    private String blog_icon;

    @DatabaseField()
    private String blog_cover;

    @DatabaseField()
    private String blog_poster;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private String[] blog_tags;

    @DatabaseField(foreign = true, canBeNull = false)
    private BlogCategory category;

    @DatabaseField(foreign = true)
    private Doctor doctor;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer priority;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long blog_views;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long blog_favorites;

    @DatabaseField(canBeNull = false, defaultValue = "true")
    private Boolean visibility;

    @DatabaseField()
    private String blog_code;

    private UUID category_id, doctor_id;
    private String create_at_str;

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

    public String getBlog_code() {
        return blog_code;
    }

    public void setBlog_code(String blog_code) {
        this.blog_code = blog_code;
    }

    public String getCreate_at_str() {
        return create_at_str;
    }

    public void setCreate_at_str(String create_at_str) {
        this.create_at_str = create_at_str;
    }

    public Long getBlog_favorites() {
        return blog_favorites;
    }

    public void setBlog_favorites(Long blog_favorites) {
        this.blog_favorites = blog_favorites;
    }

    public String getBlog_share() {
        return blog_share;
    }

    public void setBlog_share(String blog_share) {
        this.blog_share = blog_share;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public Long getBlog_views() {
        return blog_views;
    }

    public void setBlog_views(Long blog_views) {
        this.blog_views = blog_views;
    }

    public String[] getBlog_tags() {
        return blog_tags;
    }

    public void setBlog_tags(String[] blog_tags) {
        this.blog_tags = blog_tags;
    }

    public UUID getCategory_id() {
        return category_id;
    }

    public void setCategory_id(UUID category_id) {
        this.category_id = category_id;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getBlog_cover() {
        return blog_cover;
    }

    public void setBlog_cover(String blog_cover) {
        this.blog_cover = blog_cover;
    }

    public String getBlog_poster() {
        return blog_poster;
    }

    public void setBlog_poster(String blog_poster) {
        this.blog_poster = blog_poster;
    }

    public UUID getBlog_id() {
        return blog_id;
    }

    public void setBlog_id(UUID blog_id) {
        this.blog_id = blog_id;
    }

    public String getBlog_title() {
        return blog_title;
    }

    public void setBlog_title(String blog_title) {
        this.blog_title = blog_title;
    }

    public String getBlog_description() {
        return blog_description;
    }

    public void setBlog_description(String blog_description) {
        this.blog_description = blog_description;
    }

    public String getBlog_brief() {
        return blog_brief;
    }

    public void setBlog_brief(String blog_brief) {
        this.blog_brief = blog_brief;
    }

    public String getBlog_content() {
        return blog_content;
    }

    public void setBlog_content(String blog_content) {
        this.blog_content = blog_content;
    }

    public String getBlog_icon() {
        return blog_icon;
    }

    public void setBlog_icon(String blog_icon) {
        this.blog_icon = blog_icon;
    }

    public BlogCategory getCategory() {
        return category;
    }

    public void setCategory(BlogCategory category) {
        this.category = category;
    }

    public Blog() {
        super();
    }

    @Override
    public void merge(Blog info) {
        super.merge(info);
        doctor = info.doctor;
        category = info.category;
        blog_tags = info.blog_tags;
        blog_poster = info.blog_poster;
        blog_brief = info.blog_brief;
        blog_content = info.blog_content;
        blog_title = info.blog_title;
        blog_cover = info.blog_cover;
        blog_icon = info.blog_icon;
        blog_views = info.blog_views;
        blog_description = info.blog_description;
        priority = info.priority;
        blog_share = info.blog_share;
        visibility = info.visibility;
    }

    @Override
    public Blog trim() {
        if(category != null) category.trim();
        blog_description = null;
        blog_content = null;
        visibility = null;
        blog_tags = null;
        priority = null;
        return super.trim();
    }
}
