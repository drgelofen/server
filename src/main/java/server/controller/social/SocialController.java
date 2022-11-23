package server.controller.social;

import server.lib.orm.dao.Dao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authorize;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.model.Social;

import java.util.UUID;

@RestController
@RequestMapping("/social")
public class SocialController extends Controller<Social> {

    @Authorize
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Social, UUID> dao = getDao(database);
        FilterModel<Social, UUID> filter = filter(dao, request);
        return pass(HttpStatus.OK, filter.query());
    }

    public ResponseEntity getList(Database database, Request request) throws Throwable {
        Dao<Social, UUID> dao = getDao(database);
        FilterModel<Social, UUID> filter = filter(dao, request);
        filter.where().and().eq(Social.VISIBILITY, true);
        return pass(HttpStatus.OK, trim(filter.query()));
    }

    public ResponseEntity getOne(Database database, Social info) throws Throwable {
        if (info == null || info.getSocial_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Social, UUID> dao = getDao(database);
        Social banner = dao.queryForId(info.getSocial_id());
        if (banner != null) {
            banner.setSocial_views(banner.getSocial_views() + 1);
            dao.update(banner);
            return pass(HttpStatus.OK, banner);
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }

    @Authorize
    public ResponseEntity create(Database database, Social info) throws Throwable {
        Dao<Social, UUID> dao = getDao(database);
        Social banner = dao.createIfNotExists(info);
        return banner == null ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, dao.queryForId(banner.getSocial_id()));
    }

    @Authorize
    public ResponseEntity delete(Database database, Social info) throws Throwable {
        if (info == null || info.getSocial_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Social, UUID> dao = getDao(database);
        return dao.delete(info) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
    }

    @Authorize
    public ResponseEntity update(Database database, Social info) throws Throwable {
        if (info == null || info.getSocial_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Social, UUID> dao = getDao(database);
        Social banner = dao.queryForId(info.getSocial_id());
        if (banner == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        banner.merge(info);
        return dao.update(banner) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, banner);
    }
}
