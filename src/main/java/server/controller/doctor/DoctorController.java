package server.controller.doctor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authenticate;
import server.lib.interfacing.Authorize;
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

@RestController
@RequestMapping("/doctor")
public class DoctorController extends Controller<Doctor> {

    private static final boolean SINGLE_SESSION_DOCTOR = false;

    public ResponseEntity login(Database database, Request servletRequest) throws Throwable {
        Doctor request = parse(servletRequest);
        if (request != null && request.getUsername() != null && request.getPassword() != null) {
            request.setUsername(StringUtil.format(request.getUsername()));
            request.setPassword(StringUtil.format(request.getPassword()).toLowerCase());
            if (!TokenUtil.isMD5(request.getPassword())) {
                return pass(HttpStatus.BAD_REQUEST);
            }
            Dao<Doctor, UUID> dao = getDao(database);
            QueryBuilder<Doctor, UUID> builder = dao.queryBuilder();
            Where<Doctor, UUID> where = builder.where();
            where.eq(Doctor.USERNAME, request.getUsername());
            Doctor doctor = where.queryForFirst();
            if (doctor != null) {
                if (TokenUtil.isMatchBycrypt(doctor.getPassword(), request.getPassword())) {
                    doctor.setAuth_version(doctor.getAuth_version() + 1);
                    dao.update(doctor);
                    servletRequest.setAuth_doctor(doctor);
                    return passAuth(HttpStatus.OK, TokenUtil.generateToken(doctor.getDoctor_id()
                            , SINGLE_SESSION_DOCTOR ? doctor.getAuth_version() : 1, TokenUtil.TokenType.DOCTOR));
                }
            }
            return pass(Doctor.CRED_MESSAGE, HttpStatus.EXPECTATION_FAILED);
        }
        return pass(HttpStatus.BAD_REQUEST);
    }

