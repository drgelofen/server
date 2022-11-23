package server.model;

import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "دسته بندی بلاگ", permissionBrief = "در این قسمت دسته بندی بلاگ و محتوای این بخش را مدیریت کنید", permissionPriority = 550)
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "blog_categories" + SchemaUtil.TABLE_POSTFIX)
public class BlogCategory extends DatabaseModel<BlogCategory> {

    public static final String VISIBILITY = "visibility";

    public static final String PARENT_ID = "parent_id";
    public static final String ID = "category_id";
    public static final String NAME = "category_name";
    public static final String PRIORITY = "priority";
    public static final String CODE = "category_code";

    @DatabaseField(generatedId = true)
    private UUID category_id;

    @DatabaseField(canBeNull = false, unique = true)
    private String category_name;

    @DatabaseField()
    private String category_color;

    @DatabaseField()
    private String category_icon;

    @DatabaseField()
    private String category_poster;

    @DatabaseField()
    private String category_cover;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long category_views;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String category_brief;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private String[] category_tags;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String category_description;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer priority;

    @DatabaseField(foreign = true)
    private BlogCategory parent;

    @DatabaseField(canBeNull = false, defaultValue = "true")
    private Boolean visibility;

    @DatabaseField()
    private String category_code;

    private UUID parent_id;
    private Long category_count;

    public String getCategory_code() {
        return category_code;
    }

    public void setCategory_code(String category_code) {
        this.category_code = category_code;
    }

    public void setCategory_count(Long category_count) {
        this.category_count = category_count;
    }

    public void setCategory_count(long category_count) {
        this.category_count = category_count;
    }

    public long getCategory_count() {
        return category_count;
    }

    public UUID getParent_id() {
        return parent_id;
    }

    public void setParent_id(UUID parent_id) {
        this.parent_id = parent_id;
    }

    public BlogCategory getParent() {
        return parent;
    }

    public void setParent(BlogCategory parent) {
        this.parent = parent;
    }

    public String[] getCategory_tags() {
        return category_tags;
    }

    public void setCategory_tags(String[] category_tags) {
        this.category_tags = category_tags;
    }

    public Long getCategory_views() {
        return category_views;
    }

    public void setCategory_views(Long category_views) {
        this.category_views = category_views;
    }

    public String getCategory_cover() {
        return category_cover;
    }

    public void setCategory_cover(String category_cover) {
        this.category_cover = category_cover;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public UUID getCategory_id() {
        return category_id;
    }

    public void setCategory_id(UUID category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getCategory_color() {
        return category_color;
    }

    public void setCategory_color(String category_color) {
        this.category_color = category_color;
    }

    public String getCategory_icon() {
        return category_icon;
    }

    public void setCategory_icon(String category_icon) {
        this.category_icon = category_icon;
    }

    public String getCategory_brief() {
        return category_brief;
    }

    public void setCategory_brief(String category_brief) {
        this.category_brief = category_brief;
    }

    public String getCategory_description() {
        return category_description;
    }

    public String getCategory_poster() {
        return category_poster;
    }

    public void setCategory_poster(String category_poster) {
        this.category_poster = category_poster;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public void setCategory_description(String category_description) {
        this.category_description = category_description;
    }

    public BlogCategory() {
        super();
    }

    @Override
    public void merge(BlogCategory info) {
        super.merge(info);
        parent = info.parent;
        category_description = info.category_description;
        category_name = info.category_name;
        category_brief = info.category_brief;
        category_color = info.category_color;
        category_poster = info.category_poster;
        category_cover = info.category_cover;
        category_icon = info.category_icon;
        category_tags = info.category_tags;
        visibility = info.visibility;
        priority = info.priority;
    }

    @Override
    public BlogCategory trim() {
        if (parent != null) parent.trim();
        category_description = null;
        category_views = null;
        visibility = null;
        priority = null;
        return super.trim();
    }
}
