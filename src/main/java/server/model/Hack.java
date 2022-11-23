package server.model;

import com.google.gson.JsonElement;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

@Permission(permissionName = "گزارش احتمال خرابکاری")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "hacks" + SchemaUtil.TABLE_POSTFIX)
public class Hack extends DatabaseModel<Hack> {

    public static final String ROUTE = "hack_route";
    public static final String AUTH = "hack_auth";
    public static final String IP = "hack_ip";

    @DatabaseField(generatedId = true)
    private Long hack_id;

    @DatabaseField()
    private String hack_route;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String hack_params;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String hack_body;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement hack_contact;

    @DatabaseField()
    private String hack_agent;

    @DatabaseField()
    private String hack_auth;

    @DatabaseField()
    private String hack_ip;

    public Long getHack_id() {
        return hack_id;
    }

    public void setHack_id(Long hack_id) {
        this.hack_id = hack_id;
    }

    public String getHack_route() {
        return hack_route;
    }

    public void setHack_route(String hack_route) {
        this.hack_route = hack_route;
    }

    public JsonElement getHack_contact() {
        return hack_contact;
    }

    public void setHack_contact(JsonElement hack_contact) {
        this.hack_contact = hack_contact;
    }

    public String getHack_agent() {
        return hack_agent;
    }

    public void setHack_agent(String hack_agent) {
        this.hack_agent = hack_agent;
    }

    public String getHack_auth() {
        return hack_auth;
    }

    public void setHack_auth(String hack_auth) {
        this.hack_auth = hack_auth;
    }

    public String getHack_ip() {
        return hack_ip;
    }

    public void setHack_ip(String hack_ip) {
        this.hack_ip = hack_ip;
    }

    public String getHack_params() {
        return hack_params;
    }

    public void setHack_params(String hack_params) {
        this.hack_params = hack_params;
    }

    public String getHack_body() {
        return hack_body;
    }

    public void setHack_body(String hack_body) {
        this.hack_body = hack_body;
    }

    public Hack(){
        super();
    }

    @Override
    public void merge(Hack model) {
        super.merge(model);
    }
}
