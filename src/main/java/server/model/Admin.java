package server.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import server.lib.interfacing.Authorize;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.util.HashSet;
import java.util.UUID;

@Permission(permissionName = "مدیریت دسترسی", permissionBrief = "در این قسمت اپراتور و مدیران سیستم را تعریف نمایید", permissionPriority = 1000)
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "admins" + SchemaUtil.TABLE_POSTFIX)
public class Admin extends DatabaseModel<Admin> {

    public static final String USERNAME = "username";
    public static final String HASH = "admin_hash";
    public static final String NAME = "admin_name";
    public static final String ID = "admin_id";

    public static final String CRED_MESSAGE = "نام کاربری و یا رمز ورود شما اشتباه است";

    @DatabaseField(generatedId = true)
    private UUID admin_id;

    @DatabaseField(canBeNull = false)
    private String password;

    @DatabaseField(canBeNull = false, unique = true)
    private String username;

    @DatabaseField()
    private String admin_name;

    @DatabaseField()
    private String admin_hash;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String admin_brief;

    @DatabaseField()
    private String admin_avatar;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement admin_profile;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private UUID[] permissions;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement methods;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long auth_version;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Long latest_activity;

    private server.model.Permission[] accesses;

    public Long getLatest_activity() {
        return latest_activity;
    }

    public void setLatest_activity(Long latest_activity) {
        this.latest_activity = latest_activity;
    }

    public String getAdmin_hash() {
        return admin_hash;
    }

    public String getAdmin_brief() {
        return admin_brief;
    }

    public Long getAuth_version() {
        return auth_version;
    }

    public void setAuth_version(Long auth_version) {
        this.auth_version = auth_version;
    }

    public void setAdmin_hash(String admin_hash) {
        this.admin_hash = admin_hash;
    }

    public void setAdmin_brief(String admin_brief) {
        this.admin_brief = admin_brief;
    }

    public JsonElement getMethods() {
        return methods;
    }

    public void setMethods(JsonElement methods) {
        this.methods = methods;
    }

    public String getAdmin_avatar() {
        return admin_avatar;
    }

    public void setAdmin_avatar(String admin_avatar) {
        this.admin_avatar = admin_avatar;
    }

    public JsonElement getAdmin_profile() {
        return admin_profile;
    }

    public void setAdmin_profile(JsonElement admin_profile) {
        this.admin_profile = admin_profile;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public void setAdmin_name(String admin_name) {
        this.admin_name = admin_name;
    }

    public UUID getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(UUID admin_id) {
        this.admin_id = admin_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public server.model.Permission[] getAccesses() {
        return accesses;
    }

    public void setAccesses(server.model.Permission[] adminPermissions) {
        this.accesses = adminPermissions;
    }

    public UUID[] getPermissions() {
        return permissions;
    }

    public void setPermissions(UUID[] permissions) {
        this.permissions = permissions;
    }

    public HashSet<String> getMethods(UUID id) {
        HashSet<String> set = new HashSet<>();
        try {
            if (methods != null) {
                JsonArray array = methods.getAsJsonObject().getAsJsonArray(id.toString());
                if (array != null) {
                    for (JsonElement element : array) {
                        if (element != null) {
                            set.add(element.getAsString());
                        }
                    }
                }
            } else {
                set.add(Authorize.Method.CREATE.name());
                set.add(Authorize.Method.READ.name());
                set.add(Authorize.Method.UPDATE.name());
                set.add(Authorize.Method.DELETE.name());
            }
        } catch (Throwable ignored) {
        }
        return set;
    }

    public Admin() {
        super();
    }

    @Override
    public void merge(Admin model) {
        super.merge(model);
        admin_profile = model.admin_profile;
        admin_avatar = model.admin_avatar;
        admin_brief = model.admin_brief;
        admin_hash = model.admin_hash;
        admin_name = model.admin_name;
        if (model.password != null) {
            password = model.password;
        }
        if (model.username != null) {
            username = model.username;
        }
        if (model.methods != null) {
            methods = model.methods;
        }
        if (model.permissions != null) {
            permissions = model.permissions;
        }
    }

    @Override
    public Admin trim() {
        auth_version = null;
        permissions = null;
        password = null;
        return super.trim();
    }
}
