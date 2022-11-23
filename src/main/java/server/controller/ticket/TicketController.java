package server.controller.ticket;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.center.FCMCenter;
import server.lib.interfacing.Authenticate;
import server.lib.interfacing.Authorize;
import server.lib.model.FcmModel;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.QueryBuilder;
import server.lib.orm.stmt.UpdateBuilder;
import server.lib.orm.stmt.Where;
import server.lib.utils.Controller;
import server.lib.utils.Database;

import server.model.Doctor;
import server.model.Ticket;
import server.model.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ticket")
public class TicketController extends Controller<Ticket> {

    @Authorize
    @Authenticate
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Ticket, UUID> dao = getDao(database);
        FilterModel<Ticket, UUID> filter = filter(dao, request);
        QueryBuilder<Ticket, UUID> builder = filter.builder();
        Where where = filter.where();
        boolean addedParent = false;
        if (filter.getSearch_by() != null) {
            String input = "%" + filter.getSearch_by() + "%";
            where.and().or(where.like(Ticket.DESCRIPTION, input), where.like(Ticket.TITLE, input));
        }
        if (filter.getParent_by() != null) {
            UUID id = filter.getParent_by();
            where.and().eq(Ticket.PARENT, id);
            addedParent = true;
        } else {
            if (filter.getFilter_by() != null) {
                switch (Integer.parseInt(filter.getFilter_by())) {
                    case 1:
                        builder.orderBy(Ticket.LATEST, false);
                        break;
                    case 2:
                        builder.orderBy(Ticket.COUNT, false);
                        break;
                    case 3:
                        switch (request.getAuth_type()) {
                            case ADMIN:
                                builder.orderBy(Ticket.UNREAD_SYSTEM, false);
                                break;
                            case DOCTOR:
                                builder.orderBy(Ticket.UNREAD_DOCTOR, false);
                                break;
                            case USER:
                                builder.orderBy(Ticket.UNREAD_USER, false);
                                break;
                        }
                        break;
                }
            }
        }
        if (!addedParent) {
            where.and().isNull(Ticket.PARENT);
        }
        if (request.getAuth_doctor() != null) {
            where.and().eq(Ticket.DOCTOR, request.getAuth());
        } else if (request.getAuth_user() != null) {
            where.and().eq(Ticket.USER, request.getAuth());
        } else {
            Ticket consult = parse(request);
            if (consult != null && consult.getTicket_state() != null) {
                where.and().eq(Ticket.STATE, consult.getTicket_state());
            }
            if (consult != null && consult.getAdmin_id() != null) {
                where.and().eq(Ticket.ADMIN, consult.getAdmin_id());
            }
            if (consult != null && consult.getUser_id() != null) {
                where.and().eq(Ticket.USER, consult.getUser_id());
            }
            if (consult != null && consult.getDoctor_id() != null) {
                where.and().eq(Ticket.DOCTOR, consult.getDoctor_id());
            }
            if (consult != null && consult.getTicket_owner() != null) {
                where.and().eq(Ticket.OWNER, consult.getTicket_owner());
            }
        }
        Dao<User, UUID> users = getDao(database, User.class);
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
        List<Ticket> query = filter.query();
        for(Ticket ticket : query){
            users.refresh(ticket.getUser());
            doctors.refresh(ticket.getDoctor());
        }
        return pass(HttpStatus.OK, query);
    }

    @Authorize
    @Authenticate
    public ResponseEntity readAll(Database database, Request request) throws Throwable {
        Ticket ticket = parse(request);
        if (ticket == null || ticket.getTicket_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Ticket, UUID> dao = getDao(database);
        ticket = dao.queryForId(ticket.getTicket_id());
        UpdateBuilder<Ticket, UUID> builder = dao.updateBuilder();
        Where<Ticket, UUID> where = builder.where();
        switch (request.getAuth_type()){
            case USER:
            case DOCTOR:
                where.eq(Ticket.OWNER, Ticket.OWNER_SYSTEM);
                break;
            default:
                where.or(where.eq(Ticket.OWNER, Ticket.OWNER_USER), where.eq(Ticket.OWNER, Ticket.OWNER_DOCTOR));
                break;
        }
        where.and().eq(Ticket.STATUS, Ticket.STATUS_UNREAD);
        where.and().or(where.eq(Ticket.PARENT, ticket.getTicket_id()), where.eq(Ticket.ID, ticket.getTicket_id()));
        builder.updateColumnValue(Ticket.STATUS, Ticket.STATUS_READ).update();
        if (request.getAuth_doctor() != null) {
            ticket.setTicket_unread_doctor(0L);
        } else if (request.getAuth_user() != null) {
            ticket.setTicket_unread_user(0L);
        } else {
            ticket.setTicket_unread_system(0L);
        }
        dao.update(ticket);
        return pass(HttpStatus.OK);
    }

    @Authorize
    @Authenticate
    public ResponseEntity create(Database database, Request request) throws Throwable {
       return createTicket(database, request);
    }

    public static ResponseEntity createTicket(Database database, Request request) throws Throwable {
        Ticket ticket = parse(request, Ticket.class);
        User user = null;
        Doctor doctor = null;
        if (request.getAuth_doctor() != null) {
            doctor = request.getAuth_doctor();
            ticket.setTicket_owner(Ticket.OWNER_DOCTOR);
        } else if (request.getAuth_user() != null) {
            user = request.getAuth_user();
            ticket.setTicket_owner(Ticket.OWNER_USER);
        } else {
            if (ticket.getUser_id() != null) {
                user = getDaoStatic(database, User.class).queryForId(ticket.getUser_id());
            } else if (ticket.getDoctor_id() != null) {
                doctor = getDaoStatic(database, Doctor.class).queryForId(ticket.getDoctor_id());
            }
            ticket.setAdmin(request.getAuth_admin());
            ticket.setTicket_owner(Ticket.OWNER_SYSTEM);
        }
        if ((user == null && doctor == null) || ticket.getTicket_description() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        ticket.setUser(user);
        ticket.setDoctor(doctor);
        ticket.setTicket_status(Ticket.STATUS_UNREAD);
        ticket.setTicket_state(Ticket.STATE_INIT);
        if (ticket.getParent_id() != null) {
            Ticket parent = new Ticket();
            parent.setTicket_id(ticket.getParent_id());
            ticket.setParent(parent);
        }
        Dao<Ticket, UUID> dao = getDaoStatic(database, Ticket.class);
        if (ticket.getParent() == null) {
            ticket.setTicket_count(1L);
            ticket.setTicket_latest(Instant.now().toEpochMilli());
            if (request.getAuth_user() != null || request.getAuth_doctor() != null) {
                ticket.setTicket_unread_system(1L);
            } else {
                if (user != null) {
                    ticket.setTicket_unread_user(1L);
                } else {
                    ticket.setTicket_unread_doctor(1L);
                }
            }
        } else {
            Ticket parent = dao.queryForId(ticket.getParent().getTicket_id());
            parent.setTicket_count(parent.getTicket_count() + 1);
            parent.setTicket_latest(Instant.now().toEpochMilli());
            if (request.getAuth_user() != null || request.getAuth_doctor() != null) {
                parent.setTicket_unread_system(parent.getTicket_unread_system() + 1);
            } else {
                if (user != null) {
                    parent.setTicket_unread_user(parent.getTicket_unread_user() + 1);
                } else {
                    parent.setTicket_unread_doctor(parent.getTicket_unread_doctor() + 1);
                }
            }
            dao.update(parent);
        }
        if (ticket.getTicket_owner() == Ticket.OWNER_SYSTEM) {
            FcmModel model = new FcmModel();
            model.setTo(user != null ? user.getUser_pushId() : doctor.getDoctor_pushId());
            model.setData("کاربر گرامی، به تیکت شما پاسخ داده شد.", ticket.getTicket_description());
            FCMCenter.push(model);
        }
        dao.createIfNotExists(ticket);
        return pass(HttpStatus.OK);
    }

    @Authorize
    @Authenticate
    public ResponseEntity update(Database database, Request request) throws Throwable {
        Ticket ticket = parse(request);
        Dao<Ticket, UUID> dao = getDao(database);
        Ticket item = dao.queryForId(ticket.getTicket_id());
        if (item == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        if (item.getTicket_owner() == Ticket.OWNER_SYSTEM && request.getAuth_admin() == null) {
            return pass(HttpStatus.FORBIDDEN);
        } else if ((item.getTicket_owner() == Ticket.OWNER_USER && request.getAuth_user() != null)
                || (item.getTicket_owner() == Ticket.OWNER_DOCTOR && request.getAuth_doctor() != null)) {
            if (item.getDoctor() != null) {
                if (!item.getDoctor().getDoctor_id().equals(request.getAuth())) {
                    return pass(HttpStatus.UNAUTHORIZED);
                }
            } else if (item.getUser() != null) {
                if (!item.getUser().getUser_id().equals(request.getAuth())) {
                    return pass(HttpStatus.UNAUTHORIZED);
                }
            }
        }
        if (request.getAuth_admin() != null) {
            if (ticket.getTicket_status() != null) {
                item.setTicket_status(ticket.getTicket_status());
            }
            if (ticket.getTicket_state() != null) {
                item.setTicket_state(ticket.getTicket_state());
            }
        }
        item.merge(ticket);
        dao.update(item);
        return pass(HttpStatus.OK);
    }
}
