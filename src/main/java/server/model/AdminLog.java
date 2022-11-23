package server.model;

import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

@Permission(permissionName = "فعالیت اپراتورها", permissionBrief = "در این قسمت فعالیت اپراتورها و گزارش مربوطه را مشاهده نمایید", permissionPriority = 950)
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "admin_logs" + SchemaUtil.TABLE_POSTFIX)
public class AdminLog extends DatabaseModel<AdminLog> {

    public static final String ADMIN = "admin_id";
    public static final String ROUTE = "log_route";

    @DatabaseField(generatedId = true)
    private Long log_id;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String log_request;

    @DatabaseField(canBeNull = false, foreign = true)
    private Admin admin;

    @DatabaseField()
    private String log_route;

    public String getLog_route() {
        return log_route;
    }

    public void setLog_route(String log_route) {
        this.log_route = log_route;
    }

    public String getLog_request() {
        return log_request;
    }

    public void setLog_request(String log_request) {
        this.log_request = log_request;
    }

    public Long getLog_id() {
        return log_id;
    }

    public void setLog_id(Long log_id) {
        this.log_id = log_id;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public AdminLog() {
        super();
    }

    @Override
    public void merge(AdminLog model) {
        super.merge(model);
    }

    @Override
    public AdminLog trim() {
        if(admin != null) admin.trim();
        return super.trim();
    }
}