    @Authorize
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Doctor, UUID> dao = getDao(database);
        FilterModel<Doctor, UUID> filter = filter(dao, request);
        if (filter.getSearch_by() != null) {
            filter.where().and().like(Doctor.NAME, "%" + filter.getSearch_by() + "%");
        }
        Dao<DoctorCategory, UUID> categories = getDao(database, DoctorCategory.class);
        List<DoctorCategory> doctorCategories = categories.queryForAll();
        HashMap<UUID, DoctorCategory> map = new HashMap<>();
        for (DoctorCategory category : doctorCategories) {
            map.put(category.getCategory_id(), category);
        }
        List<Doctor> query = filter.query();
        for (Doctor doctor : query) {
            if (doctor.getDoctor_categories() != null) {
                ArrayList<DoctorCategory> data = new ArrayList<>();
                for (UUID uuid : doctor.getDoctor_categories()) {
                    DoctorCategory category = map.get(uuid);
                    if (category != null) {
                        data.add(category);
                    }
                }
                doctor.setCategories(data);
            }
            doctor.setPassword(null);
        }
        return pass(HttpStatus.OK, query);
    }

    @Authorize
    public ResponseEntity create(Database database, Doctor info) throws Throwable {
        if (info.getUsername() == null || info.getPassword() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        if (!TokenUtil.isMD5(info.getPassword())) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        info.setUsername(StringUtil.format(info.getUsername()));
        info.setPassword(TokenUtil.bycrypt(StringUtil.format(info.getPassword())));
        Dao<Doctor, UUID> dao = getDao(database);
        if (dao.queryBuilder().where().eq(Doctor.USERNAME, info.getUsername()).countOf() > 0) {
            return pass(HttpStatus.CONFLICT);
        }
        Doctor model = dao.createIfNotExists(info);

        SubPackage subPackage = new SubPackage();
        subPackage.setPackage_name("بسته مشاوره");
        subPackage.setPackage_image(5);
        subPackage.setPackage_character(1000);
        subPackage.setPackage_voice(2);
        subPackage.setPackage_video(1);
        subPackage.setPackage_price(30000);
        subPackage.setPackage_sell_price(25000);
        subPackage.setPackage_discount(15);
        subPackage.setPackage_brief("5 عکس، 1 ویدیو، 2 فایل صوتی و 1000 کاراکتر");
        subPackage.setDoctor(model);
        subPackage.setPackage_subscription(30);

        Dao<SubPackage, UUID> subPackages = getDao(database, SubPackage.class);
        subPackages.create(subPackage);

        return model == null ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
    }

    @Authorize
    public ResponseEntity update(Database database, Doctor info) throws Throwable {
        if (info == null || info.getDoctor_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Doctor, UUID> dao = getDao(database);
        if (info.getPassword() != null) {
            if (!TokenUtil.isMD5(info.getPassword())) {
                return pass(HttpStatus.BAD_REQUEST);
            }
            info.setPassword(TokenUtil.bycrypt((StringUtil.format(info.getPassword()))));
        }
        Doctor model = dao.queryForId(info.getDoctor_id());
        if (model == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        model.merge(info);
        return dao.update(model) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
    }

    @Authorize
    public ResponseEntity delete(Database database, Doctor info) throws Throwable {
        if (info == null || info.getDoctor_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Doctor, UUID> dao = getDao(database);
        return dao.delete(info) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
    }

    public ResponseEntity getList(Database database, Request request) throws Throwable {
        Dao<Doctor, UUID> dao = getDao(database);
        FilterModel<Doctor, UUID> filter = filter(dao, request);
        Where<Doctor, UUID> where = filter.where();
        if (filter.getParent_by() != null) {
            where.and().like(Doctor.CATEGORIES, "%" + filter.getParent_by() + "%");
        }
        if (filter.getSearch_by() != null) {
            where.and().like(Doctor.NAME, "%" + filter.getSearch_by() + "%");
        }
        where.and().eq(Doctor.VISIBILITY, true);
        filter.builder().orderBy(Doctor.PRIORITY, false);
        List<Doctor> query = filter.query();
        Collections.shuffle(query);
        return pass(HttpStatus.OK, trim(query));
    }

    public ResponseEntity getOne(Database database, Request request) throws Throwable {
        Doctor info = parse(request);
        if (info == null || info.getDoctor_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Doctor, UUID> dao = getDao(database);
        Doctor model = dao.queryForId(info.getDoctor_id());
        if (model != null) {
            model.setDoctor_views(model.getDoctor_views() + 1);
            dao.update(model);
            if (model.getDoctor_categories() != null) {
                Dao<DoctorCategory, UUID> categories = getDao(database, DoctorCategory.class);
                model.setCategories(categories.queryBuilder()
                        .where().in(DoctorCategory.ID, model.getDoctor_categories()).query());
            }
            model.hide();
            if (request.getAuth_user() != null) {
                Dao<UserDoctor, Long> userPackages = getDaoLong(database, UserDoctor.class);
                UserDoctor userDoctor = userPackages.queryBuilder().where().eq(UserDoctor.USER, request.getAuth())
                        .and().eq(UserDoctor.DOCTOR, model.getDoctor_id()).queryForFirst();
                if (userDoctor != null) {
                    model.setHistory(userDoctor);
                }
            }
            return pass(HttpStatus.OK, model);
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }

    @Authenticate
    public ResponseEntity getProfile(Database database, Request request) throws Throwable {
        if (request.getAuth_doctor() == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        Doctor auth_doctor = request.getAuth_doctor();
        appendUnreadMessages(database, auth_doctor);
        appendUnreadTickets(database, auth_doctor);
        return pass(HttpStatus.OK, auth_doctor);
    }

    private void appendUnreadTickets(Database database, Doctor user) throws Throwable {
        Dao<Ticket, UUID> tickets = getDao(database, Ticket.class);
        List<Ticket> query = tickets.queryBuilder()
                .where().eq(Ticket.DOCTOR, user.getDoctor_id())
                .and().isNull(Ticket.PARENT).query();
        long unreadMessages = 0;
        for (Ticket ticket : query) {
            unreadMessages = unreadMessages + ticket.getTicket_unread_doctor();
        }
        user.setUnreadTickets((int) unreadMessages);
    }

    @Authenticate
    public ResponseEntity edit(Database database, Request servletRequest) throws Throwable {
        Doctor updated = parse(servletRequest);
        if (updated == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Doctor, UUID> dao = getDao(database);
        Doctor user = dao.queryForId(servletRequest.getAuth());
        if (user != null) {
            if (updated.getDoctor_pushId() != null) {
                user.setDoctor_pushId(updated.getDoctor_pushId());
            }
            if (updated.getDoctor_availability() != null) {
                user.setDoctor_availability(updated.getDoctor_availability());
            }
            dao.update(user);
            return pass(HttpStatus.OK);
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }

    private void appendUnreadMessages(Database database, Doctor user) throws Throwable {
        Dao<Message, UUID> messages = getDao(database, Message.class);
        Where<Message, UUID> where = messages.queryBuilder().where();
        long unreadMessages = where.and(where.eq(Message.MESSAGE_STATE, Message.STATE_UNREAD)
                , where.eq(Message.DOCTOR_ID, user.getDoctor_id())).countOf();
        user.setUnreadMessages((int) unreadMessages);
    }

    @Authenticate
    public ResponseEntity getMyDoctors(Database database, Request request) throws Throwable {
        Dao<Consult, Long> consults = getDaoLong(database, Consult.class);
        Dao<UserDoctor, Long> daoLong = getDaoLong(database, UserDoctor.class);
        Dao<Doctor, UUID> dao = getDao(database, Doctor.class);
        QueryBuilder<UserDoctor, Long> builder = daoLong.queryBuilder();
        builder.where().eq(UserDoctor.USER, request.getAuth());
        builder.orderBy(UserDoctor.LAST_TIMESTAMP, false);
        List<UserDoctor> query = builder.leftJoin(UserDoctor.DOCTOR, Doctor.ID, dao.queryBuilder()).query();
        for (UserDoctor doctor : query) {
            consults.refresh(doctor.getRecord_last());
        }
        return pass(HttpStatus.OK, trim(query));
    }

    @Authenticate
    public ResponseEntity getMyPatients(Database database, Request request) throws Throwable {
        Dao<Consult, Long> consults = getDaoLong(database, Consult.class);
        Dao<UserDoctor, Long> daoLong = getDaoLong(database, UserDoctor.class);
        QueryBuilder<UserDoctor, Long> builder = daoLong.queryBuilder();
        builder.where().eq(UserDoctor.DOCTOR, request.getAuth());

        Dao<User, UUID> dao = getDao(database, User.class);
        QueryBuilder<User, UUID> userBuilder = dao.queryBuilder();
        String filterBy = null;
        if ((filterBy = request.toFilter().getSearch_by()) != null) {
            userBuilder.where().like(User.USERNAME, "%" + filterBy + "%");
        }
        builder.orderBy(UserDoctor.LAST_TIMESTAMP, false);
        List<UserDoctor> query = builder.leftJoin(UserDoctor.USER, User.ID, userBuilder).query();
        Dao<UserPackage, Long> userPackages = getDaoLong(database, UserPackage.class);
        for (UserDoctor doctor : query) {
            QueryBuilder<UserPackage, Long> queryBuilder = userPackages.queryBuilder();
            queryBuilder.orderBy(UserPackage.CREATE_AT, false);
            UserPackage purchased = queryBuilder.where().eq(UserPackage.USER, doctor.getUser().getUser_id())
                    .and().isNotNull(UserPackage.CONDITION)
                    .and().eq(UserPackage.DOCTOR, doctor.getDoctor().getDoctor_id()).queryForFirst();
            if (purchased != null) {
                doctor.getUser().setCondition(purchased.getUser_condition());
            }
            consults.refresh(doctor.getRecord_last());
        }
        return pass(HttpStatus.OK, trim(query));
    }

    public static boolean isAuthorized(Database database, Request request) throws Throwable {
        if (request.getAuth() == null || request.getAuth_version() == null) return false;
        Dao<Doctor, UUID> dao = getDaoStatic(database, Doctor.class);
        Doctor user = dao.queryForId(request.getAuth());
        if (user != null && (!SINGLE_SESSION_DOCTOR || (SINGLE_SESSION_DOCTOR && user.getAuth_version().equals(request.getAuth_version())))) {
            user.setLatest_activity(Instant.now().toEpochMilli());
            dao.update(user);
            request.setAuth_doctor(user);
            return true;
        }
        return false;
    }
}
