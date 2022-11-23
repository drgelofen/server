package server.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authorize;
import server.lib.model.DatabaseModel;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.Where;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.lib.utils.StringUtil;
import server.lib.utils.TokenUtil;
import server.model.Admin;
import server.model.Permission;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController extends Controller<Admin> {

    private static final boolean SINGLE_SESSION_ADMIN = false;

    public ResponseEntity login(Database database, Request servletRequest) throws Throwable {
        Admin request = parse(servletRequest);
        if (request != null && request.getUsername() != null && request.getPassword() != null) {
            request.setUsername(StringUtil.format(request.getUsername()));
            request.setPassword(StringUtil.format(request.getPassword()).toLowerCase());
            if (!TokenUtil.isMD5(request.getPassword())) {
                return pass(HttpStatus.BAD_REQUEST);
            }
            Dao<Admin, UUID> dao = getDao(database);
            Admin admin = dao.queryBuilder().where().eq(Admin.USERNAME, request.getUsername()).queryForFirst();
            if (admin != null) {
                if (TokenUtil.isMatchBycrypt(admin.getPassword(), request.getPassword())) {
                    admin.setAuth_version(admin.getAuth_version() + 1);
                    dao.update(admin);
                    servletRequest.setAuth_admin(admin);
                    AdminLogController.log(database, servletRequest);
                    return passAuth(HttpStatus.OK, TokenUtil.generateToken(admin.getAdmin_id()
                            , SINGLE_SESSION_ADMIN ? admin.getAuth_version() : 1, TokenUtil.TokenType.ADMIN));
                }
            }
            return pass(Admin.CRED_MESSAGE, HttpStatus.EXPECTATION_FAILED);
        }
        return pass(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity getAccess(Database database, Request request) throws Throwable {
        boolean authorized = isAuthorized(database, request);
        Admin auth_admin = request.getAuth_admin();
        if (!authorized || auth_admin == null) {
            return pass(HttpStatus.UNAUTHORIZED);
        }
        compilePermissions(database, auth_admin);
        FilterModel parse = parse(request, FilterModel.class);
        if (auth_admin.getAccesses() != null) {
            for (Permission permission : auth_admin.getAccesses()) {
                if (permission != null) {
                    permission.setPermission_id(null);
                    permission.setPermission_priority(null);
                    if (parse == null || parse.getFilter_by() == null) {
                        permission.setPermission_brief(null);
                    }
                }
            }
        }
        return pass(HttpStatus.OK, auth_admin);
    }

    @Authorize()
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Admin, UUID> dao = getDao(database);
        FilterModel<Admin, UUID> filter = filter(dao, request);
        if (filter.getSearch_by() != null) {
            Where<Admin, UUID> where = filter.where();
            String input = "%" + filter.getSearch_by() + "%";
            where.and().or(where.like(Admin.USERNAME, input), where.like(Admin.HASH, input), where.like(Admin.NAME, input));
        }
        List<Admin> admins = filter.query();
        Dao<Permission, UUID> daoPermission = getDao(database, Permission.class);
        List<Permission> permissions = daoPermission.queryForAll();
        ArrayList<Admin> adminList = new ArrayList<>();
        for (Admin admin : admins) {
            if (!admin.getUsername().equals(PermissionController.ADMINISTRATOR)) {
                compilePermissions(permissions, admin);
                adminList.add(admin);
            }
        }
        return pass(HttpStatus.OK, adminList);
    }

    @Authorize()
    public ResponseEntity get(Database database, Admin request) throws Throwable {
        if (request.getAdmin_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Admin, UUID> dao = getDao(database);
        Admin admin = dao.queryForId(request.getAdmin_id());
        if (admin == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        compilePermissions(database, admin);
        return pass(HttpStatus.OK, admin);
    }

    @Authorize
    public ResponseEntity create(Database database, Admin request) throws Throwable {
        if (request.getUsername() == null || request.getPassword() == null || request.getPermissions() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        if (!TokenUtil.isMD5(request.getPassword())) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        request.setUsername(StringUtil.format(request.getUsername()));
        request.setPassword(TokenUtil.bycrypt(StringUtil.format(request.getPassword())));
        Dao<Admin, UUID> dao = getDao(database);
        if (dao.queryBuilder().where().eq(Admin.USERNAME, request.getUsername()).countOf() > 0) {
            return pass(HttpStatus.CONFLICT);
        }
        Admin admin = dao.createIfNotExists(request);
        if (admin != null) {
            compilePermissions(database, admin);
            return pass(HttpStatus.OK, admin);
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }

    @Authorize
    public ResponseEntity update(Database database, Admin request) throws Throwable {
        if (request.getAdmin_id() == null || request.getPermissions() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Admin, UUID> dao = getDao(database);
        Admin admin = dao.queryForId(request.getAdmin_id());
        if (admin == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        if (request.getPassword() != null) {
            if (!TokenUtil.isMD5(request.getPassword())) {
                return pass(HttpStatus.BAD_REQUEST);
            }
            request.setPassword(TokenUtil.bycrypt((StringUtil.format(request.getPassword()))));
        }
        admin.merge(request);
        if (dao.update(admin) == 0) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        } else {
            compilePermissions(database, admin);
            return pass(HttpStatus.OK, admin);
        }
    }

    @Authorize
    public ResponseEntity delete(Database database, Admin request) throws Throwable {
        if (request.getAdmin_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Admin, UUID> dao = getDao(database);
        Admin admin = dao.queryForId(request.getAdmin_id());
        return dao.delete(admin) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
    }

    private void compilePermissions(Database database, Admin admin) throws Throwable {
        Dao<Permission, UUID> daoPermission = getDaoStatic(database, Permission.class);
        List<Permission> permissions = daoPermission.queryForAll();
        compilePermissions(permissions, admin);
    }

    private void compilePermissions(List<Permission> permissions, Admin admin) {
        ArrayList<Permission> availablePermissions = new ArrayList<>();
        for (Permission permission : permissions) {
            for (UUID id : admin.getPermissions()) {
                if (id != null && permission.getPermission_id().equals(id)) {
                    permission.setCreate_at(null);
                    permission.setUpdate_at(null);
                    availablePermissions.add(permission);
                    break;
                }
            }
        }
        Collections.sort(availablePermissions, (o2, o1) -> Double.compare(o1.getPermission_priority(), o2.getPermission_priority()));
        admin.setAccesses(availablePermissions.toArray(new Permission[availablePermissions.size()]));
        admin.trim();
    }

    public static boolean isAuthorized(Database database, Request request) throws Throwable {
        return isAuthorized(database, request, null, null);
    }

    public static boolean isAuthorized(Database database, Request request
            , Class<? extends DatabaseModel>[] tables, Authorize.Method[] methods) throws Throwable {
        if (request == null || request.getAuth() == null || request.getAuth_version() == null) {
            return false;
        }
        Dao<Admin, UUID> daoAdmins = getDaoStatic(database, Admin.class);
        Admin admin = daoAdmins.queryForId(request.getAuth());
        if (admin == null || admin.getPermissions() == null || (SINGLE_SESSION_ADMIN && !admin.getAuth_version().equals(request.getAuth_version()))) {
            return false;
        }
        boolean flag_authorized = true;
        if (tables != null) {
            List<Permission> permissions = getDaoStatic(database, Permission.class).queryForAll();
            HashMap<UUID, Permission> permissionMap = new HashMap<>();
            for (Permission permission : permissions) {
                permissionMap.put(permission.getPermission_id(), permission);
            }
            HashMap<String, HashSet<String>> availableAccess = new HashMap<>();
            for (UUID id : admin.getPermissions()) {
                Permission permission = permissionMap.get(id);
                if (permission != null) {
                    availableAccess.put(permission.getPermission_class(), admin.getMethods(id));
                }
            }
            for (Class<? extends DatabaseModel> model : tables) {
                if (!availableAccess.containsKey(model.getSimpleName())) {
                    flag_authorized = false;
                    break;
                } else if (methods != null && methods.length > 0) {
                    HashSet<String> availableMethods = availableAccess.get(model.getSimpleName());
                    for (Authorize.Method method : methods) {
                        if (!availableMethods.contains(method.name())) {
                            flag_authorized = false;
                            break;
                        }
                    }
                }
            }
        }
        if (flag_authorized) {
            request.setAuth_admin(admin);
            if (tables != null) {
                admin.setLatest_activity(Instant.now().toEpochMilli());
                daoAdmins.update(admin);
                AdminLogController.log(database, request);
            }
        }
        return flag_authorized;
    }
}
