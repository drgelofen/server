package server.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.Application;
import server.lib.interfacing.Authorize;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.Where;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.lib.utils.TokenUtil;
import server.model.Admin;
import server.model.Hack;

@RestController
@RequestMapping("/hack")
public class HackController extends Controller<Hack> {

    public static void log(Database database, Request request) {
        try {
            Dao<Hack, Long> dao = getDaoLongStatic(database, Hack.class);
            Hack hack = new Hack();
            hack.setHack_agent(request.getUserAgent());
            hack.setHack_auth(String.valueOf(request.getAuth()));
            hack.setHack_route(request.getRoute());
            hack.setHack_body(request.getBody());
            hack.setHack_ip(TokenUtil.getIP(request));
            hack.setHack_params(Application.GSON.toJson(request.getParameterMap()));
            if (request.getAuth_admin() != null) {
                hack.setHack_contact(request.getAuth_admin().toJsonObject());
            } else if (request.getAuth_user() != null) {
                hack.setHack_contact(request.getAuth_user().toJsonObject());
            } else if (request.getAuth_doctor() != null) {
                hack.setHack_contact(request.getAuth_doctor().toJsonObject());
            }
            dao.create(hack);
        } catch (Throwable ignored) {
        }
    }

    @Authorize(permissions = Admin.class)
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Hack, Long> dao = getDaoLong(database);
        FilterModel<Hack, Long> filter = filter(dao, request);
        Hack parse = parse(request);
        if (parse != null) {
            Where where = filter.where();
            if (parse.getHack_route() != null) {
                where.and().eq(Hack.ROUTE, parse.getHack_route());
            } else if (parse.getHack_auth() != null) {
                where.and().eq(Hack.AUTH, parse.getHack_auth());
            } else if (parse.getHack_ip() != null) {
                where.and().eq(Hack.IP, parse.getHack_ip());
            }
        }
        return pass(HttpStatus.OK, filter.query());
    }
}
