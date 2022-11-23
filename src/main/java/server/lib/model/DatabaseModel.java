package server.lib.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import server.Application;
import server.lib.orm.field.DatabaseField;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;

public abstract class DatabaseModel<T> implements Serializable {

    public static final long serialVersionUID = 1;

    public static final String CREATE_AT = "create_at";
    public static final String UPDATE_AT = "update_at";

    @DatabaseField(canBeNull = false)
    private Long create_at;

    @DatabaseField(canBeNull = false)
    private Long update_at;

    public Long getCreate_at() {
        return create_at;
    }

    public void setCreate_at(Long create_at) {
        this.create_at = create_at;
    }

    public Long getUpdate_at() {
        return update_at;
    }

    public void setUpdate_at(Long update_at) {
        this.update_at = update_at;
    }

    public DatabaseModel() {
    }

    public final void init() {
        if (create_at == null)
            create_at = Instant.now().toEpochMilli();
        if (update_at == null)
            update_at = create_at;
    }

    public void merge(T model) {
        update_at = Instant.now().toEpochMilli();
    }

    public JsonObject toJsonObject() {
        return JsonParser.parseString(Application.GSON.toJson(this)).getAsJsonObject();
    }

    public T copy() {
        Class<T> persistentClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        String json = Application.GSON.toJson(this);
        return Application.GSON.fromJson(json, persistentClass);
    }

    public T trim() {
        update_at = null;
        return (T) this;
    }
}
