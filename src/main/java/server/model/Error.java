package server.model;

import com.google.gson.JsonElement;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

@Permission(permissionName = "گزارش خطای سیستم")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "errors" + SchemaUtil.TABLE_POSTFIX)
public class Error extends DatabaseModel<Error> {

    public static final String FUNCTION = "error_function";
    public static final String ROUTE = "error_route";
    public static final String CLASS = "error_class";
    public static final String AUTH = "error_auth";
    public static final String PATH = "error_path";

    @DatabaseField(generatedId = true)
    private Long error_id;

    @DatabaseField()
    private String error_route;

    @DatabaseField()
    private String error_path;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String error_message;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String error_request;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String error_cause;

    @DatabaseField()
    private String error_agent;

    @DatabaseField()
    private String error_auth;

    @DatabaseField()
    private String error_line;

    @DatabaseField()
    private String error_function;

    @DatabaseField()
    private String error_class;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement error_json;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement error_contact;

    public String getError_function() {
        return error_function;
    }

    public void setError_function(String error_function) {
        this.error_function = error_function;
    }

    public String getError_class() {
        return error_class;
    }

    public void setError_class(String error_class) {
        this.error_class = error_class;
    }

    public String getError_line() {
        return error_line;
    }

    public void setError_line(String error_line) {
        this.error_line = error_line;
    }

    public JsonElement getError_contact() {
        return error_contact;
    }

    public void setError_contact(JsonElement error_contact) {
        this.error_contact = error_contact;
    }

    public String getError_auth() {
        return error_auth;
    }

    public void setError_auth(String error_auth) {
        this.error_auth = error_auth;
    }

    public String getError_agent() {
        return error_agent;
    }

    public void setError_agent(String error_agent) {
        this.error_agent = error_agent;
    }

    public Long getError_id() {
        return error_id;
    }

    public void setError_id(Long error_id) {
        this.error_id = error_id;
    }

    public String getError_route() {
        return error_route;
    }

    public void setError_route(String error_route) {
        this.error_route = error_route;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    public String getError_request() {
        return error_request;
    }

    public void setError_request(String error_request) {
        this.error_request = error_request;
    }

    public String getError_cause() {
        return error_cause;
    }

    public void setError_cause(String error_cause) {
        this.error_cause = error_cause;
    }

    public JsonElement getError_json() {
        return error_json;
    }

    public void setError_json(JsonElement error_json) {
        this.error_json = error_json;
    }

    public String getError_path() {
        return error_path;
    }

    public void setError_path(String error_path) {
        this.error_path = error_path;
    }

    public Error() {
        super();
    }

    @Override
    public void merge(Error model) {
        super.merge(model);
    }
}
