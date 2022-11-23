package server.controller.user;

import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.QueryBuilder;
import server.lib.orm.stmt.Where;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.Application;
import server.lib.center.MailCenter;
import server.lib.interfacing.Authenticate;
import server.lib.interfacing.Authorize;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.utils.*;
import server.lib.center.OTPCenter;
import server.model.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController extends Controller<User> {

    private static final boolean SINGLE_SESSION_USER = false;

    public static final int MAXIMUM_WRONG_CAPTCHA = 5;
    public static final int VERIFY_CODE_LENGTH = 4;
    public static final int SKIP_PARAM = 69;

    public ResponseEntity login(Database database, User request) throws Throwable {
        String message = request.verify();
        if (message != null) {
            return pass(message, HttpStatus.BAD_REQUEST);
        }
        Dao<UserTemp, Long> tempDao = getDaoLong(database, UserTemp.class);
        Dao<User, UUID> dao = getDao(database);
        UserTemp tempUser;
        User user;
        boolean isEmail = false;
        if (!StringUtil.isEmpty(request.getPhone())) {
            tempUser = tempDao.queryBuilder().where().eq(UserTemp.PHONE, request.getPhone()).queryForFirst();
            user = dao.queryBuilder().where().eq(User.PHONE, request.getPhone()).queryForFirst();
        } else {
            tempUser = tempDao.queryBuilder().where().eqIgnoreCase(UserTemp.EMAIL, request.getEmail()).queryForFirst();
            user = dao.queryBuilder().where().eqIgnoreCase(User.EMAIL, request.getEmail()).queryForFirst();
            isEmail = true;
        }
        if (user == null || tempUser == null) {
            return pass(User.EXIST_MESSAGE, HttpStatus.EXPECTATION_FAILED);
        }
        if (!StringUtil.isEmpty(request.getPassword())) {
            if (TokenUtil.isMatchBycrypt(user.getPassword(), request.getPassword())) {
                if (user.getSuspend() != null && user.getSuspend()) {
                    return pass(User.SUSPEND_MESSAGE, HttpStatus.FORBIDDEN);
                }
                user.setAuth_version(user.getAuth_version() + 1);
                dao.update(user);
                return passAuth(HttpStatus.OK, TokenUtil.generateToken(user.getUser_id()
                        , SINGLE_SESSION_USER ? user.getAuth_version() : 1, TokenUtil.TokenType.USER));
            } else {
                return pass(User.AUTH_MESSAGE ,HttpStatus.EXPECTATION_FAILED);
            }
        } else {
            tempUser.setTemp_type(UserTemp.TYPE_LOGIN);
            tempUser.setTemp_attempts(0);
            tempUser.setCaptcha(StringUtil.random(VERIFY_CODE_LENGTH));
            tempDao.update(tempUser);
            if (isEmail) {
                MailCenter.send(tempUser.getEmail(), tempUser.getCaptcha());
            } else {
                OTPCenter.send(tempUser.getPhone(), tempUser.getCaptcha());
            }
            return pass(HttpStatus.OK, Application.ENABLE_OTP ? null : tempUser);
        }
    }

    @Authorize
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<User, UUID> dao = getDao(database);
        FilterModel<User, UUID> filter = filter(dao, request);
        QueryBuilder<User, UUID> builder = filter.builder();
        Where<User, UUID> where = filter.where();
        if (filter.getSearch_by() != null) {
            String input = "%" + filter.getSearch_by() + "%";
            where.and().or(where.like(User.PHONE, input)
                    , where.like(User.USERNAME, input)
                    , where.like(User.EMAIL, input));
        }
        if (filter.getFilter_by() != null) {
            switch (filter.getFilter_by()) {
                case User.PURCHASED_AMOUNT:
                    builder.orderBy(User.PURCHASED_AMOUNT, false);
                    break;
                case User.CREATE_AT:
                    builder.orderBy(User.CREATE_AT, false);
                    break;
                case User.WALLET:
                    builder.orderBy(User.WALLET, false);
                    break;
                case User.SUBSCRIPTION:
                    builder.orderBy(User.SUBSCRIPTION, false);
                    break;
                case User.INVITES:
                    builder.orderBy(User.INVITES, false);
                    break;
                case User.SUSPEND:
                    where.and().eq(User.SUSPEND, true);
                    break;
                case User.LIMIT:
                    where.and().eq(User.LIMIT, true);
                    break;
            }
        }
        List<User> query = filter.query();
        return pass(HttpStatus.OK, query);
    }

    @Authorize
    public ResponseEntity getOne(Database database, Request request) throws Throwable {
        User user = parse(request);
        if (user == null || user.getUser_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<User, UUID> dao = getDao(database);
        return pass(HttpStatus.OK, dao.queryForId(user.getUser_id()));
    }

    public ResponseEntity register(Database database, UserTemp request) throws Throwable {
        String message = request.verify();
        if (message != null) {
            return pass(message, HttpStatus.BAD_REQUEST);
        }
        Dao<User, UUID> dao = getDao(database);
        Dao<UserTemp, Long> tempDao = getDaoLong(database, UserTemp.class);
        UserTemp tempUser;
        boolean isEmail = false;
        String identifier;
        if (!StringUtil.isEmpty(request.getPhone())) {
            identifier = request.getPhone();
            if (dao.queryBuilder().where().eq(User.PHONE, identifier).countOf() > 0) {
                return pass(User.PHONE_MESSAGE, HttpStatus.CONFLICT);
            }
            tempUser = tempDao.queryBuilder().where().eq(UserTemp.PHONE, identifier).queryForFirst();
        } else {
            identifier = request.getEmail();
            isEmail = true;
            if (dao.queryBuilder().where().eqIgnoreCase(User.EMAIL, identifier).countOf() > 0) {
                return pass(User.EMAIL_MESSAGE, HttpStatus.CONFLICT);
            }
            tempUser = tempDao.queryBuilder().where().eqIgnoreCase(UserTemp.EMAIL, identifier).queryForFirst();
        }
        if (tempUser != null) {
            if (isEmail) {
                tempUser.setPassword(request.getPassword());
            }
        } else {
            tempUser = new UserTemp();
            if (isEmail) {
                tempUser.setPassword(request.getPassword());
                tempUser.setEmail(identifier);
            } else {
                tempUser.setPhone(identifier);
            }
        }
        tempUser.setUsername(request.getUsername());
        tempUser.setTemp_type(UserTemp.TYPE_REGISTER);
        tempUser.setTemp_attempts(0);
        tempUser.setCaptcha(StringUtil.random(VERIFY_CODE_LENGTH));
        tempDao.createOrUpdate(tempUser);
        if (isEmail) {
            MailCenter.send(tempUser.getEmail(), tempUser.getCaptcha());
        } else {
            OTPCenter.send(tempUser.getPhone(), tempUser.getCaptcha());
        }
        return pass(HttpStatus.OK, Application.ENABLE_OTP ? null : tempUser);
    }

    public ResponseEntity verify(Database database, UserTemp request) throws Throwable {
        request.setPhone(StringUtil.format(request.getPhone()));
        request.setEmail(StringUtil.format(request.getEmail()));
        request.setCaptcha(StringUtil.format(request.getCaptcha()));
        if (StringUtil.isEmpty(request.getCaptcha())) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        if (StringUtil.isEmpty(request.getEmail()) && StringUtil.isEmpty(request.getPhone())) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<User, UUID> dao = getDao(database);
        Dao<UserTemp, Long> tempDao = getDaoLong(database, UserTemp.class);
        UserTemp tempUser;
        User user;
        if (!StringUtil.isEmpty(request.getPhone())) {
            tempUser = tempDao.queryBuilder().where().eq(UserTemp.PHONE, request.getPhone()).queryForFirst();
            user = dao.queryBuilder().where().eq(User.PHONE, request.getPhone()).queryForFirst();
        } else {
            tempUser = tempDao.queryBuilder().where().eqIgnoreCase(UserTemp.EMAIL, request.getEmail()).queryForFirst();
            user = dao.queryBuilder().where().eqIgnoreCase(User.EMAIL, request.getEmail()).queryForFirst();
        }
        if (tempUser == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        if (tempUser.getTemp_type().equals(UserTemp.TYPE_REGISTER) && user != null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        } else if (tempUser.getTemp_type().equals(UserTemp.TYPE_LOGIN) && user == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        if (tempUser.getCaptcha() == null) {
            return pass(UserTemp.CAPTCHA_MESSAGE, HttpStatus.EXPECTATION_FAILED);
        }
        if (tempUser.getCaptcha().equals(request.getCaptcha())) {
            if (tempUser.getTemp_type().equals(UserTemp.TYPE_REGISTER)) {
                user = new User();
                user.setPhone(tempUser.getPhone());
                user.setEmail(tempUser.getEmail());
                user.setPassword(TokenUtil.bycrypt(tempUser.getPassword()));
                user.setUsername(tempUser.getUsername());
                user.setUser_share_id((SKIP_PARAM + tempDao.countOf()) + StringUtil.randomAlphaNumeric(7));
                user = dao.createIfNotExists(user);
                user.setAuth_version(0L);
                ReferController.inviteRefer(user, database, dao);
            }
            if (user.getSuspend() != null && user.getSuspend()) {
                return pass(User.SUSPEND_MESSAGE, HttpStatus.FORBIDDEN);
            }
            user.setAuth_version(user.getAuth_version() + 1);
            dao.update(user);
            return passAuth(HttpStatus.OK, TokenUtil.generateToken(user.getUser_id(), SINGLE_SESSION_USER ? user.getAuth_version() : 1, TokenUtil.TokenType.USER));
        } else {
            tempUser.setTemp_attempts(tempUser.getTemp_attempts() + 1);
            if (tempUser.getTemp_attempts() > MAXIMUM_WRONG_CAPTCHA) {
                tempUser.setTemp_attempts(0);
                tempUser.setCaptcha(null);
            }
            tempDao.update(tempUser);
            return pass(User.CAPTCHA_MESSAGE, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Authenticate
    public ResponseEntity edit(Database database, Request servletRequest) throws Throwable {
        User updated = parse(servletRequest);
        if (updated == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<User, UUID> dao = getDao(database);
        User user = dao.queryForId(servletRequest.getAuth());
        if (user != null) {
            user.merge(updated);
            dao.update(user);
            appendUnreadMessages(database, user);
            appendUnreadTickets(database, user);
            appendCoaches(database, user);
            return pass(HttpStatus.OK, user.trim());
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }

    @Authorize
    public ResponseEntity delete(Database database, User deleted) throws Throwable {
        if (deleted.getUser_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<User, UUID> dao = getDao(database);
        return dao.delete(deleted) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
    }

    @Authorize
    public ResponseEntity update(Database database, User edited) throws Throwable {
        if (edited.getUser_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<User, UUID> dao = getDao(database);
        User user = dao.queryForId(edited.getUser_id());
        if (user == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        user.merge(edited);
        if (edited.getUser_wallet() != null) {
            user.setUser_wallet(edited.getUser_wallet());
        }
        if (edited.getUser_score() != null) {
            user.setUser_score(edited.getUser_score());
        }
        if (edited.getSuspend() != null) {
            user.setSuspend(edited.getSuspend());
        }
        if (edited.getLimit() != null) {
            user.setLimit(edited.getLimit());
        }
        if (edited.getRemainSubscription() != null) {
            if (!edited.getRemainSubscription().equals(user.getRemainSubscription())) {
                user.setUser_subscription(Instant.now().toEpochMilli() + TimeUnit.DAYS.toMillis(edited.getRemainSubscription()));
            }
        }
        return dao.update(user) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, user);
    }

    @Authenticate
    public ResponseEntity setPassword(Database database, Request request) throws Throwable {
        User item = parse(request);
        if (item == null || item.getPassword() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<User, UUID> dao = getDao(database);
        User user = dao.queryForId(request.getAuth());
        if (user.getPassword() == null || (user.getOldPassword() != null && TokenUtil.isMatchBycrypt(user.getPassword(), item.getOldPassword().toLowerCase()))) {
            user.setPassword(TokenUtil.bycrypt(item.getPassword()));
            dao.update(user);
            return pass(HttpStatus.OK);
        }
        return pass(User.PASSWORD_MESSAGE, HttpStatus.EXPECTATION_FAILED);
    }

    @Authenticate
    public ResponseEntity getProfile(Database database, Request request) throws Throwable {
        if (request.getAuth_user() == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        User user = request.getAuth_user();
        appendUnreadMessages(database, user);
        appendUnreadTickets(database, user);
        appendCoaches(database, user);
        return pass(HttpStatus.OK, user.trim());
    }

    private void appendCoaches(Database database, User user) throws Throwable {
        Dao<UserDoctor, Long> daoLong = getDaoLong(database, UserDoctor.class);
        long count = daoLong.queryBuilder().where().eq(UserDoctor.USER, user.getUser_id()).countOf();
        user.setHasCoach(count > 0);
    }

    private void appendUnreadMessages(Database database, User user) throws Throwable {
        Dao<Message, UUID> messages = getDao(database, Message.class);
        Where<Message, UUID> where = messages.queryBuilder().where();
        long unreadMessages = where.and(where.eq(Message.MESSAGE_STATE, Message.STATE_UNREAD)
                , where.eq(Message.USER_ID, user.getUser_id())).countOf();
        user.setUnreadMessages((int) unreadMessages);
    }

    private void appendUnreadTickets(Database database, User user) throws Throwable {
        Dao<Ticket, UUID> tickets = getDao(database, Ticket.class);
        List<Ticket> query = tickets.queryBuilder().where().eq(Ticket.USER, user.getUser_id())
                .and().isNull(Ticket.PARENT).query();
        long unreadMessages = 0;
        for (Ticket ticket : query) {
            unreadMessages = unreadMessages + ticket.getTicket_unread_user();
        }
        user.setUnreadTickets((int) unreadMessages);
    }

    public static boolean isAuthorized(Database database, Request request) throws Throwable {
        if (request.getAuth() == null || request.getAuth_version() == null) return false;
        Dao<User, UUID> dao = getDaoStatic(database, User.class);
        User user = dao.queryForId(request.getAuth());
        if (user != null && (!SINGLE_SESSION_USER || (SINGLE_SESSION_USER && user.getAuth_version().equals(request.getAuth_version())))) {
            user.setLatest_activity(Instant.now().toEpochMilli());
            dao.update(user);
            request.setAuth_user(user);
            return true;
        }
        return false;
    }
}
