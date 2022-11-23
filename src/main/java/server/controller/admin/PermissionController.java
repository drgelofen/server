package server.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authorize;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.dao.DaoManager;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.lib.utils.SchemaUtil;
import server.lib.utils.TokenUtil;
import server.model.Admin;
import server.model.Permission;

import java.util.*;

@RestController
@RequestMapping("/permission")
public class PermissionController extends Controller<Permission> {

    public static final String ADMINISTRATOR = "Admin";

    @Authorize(permissions = Admin.class)
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Permission, UUID> dao = getDao(database);
        List<Permission> query = dao.queryForAll();
        Collections.sort(query, new Comparator<Permission>() {
            @Override
            public int compare(Permission o1, Permission o2) {
                return Double.compare(o2.getPermission_priority(), o1.getPermission_priority());
            }
        });
        return pass(HttpStatus.OK, trim(query));
    }

    public static void createPermissions(Database database) throws Throwable {
        Dao<Permission, UUID> dao = DaoManager.createDao(database, Permission.class);
        List<Permission> permissions = dao.queryForAll();
        HashSet<String> hashSet = new HashSet<>();
        for (Permission permission : permissions) {
            hashSet.add(permission.getPermission_class());
        }
        ArrayList<Permission> newList = new ArrayList<>();
        for (Class table : SchemaUtil.getTables()) {
            Permission permission = Permission.from(table);
            if (permission != null) {
                if (!hashSet.contains(permission.getPermission_class())) {
                    newList.add(permission);
                }
            }
        }
        if (newList.size() > 0) {
            dao.create(newList);
        }
        Dao<Admin, UUID> adminDao = DaoManager.createDao(database, Admin.class);
        Admin admin = adminDao.queryBuilder().where().eq(Admin.USERNAME, ADMINISTRATOR).queryForFirst();
        if (admin == null) {
            admin = new Admin();
            admin.setUsername(ADMINISTRATOR);
            admin.setPassword(TokenUtil.bycrypt("931028aba6e219591461c51f53125618"));
            adminDao.create(admin);
        }
        admin.setPermissions(compilePermissions(dao.queryForAll()));
        adminDao.createOrUpdate(admin);
    }

    private static UUID[] compilePermissions(List<Permission> permissions) {
        ArrayList<UUID> ids = new ArrayList<>();
        for (Permission permission : permissions) {
            ids.add(permission.getPermission_id());
        }
        return ids.toArray(new UUID[ids.size()]);
    }
}
