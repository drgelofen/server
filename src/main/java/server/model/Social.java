package server.model;

import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "مدیریت شبکه های اجتماعی", permissionBrief = "در این قسمت شبکه های اجتماعی و راههای تماس را تعریف نمایید", permissionPriority = 1000)
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "socials" + SchemaUtil.TABLE_POSTFIX)
public class Social extends DatabaseModel<Social> {

    public static final String VISIBILITY = "visibility";

    @DatabaseField(generatedId = true)
    private UUID social_id;

    @DatabaseField(canBeNull = false)
    private String social_title;

    @DatabaseField(canBeNull = false)
    private String social_url;

    @DatabaseField()
    private String social_avatar;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String social_brief;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String social_description;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long social_views;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer priority;

    @DatabaseField(canBeNull = false, defaultValue = "true")
    private Boolean visibility;

    public UUID getSocial_id() {
        return social_id;
    }

    public void setSocial_id(UUID social_id) {
        this.social_id = social_id;
    }

    public String getSocial_title() {
        return social_title;
    }

    public void setSocial_title(String social_title) {
        this.social_title = social_title;
    }

    public String getSocial_url() {
        return social_url;
    }

    public void setSocial_url(String social_url) {
        this.social_url = social_url;
    }

    public String getSocial_avatar() {
        return social_avatar;
    }

    public void setSocial_avatar(String social_avatar) {
        this.social_avatar = social_avatar;
    }

    public String getSocial_brief() {
        return social_brief;
    }

    public void setSocial_brief(String social_brief) {
        this.social_brief = social_brief;
    }

    public String getSocial_description() {
        return social_description;
    }

    public void setSocial_description(String social_description) {
        this.social_description = social_description;
    }

    public Long getSocial_views() {
        return social_views;
    }

    public void setSocial_views(Long social_views) {
        this.social_views = social_views;
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

    public Social() {
        super();
    }

    @Override
    public void merge(Social model) {
        super.merge(model);
        social_title = model.social_title;
        social_avatar = model.social_avatar;
        social_brief = model.social_brief;
        social_description = model.social_description;
        social_url = model.social_url;
        visibility = model.visibility;
        priority = model.priority;
    }

    @Override
    public Social trim() {
        social_description = null;
        priority = null;
        visibility = null;
        social_id = null;
        social_views = null;
        return super.trim();
    }
}
