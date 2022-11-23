package server.model;

import com.google.gson.JsonElement;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "تنظیمات", permissionBrief = "در این قسمت تنظیمات مربوط به نرم افزار را مدیریت کنید")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "settings" + SchemaUtil.TABLE_POSTFIX)
public class Setting extends DatabaseModel<Setting> {

    public static final String CANCEL_MESSAGE = "cancel_message";
    public static final String REMAIN_TIME = "remain_time";
    public static final String CANCEL_LIMIT = "limit_time";

    @DatabaseField(generatedId = true)
    private UUID setting_id;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement repository;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement information;

    public JsonElement getRepository() {
        return repository;
    }

    public void setRepository(JsonElement repository) {
        this.repository = repository;
    }

    public JsonElement getInformation() {
        return information;
    }

    public void setInformation(JsonElement information) {
        this.information = information;
    }

    public UUID getSetting_id() {
        return setting_id;
    }

    public void setSetting_id(UUID setting_id) {
        this.setting_id = setting_id;
    }

    public Setting() {
        super();
    }

    @Override
    public void merge(Setting model) {
        super.merge(model);
        if (model.information != null) {
            information = model.information;
        }
        if (model.repository != null) {
            repository = model.repository;
        }
    }

    @Override
    public Setting trim() {
        setting_id = null;
        return super.trim();
    }

    public String getField(String column) {
        try {
            return getInformation().getAsJsonObject().getAsJsonPrimitive(column).getAsString();
        } catch (Throwable ignored) {
        }
        return null;
    }

    public int getFieldAsInt(String column) {
        try {
            return Integer.parseInt(getField(column));
        } catch (Throwable ignored) {
        }
        return 0;
    }
}
