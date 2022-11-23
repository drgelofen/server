package server.model;

import server.lib.model.DatabaseModel;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.lang.annotation.Annotation;
import java.util.UUID;

@server.lib.interfacing.Permission(permissionName = "دسترسی داخلی")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "permissions" + SchemaUtil.TABLE_POSTFIX)
public class Permission extends DatabaseModel<Permission> {

    @DatabaseField(generatedId = true)
    private UUID permission_id;

    @DatabaseField()
    private String permission_class;

    @DatabaseField()
    private String permission_name;

    @DatabaseField()
    private String permission_brief;

    @DatabaseField()
    private Integer permission_priority;

    public String getPermission_class() {
        return permission_class;
    }

    public void setPermission_class(String permission_class) {
        this.permission_class = permission_class;
    }

    public String getPermission_name() {
        return permission_name;
    }

    public void setPermission_name(String permission_name) {
        this.permission_name = permission_name;
    }

    public String getPermission_brief() {
        return permission_brief;
    }

    public void setPermission_brief(String permission_brief) {
        this.permission_brief = permission_brief;
    }

    public UUID getPermission_id() {
        return permission_id;
    }

    public void setPermission_id(UUID permission_id) {
        this.permission_id = permission_id;
    }

    public Integer getPermission_priority() {
        return permission_priority;
    }

    public void setPermission_priority(Integer permission_priority) {
        this.permission_priority = permission_priority;
    }

    public Permission() {
        super();
    }

    @Override
    public void merge(Permission model) {
        super.merge(model);
    }

    public static Permission from(Class table) {
        Annotation[] annotations = table.getAnnotations();
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof server.lib.interfacing.Permission
                        && !((server.lib.interfacing.Permission) annotation).permissionBrief()
                        .equalsIgnoreCase(server.lib.interfacing.Permission.VOID)) {
                    Permission permission = new Permission();
                    permission.setPermission_priority(((server.lib.interfacing.Permission) annotation).permissionPriority());
                    permission.setPermission_brief(((server.lib.interfacing.Permission) annotation).permissionBrief());
                    permission.setPermission_name(((server.lib.interfacing.Permission) annotation).permissionName());
                    permission.setPermission_class(table.getSimpleName());
                    return permission;
                }
            }
        }
        return null;
    }

    @Override
    public Permission trim() {
        permission_priority = null;
        permission_brief = null;
        permission_class = null;
        return super.trim();
    }
}
