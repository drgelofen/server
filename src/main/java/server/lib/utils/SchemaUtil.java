package server.lib.utils;

import org.reflections.Reflections;
import server.Application;
import server.controller.admin.PermissionController;
import server.controller.setting.SettingController;
import server.lib.model.DatabaseModel;
import server.lib.orm.db.PostgresDatabaseType;
import server.lib.orm.field.DataPersisterManager;
import server.lib.orm.persister.ArrayPersister;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.TableUtils;
import server.model.*;

import java.util.ArrayList;

public class SchemaUtil {

    private static final String DATABASE_SCHEMA = "jdbc:postgresql://localhost/" + Application.SCHEMA_NAME
            + "?user=" + Application.SCHEMA_USER + "&password=" + Application.SCHEMA_CRED;
    public static final String TABLE_PREFIX = "__", TABLE_POSTFIX = "__";

    public static Database getDB() throws Throwable {
        return new Database(DATABASE_SCHEMA, new PostgresDatabaseType());
    }

    public static void initDatabase() {
        try (Database connectionSource = getDB()) {
            DataPersisterManager.registerDataPersisters(ArrayPersister.getSingleton());
            DataPersisterManager.registerDataPersisters(JsonPersister.getSingleton());

            runPreScheduledTask(connectionSource);
            createTables(connectionSource);

            PermissionController.createPermissions(connectionSource);
            SettingController.createSetting(connectionSource);

            runPostScheduledTask(connectionSource);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void createTables(Database connectionSource) throws Throwable {
        for (Class<? extends DatabaseModel> table : getTables()) {
            if (Application.INIT_DATABASE && !Application.PRODUCTION_MODE) {
                TableUtils.dropTable(connectionSource, table, true);
            }
            try {
                TableUtils.createTableIfNotExists(connectionSource, table);
            } catch (Throwable ignored) {
            }
        }
    }

    public static ArrayList<Class<? extends DatabaseModel>> getTables() {
        Reflections reflections = new Reflections("server.model");
        ArrayList tables = new ArrayList<>();
        for (Class table : reflections.getSubTypesOf(DatabaseModel.class)) {
            tables.add(table);
        }
        return tables;
    }

    private static void runPreScheduledTask(Database db) throws Throwable {
        try {
//            TableUtils.dropTable(db, UserPackage.class, true);
//            TableUtils.dropTable(db, UserDoctor.class, true);
//            TableUtils.dropTable(db, Consult.class, true);
//            TableUtils.dropTable(db, SubPackage.class, true);
//            TableUtils.dropTable(db, Payment.class, true);
//            TableUtils.dropTable(db, DoctorIncome.class, true);
//            TableUtils.dropTable(db, DoctorProduct.class, true);
//            TableUtils.dropTable(db, Doctor.class, true);
        } catch (Throwable ignored) {
        }
    }

    private static void runPostScheduledTask(Database db) throws Throwable {
        try {
//            System.out.println(SettingController.getParam(db, Setting.CANCEL_MESSAGE));
        } catch (Throwable ignored) {
        }
    }
}
