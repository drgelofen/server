package server.controller.payment;

import server.controller.user.ReferController;
import server.gateway.Saman;
import server.lib.interfacing.GET;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.Where;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authenticate;
import server.lib.interfacing.Authorize;
import server.lib.interfacing.BankInterface;
import server.lib.model.FilterModel;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.lib.model.Request;
import server.lib.utils.StringUtil;
import server.lib.utils.TokenUtil;
import server.model.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/payment")
public class PaymentController extends Controller<Payment> {

    @Authorize
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Payment, UUID> dao = getDao(database);
        FilterModel<Payment, UUID> filter = filter(dao, request);
        Where where = filter.where();
        Payment payment = parse(request);
        boolean typeSet = false;
        if (payment != null) {
            if (payment.getUser_id() != null) {
                where.and().eq(Payment.USER_ID, payment.getUser_id());
            }
            if (payment.getSubPackage_id() != null) {
                where.and().eq(Payment.PACKAGE_ID, payment.getSubPackage_id());
            }
            if (payment.getDoctor_id() != null) {
                where.and().eq(Payment.DOCTOR_ID, payment.getDoctor_id());
            }
            if (payment.getPayment_type() != null) {
                where.and().eq(Payment.TYPE, payment.getPayment_type());
            }
            if (payment.getPayment_state() != null) {
                typeSet = true;
                where.and().eq(Payment.STATE, payment.getPayment_state());
            }
            if (payment.getPayment_mode() != null) {
                where.and().eq(Payment.MODE, payment.getPayment_mode());
            }
            if (payment.getPayment_identity() != null) {
                where.and().like(Payment.IDENTITY, "%" + payment.getPayment_identity() + "%");
            }
        }
        if (!typeSet) {
            where.and().eq(Payment.STATE, Payment.STATE_DONE);
        }
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
        Dao<SubPackage, UUID> packages = getDao(database, SubPackage.class);
        Dao<User, UUID> users = getDao(database, User.class);
        List<Payment> query = filter.builder().leftJoin(Payment.USER_ID, User.ID, users.queryBuilder()).query();
        for (Payment pay : query) {
            doctors.refresh(pay.getDoctor());
            packages.refresh(pay.getSubPackage());
        }
        return pass(HttpStatus.OK, query);
    }

    @Authorize
    @Authenticate
    public ResponseEntity getTransactions(Database database, Request request) throws Throwable {
        UserDoctor userDoctor = parse(request, UserDoctor.class);
        if (request.getAuth_type() == TokenUtil.TokenType.USER || (userDoctor != null && userDoctor.getUser_id() != null)) {
            Dao<Payment, UUID> dao = getDao(database);
            FilterModel<Payment, UUID> filter = filter(dao, request);
            Where<Payment, UUID> where = filter.where();
            where.and().and(where.eq(Payment.USER_ID
                    , (userDoctor != null && userDoctor.getUser_id() != null ? userDoctor.getUser_id() : request.getAuth()))
                    , where.eq(Payment.STATE, Payment.STATE_DONE));
            Dao<User, UUID> users = getDao(database, User.class);
            List<Payment> query = filter.builder().leftJoin(Payment.USER_ID, User.ID, users.queryBuilder()).query();
            return pass(HttpStatus.OK, trim(query));
        } else if (request.getAuth_type() == TokenUtil.TokenType.DOCTOR || (userDoctor != null && userDoctor.getDoctor_id() != null)) {
            Dao<DoctorIncome, Long> daoLong = getDaoLong(database, DoctorIncome.class);
            FilterModel<DoctorIncome, Long> filter = filter(daoLong, request);
            List<DoctorIncome> query = filter.where().and().eq(DoctorIncome.DOCTOR
                    , (userDoctor != null && userDoctor.getDoctor_id() != null ? userDoctor.getDoctor_id() : request.getAuth())).query();
            return pass(HttpStatus.OK, trim(query));
        }
        return pass(HttpStatus.BAD_REQUEST);
    }

    @Authenticate
    public ResponseEntity create(Database database, Request request) throws Throwable {
        Payment info = parse(request);
        if (info == null || info.getPayment_type() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        int type = info.getPayment_type();
        switch (type) {
            case Payment.TYPE_CHARGE:
                if (info.getPayment_amount() == null || info.getPayment_amount() == 0) {
                    return pass(HttpStatus.BAD_REQUEST);
                }
                break;
            case Payment.TYPE_BUY_DOCTOR:
                if (info.getSubPackage_id() == null || info.getPayment_detail() == null) {
                    return pass(HttpStatus.BAD_REQUEST);
                }
                Dao<SubPackage, UUID> packages = getDao(database, SubPackage.class);
                SubPackage subPackage = packages.queryForId(info.getSubPackage_id());
                if (subPackage == null) {
                    return pass(HttpStatus.EXPECTATION_FAILED);
                }
                info.setSubPackage(subPackage);
                Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
                info.setDoctor(subPackage.getDoctor());
                doctors.refresh(info.getDoctor());
                break;
        }
        Dao<Payment, UUID> dao = getDao(database);

        Payment record = new Payment();
        record.setUser(request.getAuth_user());
        record.setPayment_code(String.valueOf(dao.countOf()));
        record.setPayment_tag(StringUtil.randomAlphabets(1) + record.getPayment_code() + StringUtil.randomAlphabets(1));
        record.setPayment_identity(request.getAuth_user().getPhone() != null ? request.getAuth_user().getPhone() : request.getAuth_user().getEmail());
        record.setPayment_state(Payment.STATE_INIT);
        record.setPayment_redirect(info.getPayment_redirect());
        record.setPayment_type(type);
        switch (type) {
            case Payment.TYPE_CHARGE:
                record.setPayment_amount(info.getPayment_amount());
                record.setPayment_description("افزایش اعتبار کیف پول درون برنامه ای");
                break;
            case Payment.TYPE_BUY_DOCTOR:
                record.setPayment_detail(info.getPayment_detail());
                record.setDoctor(info.getDoctor());
                record.setSubPackage(info.getSubPackage());
                record.setPayment_amount(Double.valueOf(info.getSubPackage().getPackage_sell_price()));
                record.setPayment_description("خرید پکیج " + info.getSubPackage().getPackage_name() + " مربوط به پزشک " + info.getDoctor().getDoctor_name());
                break;
        }
        if (type != Payment.TYPE_CHARGE && request.getAuth_user().getUser_wallet() >= record.getPayment_amount()) {
            record.setPayment_state(Payment.STATE_DONE);
            record.setPayment_mode(Payment.MODE_CREDIT);
        }
        Payment payment = dao.createIfNotExists(record);
        payment = dao.queryForId(payment.getPayment_id());
        if (payment.getPayment_mode() == Payment.MODE_CREDIT && payment.getPayment_state() == Payment.STATE_DONE) {
            User user = request.getAuth_user();
            user.setUser_wallet((int) (user.getUser_wallet() - payment.getPayment_amount()));
            Dao<User, UUID> daoUser = getDao(database, User.class);
            daoUser.update(user);
            completePurchase(database, payment);
            return pass(HttpStatus.OK);
        }

        if (info.getPayment_gateway() != null) {
            record.setPayment_gateway(info.getPayment_gateway());
        }
        String callBackUrl = request.redirect("/payment/verify?gateway="
                + payment.getPayment_gateway() + "&id=" + payment.getPayment_id().toString());
        BankInterface bank = findBank(record.getPayment_gateway());
        payment = bank.init(payment, StringUtil.certify(callBackUrl));
        if (payment != null) {
            dao.update(payment);
            if (payment.getPayment_url() != null) {
                Payment response = new Payment();
                response.setPayment_url(payment.getPayment_url());
                return pass(HttpStatus.OK, response);
            }
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }

    @GET
    public ResponseEntity verify(Database database, Request request) throws Throwable {
        if (request == null || request.getParameter("gateway") == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        BankInterface bank = findBank(request.getParameter("gateway"));
        Dao<Payment, UUID> dao = getDao(database);
        Payment stock = bank.verify(request, dao);
        if (stock != null) {
            String redirectTag = stock.getPayment_redirect() == null ? "بازگشت به دکتر ژلوفن" : "بازگشت به وب اپلیکیشن";
            if (stock.getPayment_state() == Payment.STATE_DONE) {
                completePurchase(database, stock);
                return passHTML("payment/success.html", stock.getPayment_redirect() == null ? "gelofen://success" : stock.getPayment_redirect(), redirectTag);
            } else {
                return passHTML("payment/failure.html", stock.getPayment_redirect() == null ? "gelofen://failed" : stock.getPayment_redirect(), redirectTag);
            }
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }

    @GET
    public ResponseEntity redirect(Database database, Request request) throws Throwable {
        if (request == null || request.getParameter("id") == null || request.getParameter("gateway") == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Payment, UUID> dao = getDao(database);
        Payment payment = dao.queryForId(StringUtil.getUUID(request.getParameter("id")));
        if (payment == null || payment.getPayment_state() != Payment.STATE_INIT) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        BankInterface bank = findBank(request.getParameter("gateway"));
        return bank.redirect(payment);
    }

    private void completePurchase(Database database, Payment payment) throws Throwable {
        Dao<User, UUID> dao = getDao(database, User.class);
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
        Dao<SubPackage, UUID> packages = getDao(database, SubPackage.class);
        Dao<DoctorIncome, Long> doctorIncomes = getDaoLong(database, DoctorIncome.class);
        packages.refresh(payment.getSubPackage());
        doctors.refresh(payment.getDoctor());
        dao.refresh(payment.getUser());
        User user = payment.getUser();
        if (user != null) {
            user.setUser_purchases(user.getUser_purchases() + 1);
            user.setUser_purchases_amount((long) (user.getUser_purchases_amount() + payment.getPayment_amount()));
            switch (payment.getPayment_type()) {
                case Payment.TYPE_CHARGE:
                    user.setUser_wallet((int) (user.getUser_wallet() + payment.getPayment_amount()));
                    break;
                case Payment.TYPE_BUY_DOCTOR:
                    completePurchaseDoctor(database, payment, user, doctors, packages, doctorIncomes);
                    break;
            }
            dao.update(user);
            ReferController.paymentRefer(payment, database, dao);
        }
    }

    private void completePurchaseDoctor(Database database, Payment payment, User user
            , Dao<Doctor, UUID> doctors, Dao<SubPackage, UUID> packages, Dao<DoctorIncome, Long> doctorIncomes) throws Throwable {
        Dao<UserPackage, Long> userPackages = getDaoLong(database, UserPackage.class);
        UserPackage userPackage = new UserPackage();
        userPackage.setUser(user);
        userPackage.setDoctor(payment.getDoctor());
        userPackage.setSub_package(payment.getSubPackage());
        userPackage.setUser_condition(payment.getPayment_detail());
        userPackages.create(userPackage);

        Dao<UserDoctor, Long> userDoctors = getDaoLong(database, UserDoctor.class);
        Where<UserDoctor, Long> where = userDoctors.queryBuilder().where();
        UserDoctor userDoctor = where.eq(UserDoctor.DOCTOR, payment.getDoctor().getDoctor_id())
                .and().eq(UserDoctor.USER, user.getUser_id()).queryForFirst();

        if (userDoctor == null) {
            payment.getDoctor().setDoctor_patients(payment.getDoctor().getDoctor_patients() + 1);
            userDoctor = new UserDoctor();
            userDoctor.setDoctor(payment.getDoctor());
            userDoctor.setRecord_packages(1L);
            userDoctor.setUser(user);
        } else {
            userDoctor.setRecord_packages(userDoctor.getRecord_packages() + 1);
        }
        userDoctors.createOrUpdate(userDoctor);

        double benefit = (payment.getPayment_amount() * payment.getDoctor().getDoctor_commission()) / 100d;
        payment.getDoctor().setDoctor_wallet((int) (payment.getDoctor().getDoctor_wallet() + benefit));
        payment.getDoctor().setDoctor_income((int) (payment.getDoctor().getDoctor_income() + benefit));
        doctors.update(payment.getDoctor());

        payment.getSubPackage().setPackage_purchase(payment.getSubPackage().getPackage_purchase() + 1);
        packages.update(payment.getSubPackage());

        if (benefit > 0) {
            DoctorIncome paycheck = new DoctorIncome();
            paycheck.setDoctor(payment.getDoctor());
            paycheck.setRecord_type(DoctorIncome.TYPE_PACKAGE);
            paycheck.setRecord_message("خرید بسته مشاوره: " + payment.getSubPackage().getPackage_name());
            paycheck.setUser(user);
            paycheck.setRecord_benefit((float) benefit);
            doctorIncomes.create(paycheck);
        }

        if (payment.getSubPackage().getPackage_score() > 0) {
            user.setUser_score(user.getUser_score() + payment.getSubPackage().getPackage_score());
        }
        if (payment.getSubPackage().getPackage_subscription() > 0) {
            long subscription = user.getUser_subscription();
            long now = Instant.now().toEpochMilli();
            long sub = TimeUnit.DAYS.toMillis(payment.getSubPackage().getPackage_subscription());
            if (subscription >= now) {
                subscription = subscription + sub;
            } else {
                subscription = now + sub;
            }
            user.setUser_subscription(subscription);
        }
    }

    private BankInterface findBank(String payment_gateway) {
        return new Saman();
//        if (!StringUtil.isEmpty(payment_gateway)) {
//            if (payment_gateway.equalsIgnoreCase(Parsian.class.getSimpleName())) {
//                return new Parsian();
//            }
//        }
    }
}
