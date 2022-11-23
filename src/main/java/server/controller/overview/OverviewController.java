package server.controller.overview;

import com.google.gson.JsonObject;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.Where;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authorize;
import server.lib.model.Request;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.model.Payment;
import server.model.Ticket;
import server.model.User;
import server.model.UserDoctor;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/overview")
public class OverviewController extends Controller<User> {

    @Authorize(permissions = {Payment.class, User.class}, important = false)
    public ResponseEntity report(Database database, Request request) throws Throwable {
        JsonObject object = new JsonObject();
        try {
            userTable(database, object);
            paymentTable(database, object);
            ticketTable(database, object);
            consultTable(database, object);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return pass(HttpStatus.OK, object);
    }

    private void consultTable(Database database, JsonObject object) throws Throwable {
        Dao<UserDoctor, Long> dao = getDaoLong(database, UserDoctor.class);
        object.addProperty("consult_count", dao.countOf());
        long sum = 0;
        for (UserDoctor doctor : dao) {
            sum = (long) (sum + doctor.getRecord_count());
        }
        object.addProperty("consult_message_count", sum);
    }

    private void ticketTable(Database database, JsonObject object) throws Throwable {
        Dao<Ticket, UUID> dao = getDao(database, Ticket.class);
        List<Ticket> query = dao.queryBuilder().where().isNull(Ticket.PARENT).query();
        object.addProperty("ticket_count", query.size());
        long sum = 0;
        for (Ticket ticket : query) {
            sum = (long) (sum + ticket.getTicket_count());
        }
        object.addProperty("ticket_message_count", sum);
    }

    private void userTable(Database database, JsonObject object) throws Throwable {
        long now = Calendar.getInstance().getTimeInMillis();
        Dao<User, UUID> dao = getDao(database);
        long allUsers = dao.countOf();
        long thisMonth = dao.queryBuilder().where().between(User.CREATE_AT
                , now - TimeUnit.DAYS.toMillis(30), now).countOf();
        long lastMonth = dao.queryBuilder().where().between(User.CREATE_AT
                , now - TimeUnit.DAYS.toMillis(60), now - TimeUnit.DAYS.toMillis(30)).countOf();
        float stat;
        if (lastMonth == 0) {
            stat = thisMonth * 100f;
        } else {
            stat = ((1f * thisMonth) / (1f * lastMonth)) * 100f;
        }
        if (stat < 100) {
            stat = stat * -1;
        }
        object.addProperty("user_count", allUsers);
        object.addProperty("user_rate", stat);
        object.addProperty("user_differ", thisMonth - lastMonth);
    }

    private void paymentTable(Database database, JsonObject object) throws Throwable {
        Dao<Payment, UUID> dao = getDao(database, Payment.class);
        Where<Payment, UUID> where = dao.queryBuilder().where();
        List<Payment> query = where.eq(Payment.STATE, Payment.STATE_DONE).and()
                .or(where.eq(Payment.TYPE, Payment.TYPE_BUY_DOCTOR)
                        , where.eq(Payment.TYPE, Payment.TYPE_CHARGE)).query();
        long sum = 0;
        for (Payment payment : query) {
            sum = (long) (sum + payment.getPayment_amount());
        }
        long now = Calendar.getInstance().getTimeInMillis();
        where = dao.queryBuilder().where();
        query = where.eq(Payment.STATE, Payment.STATE_DONE).and().between(Payment.CREATE_AT
                , now - TimeUnit.DAYS.toMillis(30), now).and()
                .or(where.eq(Payment.TYPE, Payment.TYPE_BUY_DOCTOR)
                        , where.eq(Payment.TYPE, Payment.TYPE_CHARGE)).query();
        long sumThisMonth = 0;
        for (Payment payment : query) {
            sumThisMonth = (long) (sumThisMonth + payment.getPayment_amount());
        }
        where = dao.queryBuilder().where();
        query = where.eq(Payment.STATE, Payment.STATE_DONE).and().between(Payment.CREATE_AT
                , now - TimeUnit.DAYS.toMillis(60), now - TimeUnit.DAYS.toMillis(30)).and()
                .or(where.eq(Payment.TYPE, Payment.TYPE_BUY_DOCTOR)
                        , where.eq(Payment.TYPE, Payment.TYPE_CHARGE)).query();
        long sumLastMonth = 0;
        for (Payment payment : query) {
            sumLastMonth = (long) (sumLastMonth + payment.getPayment_amount());
        }
        float stat;
        if (sumLastMonth == 0) {
            stat = sumThisMonth * 100f;
        } else {
            stat = ((1f * sumThisMonth) / (1f * sumLastMonth)) * 100f;
        }
        if (stat < 100) {
            stat = stat * -1;
        }
        object.addProperty("payment_amount", sum);
        object.addProperty("payment_rate", stat);
        object.addProperty("payment_differ", sumThisMonth - sumLastMonth);
    }
}
