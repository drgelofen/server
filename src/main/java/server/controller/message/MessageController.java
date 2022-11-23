package server.controller.message;

import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.DeleteBuilder;
import server.lib.orm.stmt.UpdateBuilder;
import server.lib.orm.stmt.Where;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authenticate;
import server.lib.interfacing.Authorize;
import server.lib.model.FcmModel;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.utils.*;
import server.lib.center.FCMCenter;
import server.model.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/message")
public class MessageController extends Controller<Message> {

    @Authorize
    public ResponseEntity getSent(Database database, Request request) throws Throwable {
        Dao<Message, Long> dao = getDaoLong(database);
        FilterModel<Message, Long> filter = filter(dao, request);
        filter.builder().distinct().selectColumns(Message.MESSAGE_IDENTITY);
        List<Message> messages = filter.query();
        HashSet<String> set = new HashSet<>();
        ArrayList<Message> list = new ArrayList<>();
        for (Message message : messages) {
            if (!set.contains(message.getMessage_identity())) {
                set.add(message.getMessage_identity());
                list.add(message);
            }
        }
        return pass(HttpStatus.OK, list);
    }

    @Authorize
    public ResponseEntity delete(Database database, Message request) throws Throwable {
        if (request == null || request.getMessage_identity() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Message, Long> dao = getDaoLong(database);
        DeleteBuilder<Message, Long> deleteBuilder = dao.deleteBuilder();
        Where<Message, Long> where = deleteBuilder.where();
        where.eq(Message.MESSAGE_IDENTITY, request.getMessage_identity());
        deleteBuilder.delete();
        return pass(HttpStatus.OK);
    }

    @Authenticate
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Message, Long> dao = getDaoLong(database);
        FilterModel<Message, Long> filter = filter(dao, request);
        Where where = filter.where();
        if (request.getAuth_doctor() != null) {
            where.and().eq(Message.DOCTOR_ID, request.getAuth());
        } else {
            where.and().eq(Message.USER_ID, request.getAuth());
        }
        return pass(HttpStatus.OK, trim(filter.query()));
    }

    @Authenticate
    public ResponseEntity readAll(Database database, Request request) throws Throwable {
        Dao<Message, Long> dao = getDaoLong(database);
        UpdateBuilder<Message, Long> updateBuilder = dao.updateBuilder();
        Where where = updateBuilder.where();
        String column;
        if (request.getAuth_doctor() != null) {
            column = Message.DOCTOR_ID;
        } else {
            column = Message.USER_ID;
        }
        where.and(where.eq(column, request.getAuth()), where.eq(Message.MESSAGE_STATE, Message.STATE_UNREAD));
        updateBuilder.updateColumnValue(Message.MESSAGE_STATE, Message.STATE_READ);
        updateBuilder.update();
        return pass(HttpStatus.OK);
    }

    @Authorize
    public ResponseEntity sendToAll(Database database, Request request) throws Throwable {
        Message info = parse(request);
        if (info == null || info.getMessage_body() == null || info.getMessage_title() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        if (info.getMessage_type() == null) {
            info.setMessage_type(Message.TYPE_PUSH_USERS);
        }
        Dao<Message, Long> dao = getDaoLong(database);
        List userList;
        String identity = Instant.now().toEpochMilli() + "-" + StringUtil.randomAlphaNumeric(10);
        if (info.getMessage_type() == Message.TYPE_PUSH_DOCTORS) {
            if (info.getMessage_topic() == null) {
                info.setMessage_topic("doctors");
            }
            Dao<Doctor, UUID> users = getDao(database, Doctor.class);
            userList = users.queryForAll();
        } else {
            if (info.getMessage_topic() == null) {
                info.setMessage_topic("users");
            }
            Dao<User, UUID> users = getDao(database, User.class);
            userList = users.queryForAll();
        }
        ArrayList<Message> messages = new ArrayList<>();
        for (Object target : userList) {
            Message message = new Message();
            if (target instanceof User) {
                message.setUser((User) target);
            } else {
                message.setDoctor((Doctor) target);
            }
            message.setMessage_type(info.getMessage_type());
            message.setMessage_title(info.getMessage_title());
            message.setMessage_identity(identity);
            message.setMessage_body(info.getMessage_body());
            message.setMessage_category(Message.CATEGORY_PUBLIC);
            message.setMessage_state(Message.STATE_UNREAD);
            message.setMessage_topic(info.getMessage_topic());
            messages.add(message);
        }
        dao.create(messages);

        FcmModel model = new FcmModel();
        model.setTopic(info.getMessage_topic());
        model.setData(info.getMessage_title(), info.getMessage_body(), info.getMessage_type());

        String response = FCMCenter.push(model);
        return pass(HttpStatus.OK, response);
    }

    @Authorize
    public ResponseEntity send(Database database, Request request) throws Throwable {
        Message info = parse(request);
        if (info == null || info.getMessage_body() == null || info.getMessage_title() == null
                || info.getAudience() == null || info.getAudience().length == 0) {
            return pass(HttpStatus.BAD_REQUEST);
        }

        Dao<Message, Long> dao = getDaoLong(database);
        Dao<User, UUID> users = getDao(database, User.class);
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);

        List<Doctor> queryDoctor = doctors.queryBuilder().where().in(Doctor.ID, info.getAudience()).query();
        List<User> queryUser = users.queryBuilder().where().in(User.ID, info.getAudience()).query();
        ArrayList userList = new ArrayList();
        userList.addAll(queryDoctor);
        userList.addAll(queryUser);

        HashSet<String> userTokens = new HashSet<>();

        String identity = Instant.now().toEpochMilli() + "-" + StringUtil.randomAlphaNumeric(10);
        ArrayList<Message> messages = new ArrayList<>();
        for (Object target : userList) {
            Message message = new Message();
            if (target instanceof User) {
                message.setUser((User) target);
                message.setMessage_type(Message.TYPE_PUSH_USERS);
                info.setMessage_type(Message.TYPE_PUSH_USERS);
                if (((User) target).getUser_pushId() != null) {
                    userTokens.add(((User) target).getUser_pushId());
                }
            } else {
                message.setDoctor((Doctor) target);
                message.setMessage_type(Message.TYPE_PUSH_DOCTORS);
                info.setMessage_type(Message.TYPE_PUSH_DOCTORS);
                if (((Doctor) target).getDoctor_pushId() != null) {
                    userTokens.add(((Doctor) target).getDoctor_pushId());
                }
            }
            message.setMessage_identity(identity);
            message.setMessage_title(info.getMessage_title());
            message.setMessage_body(info.getMessage_body());
            message.setMessage_category(Message.CATEGORY_INDIVIDUAL);
            message.setMessage_state(Message.STATE_UNREAD);
            messages.add(message);
        }
        dao.create(messages);

        FcmModel model = new FcmModel();
        if (userTokens.size() == 1) {
            model.setTo(userTokens.toArray(new String[userTokens.size()])[0]);
        } else {
            model.setRegistration_ids(userTokens.toArray(new String[userTokens.size()]));
        }
        model.setData(info.getMessage_title(), info.getMessage_body(), info.getMessage_type());

        String response = FCMCenter.push(model);
        return pass(HttpStatus.OK, response);
    }
}
