package server.controller.consult;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.controller.setting.SettingController;
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
import server.lib.utils.TokenUtil;
import server.model.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/consult")
public class ConsultController extends Controller<Consult> {

    @Authorize
    @Authenticate
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Consult info = parse(request);
        if (info == null || info.getRecord_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<UserDoctor, Long> userDoctors = getDaoLong(database, UserDoctor.class);
        Dao<Consult, Long> dao = getDaoLong(database);
        UserDoctor userDoctor = null;
        if (info.getRecord_id() != null) {
            userDoctor = userDoctors.queryForId(info.getRecord_id());
        }
        if (userDoctor == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        FilterModel<Consult, Long> filter = filter(dao, request);
        Where<Consult, Long> where = filter.where();
        where.and().eq(Consult.USER, userDoctor.getUser().getUser_id())
                .and().eq(Consult.DOCTOR, userDoctor.getDoctor().getDoctor_id())
                .and().not().eq(Consult.TYPE, Consult.TYPE_SUGGEST);
        filter.builder().orderBy(Consult.CREATE_AT, false);
        List<Consult> trim = where.query();

        if (request.getAuth_type() != TokenUtil.TokenType.ADMIN) {
            int read = 0;
            for (Consult consult : trim) {
                if (consult.getPost_status() == Consult.STATUS_UNREAD) {
                    if ((request.getAuth_type() == TokenUtil.TokenType.DOCTOR && consult.getPost_owner() == Consult.OWNER_USER)
                            || (request.getAuth_type() == TokenUtil.TokenType.USER && consult.getPost_owner() == Consult.OWNER_DOCTOR)) {
                        consult.setPost_status(Consult.STATUS_READ);
                        dao.update(consult);
                        read++;
                    }
                }
            }
            if (read > 0) {
                if (request.getAuth_type() == TokenUtil.TokenType.USER) {
                    userDoctor.setRecord_unread_user(userDoctor.getRecord_unread_user() - read);
                    if (userDoctor.getRecord_unread_user() < 0) {
                        userDoctor.setRecord_unread_user(0);
                    }
                } else {
                    userDoctor.setRecord_unread_doctor(userDoctor.getRecord_unread_doctor() - read);
                    if (userDoctor.getRecord_unread_doctor() < 0) {
                        userDoctor.setRecord_unread_doctor(0);
                    }
                }
                userDoctors.update(userDoctor);
            }
            return pass(HttpStatus.OK, trim(trim), getBadge(database, request));
        }
        return pass(HttpStatus.OK, trim(trim));
    }

    @Authorize
    public ResponseEntity getConnections(Database database, Request request) throws Throwable {
        Dao<UserDoctor, Long> daoLong = getDaoLong(database, UserDoctor.class);
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
        Dao<User, UUID> users = getDao(database, User.class);
        FilterModel<UserDoctor, Long> filter = filter(daoLong, request);
        UserDoctor info = parse(request, UserDoctor.class);
        if (info != null) {
            Where<UserDoctor, Long> where = filter.where();
            if (info.getUser_id() != null) {
                where.and().eq(UserDoctor.USER, info.getUser_id());
            }
            if (info.getDoctor_id() != null) {
                where.and().eq(UserDoctor.DOCTOR, info.getDoctor_id());
            }
        }
        QueryBuilder<UserDoctor, Long> builder = filter.builder();
        builder.orderBy(UserDoctor.LAST_TIMESTAMP, false);
        List<UserDoctor> query = builder.leftJoin(UserDoctor.USER, User.ID, users.queryBuilder()).query();
        List<Doctor> allDoctors = doctors.queryForAll();
        for (UserDoctor doctor : query) {
            for (Doctor dr : allDoctors) {
                if (doctor.getDoctor() != null) {
                    if (dr.getDoctor_id().equals(doctor.getDoctor().getDoctor_id())) {
                        doctor.setDoctor(dr);
                        break;
                    }
                }
            }
        }
        return pass(HttpStatus.OK, trim(query));
    }

    @Authenticate
    public ResponseEntity create(Database database, Request request) throws Throwable {
        Consult consult = parse(request);
        if (consult == null || consult.getRecord_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        if (request.getAuth_user() != null && consult.getDoctor_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        if (consult.getPost_type() == Consult.TYPE_CHARACTER && StringUtil.isEmpty(consult.getPost_message())) {
            return pass(Consult.MESSAGE, HttpStatus.BAD_REQUEST);
        }
        if (consult.getPost_type() == Consult.TYPE_SUGGEST && (request.getAuth_doctor() == null || consult.getPost_suggests() == null || consult.getPost_suggests().length == 0)) {
            return pass(Consult.MESSAGE, HttpStatus.BAD_REQUEST);
        }
        if (consult.getPost_type() != Consult.TYPE_CHARACTER && consult.getPost_type() != Consult.TYPE_SUGGEST && consult.getPost_attachment() == null) {
            return pass(Consult.MESSAGE, HttpStatus.BAD_REQUEST);
        }
        Dao<Consult, Long> daoLong = getDaoLong(database);

        Dao<UserDoctor, Long> dao = getDaoLong(database, UserDoctor.class);
        UserDoctor record = dao.queryForId(consult.getRecord_id());
        if (record == null || (request.getAuth_type() == TokenUtil.TokenType.USER && !record.getUser().getUser_id().equals(request.getAuth()))
                || (request.getAuth_type() == TokenUtil.TokenType.DOCTOR && !record.getDoctor().getDoctor_id().equals(request.getAuth()))) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        if (record.getRecord_canceled() != null && record.getRecord_canceled()) {
            return pass(UserDoctor.MESSAGE_CANCEL_CONSULT, HttpStatus.EXPECTATION_FAILED);
        }
        UserPackage selectedPackage = null;
        if (request.getAuth_type() == TokenUtil.TokenType.USER) {
            Dao<SubPackage, UUID> daoPacks = getDao(database, SubPackage.class);
            Dao<UserPackage, Long> daoUsers = getDaoLong(database, UserPackage.class);
            QueryBuilder<UserPackage, Long> builder = daoUsers.queryBuilder();
            Where<UserPackage, Long> where = builder.where();
            where.eq(UserPackage.USER, request.getAuth()).and()
                    .eq(UserPackage.DOCTOR, record.getDoctor().getDoctor_id());
            List<UserPackage> query = builder.innerJoin(UserPackage.PACKAGE, SubPackage.ID, daoPacks.queryBuilder()).query();
            boolean foundButExpired = false;
            for (UserPackage userPackage : query) {
                if (selectedPackage == null) {
                    switch (consult.getPost_type()) {
                        case Consult.TYPE_CHARACTER:
                            userPackage.setPackage_character(userPackage.getPackage_character() + consult.getPost_message().trim().length());
                            if (userPackage.getPackage_character() <= userPackage.getSub_package().getPackage_character()) {
                                selectedPackage = userPackage;
                            }
                            break;
                        case Consult.TYPE_AUDIO:
                            userPackage.setPackage_voice(userPackage.getPackage_voice() + 1);
                            if (userPackage.getPackage_voice() <= userPackage.getSub_package().getPackage_voice()) {
                                selectedPackage = userPackage;
                            }
                            break;
                        case Consult.TYPE_IMAGE:
                            userPackage.setPackage_image(userPackage.getPackage_image() + 1);
                            if (userPackage.getPackage_image() <= userPackage.getSub_package().getPackage_image()) {
                                selectedPackage = userPackage;
                            }
                            break;
                        case Consult.TYPE_VIDEO:
                            userPackage.setPackage_video(userPackage.getPackage_video() + 1);
                            if (userPackage.getPackage_video() <= userPackage.getSub_package().getPackage_video()) {
                                selectedPackage = userPackage;
                            }
                            break;
                    }
                }
                if (selectedPackage != null) {
                    long expire = SettingController.getParamAsInt(database, Setting.REMAIN_TIME);
                    Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
                    Doctor doctor = doctors.queryForId(record.getDoctor().getDoctor_id());
//                    if (!StringUtil.isEmpty(doctor.getDoctor_worktable())) {
//                        expire = Long.parseLong(doctor.getDoctor_worktable());
//                    }
                    expire = TimeUnit.HOURS.toMillis(expire);

                    QueryBuilder<Consult, Long> firstDoctor = daoLong.queryBuilder();
                    firstDoctor.orderBy(Consult.CREATE_AT, true);
                    Consult firstMessageDoctor = firstDoctor.where().eq(Consult.USER, request.getAuth())
                            .and().eq(Consult.DOCTOR, doctor.getDoctor_id())
                            .and().eq(Consult.OWNER, Consult.OWNER_DOCTOR)
                            .queryForFirst();

                    QueryBuilder<Consult, Long> firstUser = daoLong.queryBuilder();
                    firstUser.orderBy(Consult.CREATE_AT, true);
                    Consult firstMessageUser = firstUser.where().eq(Consult.USER, request.getAuth())
                            .and().eq(Consult.DOCTOR, doctor.getDoctor_id())
                            .and().eq(Consult.OWNER, Consult.OWNER_USER)
                            .queryForFirst();

                    if (firstMessageDoctor != null && firstMessageUser != null) {
                        long create = firstMessageDoctor.getCreate_at() > firstMessageUser.getCreate_at()
                                ? firstMessageDoctor.getCreate_at() : firstMessageUser.getCreate_at();
                        if (Instant.now().toEpochMilli() > create + expire) {
                            selectedPackage = null;
                            foundButExpired = true;
                        }
                    }
                }
            }
            if (selectedPackage == null) {
                return pass(foundButExpired ? SubPackage.EXPIRE : SubPackage.MESSAGE, HttpStatus.EXPECTATION_FAILED);
            }
            daoUsers.update(selectedPackage);
        }

        consult.setUser(record.getUser());
        consult.setDoctor(record.getDoctor());
        consult.setPost_owner(request.getAuth_type() == TokenUtil.TokenType.USER ? Consult.OWNER_USER : Consult.OWNER_DOCTOR);
        Consult created = daoLong.createIfNotExists(consult);

        record.setRecord_count(record.getRecord_count() + 1);
        if (request.getAuth_type() == TokenUtil.TokenType.DOCTOR) {
            record.setRecord_unread_user(record.getRecord_unread_user() + 1);
        } else {
            record.setRecord_unread_doctor(record.getRecord_unread_doctor() + 1);
        }
        if (consult.getPost_type() != Consult.TYPE_SUGGEST) {
            record.setRecord_last(created);
            record.setRecord_last_stamp(created.getCreate_at());
        }
        dao.update(record);

        if (consult.getPost_type() == Consult.TYPE_SUGGEST) {
            Dao<DoctorProduct, Long> doctorProducts = getDaoLong(database, DoctorProduct.class);
            DoctorProduct suggest = doctorProducts.queryBuilder().where().eq(DoctorProduct.USER, record.getUser().getUser_id())
                    .and().eq(DoctorProduct.DOCTOR, record.getDoctor().getDoctor_id()).queryForFirst();
            if (suggest == null) {
                suggest = new DoctorProduct();
                suggest.setUser(record.getUser());
                suggest.setDoctor(record.getDoctor());
            }
            HashSet<UUID> ids = new HashSet<>();
            if (suggest.getRecord_products() != null) {
                Collections.addAll(ids, suggest.getRecord_products());
            }
            ids.addAll(Arrays.asList(consult.getPost_suggests()));
            suggest.setRecord_products(ids.toArray(new UUID[ids.size()]));
            doctorProducts.createOrUpdate(suggest);
        }
        FcmModel fcmModel = new FcmModel();
        Doctor doctor = getDao(database, Doctor.class).queryForId(consult.getDoctor().getDoctor_id());
        User user = getDao(database, User.class).queryForId(consult.getUser().getUser_id());
        if (consult.getPost_owner() == Consult.OWNER_USER) {
            fcmModel.setTo(doctor.getDoctor_pushId());
            fcmModel.setData(user.getUsername() + " پیام جدیدی برای شما ارسال کرد", consult.getPost_message());
        } else {
            fcmModel.setTo(user.getUser_pushId());
            fcmModel.setData(doctor.getDoctor_name() + " پیام جدیدی برای شما ارسال کرد", consult.getPost_message());
        }
        FCMCenter.push(fcmModel);
        return pass(HttpStatus.OK, selectedPackage);
    }

    @Authenticate
    public ResponseEntity getBadgeCounter(Database database, Request request) throws Throwable {
        return pass(HttpStatus.OK, getBadge(database, request));
    }

    @Authenticate
    public ResponseEntity getHistory(Database database, Request request) throws Throwable {
        Doctor doctor = parse(request, Doctor.class);
        if (doctor == null || doctor.getDoctor_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
        Dao<UserDoctor, Long> dao = getDaoLong(database, UserDoctor.class);
        UserDoctor history = dao.queryBuilder().where().eq(UserDoctor.USER, request.getAuth())
                .and().eq(UserDoctor.DOCTOR, doctor.getDoctor_id()).queryForFirst();
        doctors.refresh(history.getDoctor());
        return pass(HttpStatus.OK, history);
    }

    private long getBadge(Database database, Request request) throws Throwable {
        Dao<UserDoctor, Long> dao = getDaoLong(database, UserDoctor.class);
//        List<UserDoctor> query;
        long query;
        if (request.getAuth_user() != null) {
            query = dao.queryBuilder().where().eq(UserDoctor.USER, request.getAuth())
                    .and().not().eq(UserDoctor.UNREAD_USER, 0).countOf();
        } else {
            query = dao.queryBuilder().where().eq(UserDoctor.DOCTOR, request.getAuth())
                    .and().not().eq(UserDoctor.UNREAD_DOCTOR, 0).countOf();
        }
//        int sum = 0;
//        for (UserDoctor doctor : query) {
//            if (request.getAuth_user() != null) {
//                sum = sum + doctor.getRecord_unread_user();
//            } else {
//                sum = sum + doctor.getRecord_unread_doctor();
//            }
//        }
//        return sum;
        return query;
    }
}
