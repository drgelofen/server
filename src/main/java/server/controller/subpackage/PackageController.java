package server.controller.subpackage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.controller.setting.SettingController;
import server.controller.ticket.TicketController;
import server.lib.center.FCMCenter;
import server.lib.interfacing.Authenticate;
import server.lib.interfacing.Authorize;
import server.lib.model.FcmModel;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.QueryBuilder;
import server.lib.orm.stmt.Where;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.lib.utils.StringUtil;
import server.model.ApiModels.UserDoctor_UserPackageApiModel;
import server.model.*;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/package")
public class PackageController extends Controller<SubPackage> {

    @Authorize
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<SubPackage, UUID> dao = getDao(database);
        FilterModel<SubPackage, UUID> filter = filter(dao, request);
        if (filter.getSearch_by() != null) {
            filter.where().and().like(SubPackage.NAME, "%" + filter.getSearch_by() + "%");
        }
        if (filter.getParent_by() != null) {
            filter.where().and().eq(SubPackage.DOCTOR, filter.getParent_by());
        }
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
        List<SubPackage> query = filter.builder().leftJoin(SubPackage.DOCTOR, Doctor.ID, doctors.queryBuilder()).query();
        return pass(HttpStatus.OK, query);
    }

    @Authorize
    public ResponseEntity create(Database database, SubPackage info) throws Throwable {
        if (info.getDoctor_id() != null) {
            Doctor content = new Doctor();
            content.setDoctor_id(info.getDoctor_id());
            info.setDoctor(content);
        }
        Dao<SubPackage, UUID> dao = getDao(database);
        SubPackage subPackage = dao.createIfNotExists(info);
        return subPackage == null ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, dao.queryForId(subPackage.getPackage_id()));
    }

    @Authorize
    public ResponseEntity update(Database database, SubPackage info) throws Throwable {
        if (info == null || info.getPackage_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<SubPackage, UUID> dao = getDao(database);
        SubPackage subPackage = dao.queryForId(info.getPackage_id());
        if (subPackage == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        if (info.getDoctor_id() != null) {
            Doctor content = new Doctor();
            content.setDoctor_id(info.getDoctor_id());
            info.setDoctor(content);
        }
        subPackage.merge(info);
        return dao.update(subPackage) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, subPackage);
    }

    @Authorize
    public ResponseEntity delete(Database database, SubPackage info) throws Throwable {
        if (info == null || info.getPackage_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<SubPackage, UUID> dao = getDao(database);
        return dao.delete(info) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
    }

    public ResponseEntity getList(Database database, Request request) throws Throwable {
        Dao<SubPackage, UUID> dao = getDao(database);
        FilterModel<SubPackage, UUID> filter = filter(dao, request);
        filter.where().and().eq(SubPackage.VISIBILITY, true);
        if (filter.getParent_by() != null) {
            filter.where().and().eq(SubPackage.DOCTOR, filter.getParent_by());
        }
        return pass(HttpStatus.OK, trim(filter.query()));
    }

    public ResponseEntity getOne(Database database, SubPackage info) throws Throwable {
        if (info == null || info.getPackage_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<SubPackage, UUID> dao = getDao(database);
        SubPackage subPackage = dao.queryForId(info.getPackage_id());
        if (subPackage != null) {
            subPackage.setPackage_views(subPackage.getPackage_views() + 1);
            dao.update(subPackage);
            return pass(HttpStatus.OK, subPackage);
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }

    @Authenticate
    public ResponseEntity getCancelTimer(Database database, Request request) throws Throwable {
        UserDoctor userDoctor = parse(request, UserDoctor.class);
        if (userDoctor == null || userDoctor.getRecord_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<UserDoctor, Long> dao = getDaoLong(database, UserDoctor.class);
        userDoctor = dao.queryForId(userDoctor.getRecord_id());
        if (userDoctor == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        userDoctor.setRemain(calculateCancelTime(database, userDoctor));
        return pass(HttpStatus.OK, userDoctor);
    }

    @Authenticate
    public ResponseEntity cancelPackage(Database database, Request request) throws Throwable {
        UserDoctor userDoctor = parse(request, UserDoctor.class);
        if (userDoctor == null || userDoctor.getRecord_id() == null || request.getAuth_doctor() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<UserDoctor, Long> dao = getDaoLong(database, UserDoctor.class);
        userDoctor = dao.queryForId(userDoctor.getRecord_id());
        if (userDoctor == null || userDoctor.getRecord_canceled() || userDoctor.getRecord_settled()) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        long time = calculateCancelTime(database, userDoctor);
        if (time < 0) {
            return pass(UserDoctor.MESSAGE_CANCEL, HttpStatus.EXPECTATION_FAILED);
        }
        userDoctor.setRecord_canceled(true);
        dao.update(userDoctor);

        User user = getDao(database, User.class).queryForId(userDoctor.getUser().getUser_id());
        if (user.getUser_pushId() != null) {
            FcmModel fcmModel = new FcmModel();
            fcmModel.setTo(user.getUser_pushId());
            fcmModel.setData("مشاوره از سمت پزشک لغو شد", SettingController.getParam(database, Setting.CANCEL_MESSAGE));
            FCMCenter.push(fcmModel);
        }
        return pass(HttpStatus.OK);
    }

    @Authenticate
    public ResponseEntity accept(Database database, Request request) throws Throwable {
        UserPackage parse = parse(request, UserPackage.class);

        Dao<UserPackage, Long> dao = getDaoLong(database, UserPackage.class);
        UserPackage userPackage = dao.queryForId(parse.getRecord_id());

        if (userPackage == null || userPackage.getAccepted()) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }

        userPackage.setAccepted(true);
        dao.update(userPackage);

        return pass(HttpStatus.OK);
    }

//    public ResponseEntity seedFalse(Database database,Request request) throws Throwable {
//        Dao<UserPackage,Long> dao = getDaoLong(database,UserPackage.class);
//        List<UserPackage> userpackages = dao.queryForAll();
//        userpackages.stream().forEach(userPackage -> {
//            userPackage.setAccepted(false);
//            try {
//                dao.update(userPackage);
//            } catch (SQLException e) {
//            }
//        });
//        return pass(HttpStatus.OK);
//    }

    @Authenticate
    public ResponseEntity settlePackage(Database database, Request request) throws Throwable {

        UserDoctor_UserPackageApiModel parse = parse(request, UserDoctor_UserPackageApiModel.class);
        if (parse == null || request.getAuth_user() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }

        Dao<UserDoctor, Long> dao = getDaoLong(database, UserDoctor.class);
        UserDoctor userDoctor = dao.queryForId(parse.getUserDoctorId());
        if (userDoctor == null || !userDoctor.getRecord_canceled() || userDoctor.getRecord_settled()) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }

        Dao<UserPackage, Long> daoLong = getDaoLong(database, UserPackage.class);
        UserPackage userPackage = daoLong.queryForId(parse.getUserPackageId());

        if (userPackage.getAccepted()) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }

        Dao<SubPackage, UUID> subPackages = getDao(database);
        SubPackage subPackage = subPackages.queryForId(userPackage.getSub_package().getPackage_id());

        double sum = subPackage.getPackage_sell_price();

        if (parse.getSettleType() == 1) {

            Dao<User, UUID> users = getDao(database, User.class);
            request.getAuth_user().setUser_wallet((int) sum);

            users.update(request.getAuth_user());

            userDoctor.setRecord_settled(true);
            userDoctor.setRecord_canceled(true);

            dao.update(userDoctor);

        } else if (parse.getSettleType() == 2) {

            if (parse.getSettleName() == null || parse.getSettleAccount() == null) {
                return pass(HttpStatus.BAD_REQUEST);
            }

            Ticket ticket = new Ticket();
            ticket.setTicket_title("درخواست استرداد وجه مشاوره با " + userDoctor.getDoctor().getDoctor_name());
            ticket.setTicket_description("مبلغ " + StringUtil.formatPrice(sum)
                    + " به حساب " + parse.getSettleAccount() + " به نام " + parse.getSettleName() + "واریز گردد.");
            request.setBody(toJson(ticket));
            TicketController.createTicket(database, request);
        }
        return pass(HttpStatus.OK);
    }

//    @Authenticate
//    public ResponseEntity settlePackage(Database database, Request request) throws Throwable {
//       UserDoctor parse = parse(request, UserDoctor.class);
//        if (parse == null || parse.getRecord_id() == null || parse.getSettle_type() == null || request.getAuth_user() == null) {
//            return pass(HttpStatus.BAD_REQUEST);
//        }
//        Dao<UserDoctor, Long> dao = getDaoLong(database, UserDoctor.class);
//        UserDoctor userDoctor = dao.queryForId(parse.getRecord_id());
//        if (userDoctor == null || !userDoctor.getRecord_canceled() || userDoctor.getRecord_settled()) {
//            return pass(HttpStatus.EXPECTATION_FAILED);
//        }
//        Dao<UserPackage, Long> daoLong = getDaoLong(database, UserPackage.class);
//
//        double sum = 0;
//        List<UserPackage> query = daoLong.queryBuilder()
//                .where().eq(UserDoctor.DOCTOR, userDoctor.getDoctor().getDoctor_id())
//                .and().eq(UserDoctor.USER, userDoctor.getUser()).query();
//        Dao<SubPackage, UUID> subPackages = getDao(database);
//        for (UserPackage userPackage : query) {
//            subPackages.refresh(userPackage.getSub_package());
//            sum = sum + userPackage.getSub_package().getPackage_sell_price();
//        }
//
//        if (parse.getSettle_type() == UserDoctor.SETTLE_TYPE_TICKET) {
//            getDao(database, Doctor.class).refresh(userDoctor.getDoctor());
//
//            Ticket ticket = new Ticket();
//            ticket.setTicket_title("درخواست استرداد وجه مشاوره با " + userDoctor.getDoctor().getDoctor_name());
//            ticket.setTicket_description("مبلغ " + StringUtil.formatPrice(sum)
//                    + " به حساب " + parse.getSettle_account() + " به نام " + parse.getSettle_name() + "واریز گردد.");
//            request.setBody(toJson(ticket));
//            TicketController.createTicket(database, request);
//        } else {
//            Dao<User, UUID> users = getDao(database, User.class);
//            request.getAuth_user().setUser_wallet((int) (request.getAuth_user().getUser_wallet() + sum));
//            users.update(request.getAuth_user());
//        }
//        userDoctor.setRecord_settled(true);
//        dao.update(userDoctor);
//        return pass(HttpStatus.OK);
//    }

    private long calculateCancelTime(Database database, UserDoctor userDoctor) throws Throwable {
        if (userDoctor.getRecord_canceled() || userDoctor.getRecord_settled()) {
            return 0;
        }
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
        Doctor doctor = doctors.queryForId(userDoctor.getDoctor().getDoctor_id());

        Dao<Consult, Long> consults = getDaoLong(database, Consult.class);

        QueryBuilder<Consult, Long> firstDoctor = consults.queryBuilder();
        firstDoctor.orderBy(Consult.CREATE_AT, true);
        Consult firstMessageDoctor = firstDoctor.where().eq(Consult.USER, userDoctor.getUser().getUser_id())
                .and().eq(Consult.DOCTOR, doctor.getDoctor_id())
                .and().eq(Consult.OWNER, Consult.OWNER_DOCTOR)
                .queryForFirst();

        QueryBuilder<Consult, Long> firstUser = consults.queryBuilder();
        firstUser.orderBy(Consult.CREATE_AT, true);
        Consult firstMessageUser = firstUser.where().eq(Consult.USER, userDoctor.getUser().getUser_id())
                .and().eq(Consult.DOCTOR, doctor.getDoctor_id())
                .and().eq(Consult.OWNER, Consult.OWNER_USER)
                .queryForFirst();

        long expire = SettingController.getParamAsInt(database, Setting.CANCEL_LIMIT);
        if (!StringUtil.isEmpty(doctor.getDoctor_worktable())) {
            expire = Long.parseLong(doctor.getDoctor_worktable());
        }
        expire = TimeUnit.HOURS.toMillis(expire);

        long remain = 0;
        if (firstMessageDoctor == null && firstMessageUser != null) {
            long differ = Instant.now().toEpochMilli() - firstMessageUser.getCreate_at();
            remain = expire - differ;
            if (remain < 0) {
                Dao<UserDoctor, Long> dao = getDaoLong(database, UserDoctor.class);
                userDoctor.setRecord_canceled(true);
                dao.update(userDoctor);

                User user = getDao(database, User.class).queryForId(userDoctor.getUser().getUser_id());
                if (user.getUser_pushId() != null) {
                    FcmModel fcmModel = new FcmModel();
                    fcmModel.setTo(user.getUser_pushId());
                    fcmModel.setData("مشاوره از سمت پزشک لغو شد", SettingController.getParam(database, Setting.CANCEL_MESSAGE));
                    FCMCenter.push(fcmModel);
                }
            }
        }
        return remain;
    }

    @Authenticate
    public ResponseEntity getStats(Database database, Request request) throws Throwable {
        Doctor doctor = parse(request, Doctor.class);
        if (doctor == null || doctor.getDoctor_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
        doctor = doctors.queryForId(doctor.getDoctor_id());
        if (doctor == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }

        Dao<SubPackage, UUID> dao = getDao(database);
        Dao<UserPackage, Long> daoLong = getDaoLong(database, UserPackage.class);
        QueryBuilder<UserPackage, Long> builder = daoLong.queryBuilder();
        Where<UserPackage, Long> where = builder.where();
        List<UserPackage> query = where.eq(UserPackage.USER, request.getAuth()).and()
                .eq(UserPackage.DOCTOR, doctor.getDoctor_id()).query();

        long expire = SettingController.getParamAsInt(database, Setting.REMAIN_TIME);
//        if (!StringUtil.isEmpty(doctor.getDoctor_worktable())) {
//            expire = Long.parseLong(doctor.getDoctor_worktable());
//        }
        expire = TimeUnit.HOURS.toMillis(expire);

        Dao<Consult, Long> consults = getDaoLong(database, Consult.class);

        QueryBuilder<Consult, Long> firstDoctor = consults.queryBuilder();
        firstDoctor.orderBy(Consult.CREATE_AT, true);
        Consult firstMessageDoctor = firstDoctor.where().eq(Consult.USER, request.getAuth())
                .and().eq(Consult.DOCTOR, doctor.getDoctor_id())
                .and().eq(Consult.OWNER, Consult.OWNER_DOCTOR)
                .queryForFirst();

        QueryBuilder<Consult, Long> firstUser = consults.queryBuilder();
        firstUser.orderBy(Consult.CREATE_AT, true);
        Consult firstMessageUser = firstUser.where().eq(Consult.USER, request.getAuth())
                .and().eq(Consult.DOCTOR, doctor.getDoctor_id())
                .and().eq(Consult.OWNER, Consult.OWNER_USER)
                .queryForFirst();

        for (UserPackage sp : query) {
            dao.refresh(sp.getSub_package());
            if (firstMessageDoctor != null && firstMessageUser != null) {
                long create = firstMessageDoctor.getCreate_at() > firstMessageUser.getCreate_at()
                        ? firstMessageDoctor.getCreate_at() : firstMessageUser.getCreate_at();
                sp.setPackage_remain(create + expire - Instant.now().toEpochMilli());
            } else {
                sp.setPackage_remain(expire);
            }
        }
        return pass(HttpStatus.OK, trim(query));
    }

    public ResponseEntity getComments(Database database, Request request) throws Throwable {
        Doctor doctor = parse(request, Doctor.class);
        if (doctor == null || doctor.getDoctor_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<User, UUID> dao = getDao(database, User.class);
        Dao<UserPackage, Long> daoLong = getDaoLong(database, UserPackage.class);
        QueryBuilder<UserPackage, Long> builder = daoLong.queryBuilder();
        Where<UserPackage, Long> where = builder.where();
        where.isNotNull(UserPackage.COMMENT)
                .and().isNotNull(UserPackage.RATE)
                .and().eq(UserPackage.DOCTOR, doctor.getDoctor_id());
        List<UserPackage> query = builder.leftJoin(UserPackage.USER, User.ID, dao.queryBuilder()).query();
        JsonArray array = new JsonArray();
        for (UserPackage pack : query) {
            JsonObject obj = new JsonObject();
            obj.addProperty("comment_date", pack.getComment_date());
            obj.addProperty("comment", pack.getComment());
            obj.addProperty("rate", pack.getRate());
            User user = new User();
            user.setUsername(pack.getUser().getUsername());
            user.setUser_avatar(pack.getUser().getUser_avatar());
            obj.add("user", user.toJsonObject());
            array.add(obj);
        }
        return pass(HttpStatus.OK, array);
    }

    @Authenticate
    public ResponseEntity sendComment(Database database, Request request) throws Throwable {
        UserPackage subPackage = parse(request, UserPackage.class);
        if (subPackage == null || subPackage.getComment() == null || subPackage.getRate() == null || subPackage.getRecord_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<UserPackage, Long> daoLong = getDaoLong(database, UserPackage.class);
        UserPackage userPackage = daoLong.queryForId(subPackage.getRecord_id());
        if (userPackage == null || !userPackage.getUser().getUser_id().equals(request.getAuth())) {
            return pass(HttpStatus.FORBIDDEN);
        }
        if (request.getAuth_user().getLimit()) {
            return pass(User.COMMENT_MESSAGE, HttpStatus.FORBIDDEN);
        }
        Dao<Doctor, UUID> dao = getDao(database, Doctor.class);
        Doctor doctor = dao.queryForId(userPackage.getDoctor().getDoctor_id());
        if (doctor == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        userPackage.merge(subPackage);
        daoLong.update(userPackage);

        double sum = (doctor.getDoctor_rate() * doctor.getDoctor_voters()) + subPackage.getRate();
        doctor.setDoctor_rate((float) (sum / (doctor.getDoctor_voters() + 1)));
        dao.update(doctor);
        return pass(HttpStatus.OK);
    }

}
