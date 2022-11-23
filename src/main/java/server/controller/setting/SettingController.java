package server.controller.setting;

import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authorize;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.dao.DaoManager;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.lib.utils.StringUtil;
import server.model.Setting;

import java.util.UUID;

@RestController
@RequestMapping("/setting")
public class SettingController extends Controller<Setting> {

    @Authorize
    public ResponseEntity setInfo(Database database, JsonObject jsonObject) throws Throwable {
        Dao<Setting, UUID> dao = getDao(database);
        Setting setting = dao.queryForAll().get(0);
        if (setting == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        Setting updated = new Setting();
        updated.setInformation(jsonObject);
        setting.merge(updated);
        return dao.update(setting) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, setting);
    }

    @Authorize
    public ResponseEntity setRepo(Database database, JsonObject jsonObject) throws Throwable {
        Dao<Setting, UUID> dao = getDao(database);
        Setting setting = dao.queryForAll().get(0);
        if (setting == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        Setting updated = new Setting();
        updated.setRepository(jsonObject);
        setting.merge(updated);
        return dao.update(setting) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, setting);
    }

    public ResponseEntity getSetting(Database database) throws Throwable {
        return pass(HttpStatus.OK, get(database).trim());
    }

    public ResponseEntity getInfo(Database database, Request request) throws Throwable {
        Dao<Setting, UUID> dao = getDao(database);
        JsonObject information = dao.queryForAll().get(0).getInformation().getAsJsonObject();
        FilterModel parse = parse(request, FilterModel.class);
        if (parse == null || StringUtil.isEmpty(parse.getSearch_by())) {
            return pass(HttpStatus.OK, information);
        }
        return pass(HttpStatus.OK, information.get(parse.getSearch_by()));
    }

    public ResponseEntity getRepo(Database database, Request request) throws Throwable {
        Dao<Setting, UUID> dao = getDao(database);
        JsonObject information = dao.queryForAll().get(0).getRepository().getAsJsonObject();
        FilterModel parse = parse(request, FilterModel.class);
        if (parse == null || StringUtil.isEmpty(parse.getSearch_by())) {
            return pass(HttpStatus.OK, information);
        }
        return pass(HttpStatus.OK, information.get(parse.getSearch_by()));
    }

    public static Setting get(Database database) throws Throwable {
        Dao<Setting, UUID> dao = getDaoStatic(database, Setting.class);
        return dao.queryForAll().get(0);
    }

    public static String getParam(Database database, String column) throws Throwable {
        Dao<Setting, UUID> dao = getDaoStatic(database, Setting.class);
        Setting setting = dao.queryForAll().get(0);
        return setting.getField(column);
    }

    public static int getParamAsInt(Database database, String column) throws Throwable {
        String param = getParam(database, column);
        try {
            return Integer.parseInt(param);
        } catch (Throwable ignored) {
        }
        return 0;
    }

    public static void createSetting(Database database) throws Throwable {
        Dao<Setting, UUID> settingDao = DaoManager.createDao(database, Setting.class);
        Setting setting;
        if (settingDao.countOf() == 0) {
            setting = new Setting();
            setting.setInformation(new JsonObject());
        } else {
            setting = settingDao.queryForAll().get(0);
        }

        JsonObject android_ = new JsonObject();
        android_.addProperty("last_version", 0);
        android_.addProperty("update_message", "نسخه جدیدی از برنامه در دسترس است، برای بروزرسانی اقدام کنید.");
        android_.addProperty("force_update_from", 100);
        android_.addProperty("update_link", "https://nahantech.com");

        JsonObject client = new JsonObject();
        client.addProperty("last_version", 0);
        client.addProperty("update_message", "نسخه جدیدی از برنامه در دسترس است، برای بروزرسانی اقدام کنید.");
        client.addProperty("force_update_from", 100);
        client.addProperty("update_link", "https://nahantech.com");

        JsonObject repo = new JsonObject();
        repo.add("com.drgelofen.doctor", android_);
        repo.add("com.drgelofen.user", client);

        setting.setRepository(repo);
        settingDao.createOrUpdate(setting);
    }
}
