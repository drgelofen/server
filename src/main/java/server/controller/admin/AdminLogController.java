package server.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authorize;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.QueryBuilder;
import server.lib.orm.stmt.Where;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.model.Admin;
import server.model.AdminLog;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/log")
public class AdminLogController extends Controller<AdminLog> {

    public static void log(Database database, Request servletRequest) {
        try {
            if (servletRequest.getAuth_admin() == null || servletRequest.getServletPath().equalsIgnoreCase("/admin/log/getAll")) {
                return;
            }
            Dao<AdminLog, Long> dao = getDaoLongStatic(database, AdminLog.class);
            AdminLog log = new AdminLog();
            log.setAdmin(servletRequest.getAuth_admin());
            log.setLog_request(servletRequest.getBody());
            log.setLog_route(servletRequest.getServletPath());
            dao.create(log);
        } catch (Throwable ignored) {
        }
    }

    @Authorize
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Admin, UUID> admins = getDao(database, Admin.class);
        Dao<AdminLog, Long> logs = getDaoLong(database);
        FilterModel<AdminLog, Long> filter = filter(logs, request);
        Where<AdminLog, Long> where = filter.where();
        if (filter.getParent_by() != null) {
            where.and().eq(AdminLog.ADMIN, filter.getParent_by());
        }
        where.and().not().like(AdminLog.ROUTE, "%/get%");
        QueryBuilder<Admin, UUID> adminBuilder = admins.queryBuilder();
        adminBuilder.where().not().eq(Admin.NAME, PermissionController.ADMINISTRATOR);
        List<AdminLog> query = filter.builder().leftJoin(AdminLog.ADMIN, Admin.ID, adminBuilder).query();
        return pass(HttpStatus.OK, trim(query));
    }
}
