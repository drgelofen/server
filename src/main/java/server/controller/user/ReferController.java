package server.controller.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.Application;
import server.controller.setting.SettingController;
import server.lib.center.MailCenter;
import server.lib.center.OTPCenter;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.Where;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.lib.utils.StringUtil;
import server.model.*;

import java.util.UUID;

@RestController
@RequestMapping("/refer")
public class ReferController extends Controller<User> {

    public ResponseEntity getUsername(Database database, UserRefer refer) throws Throwable {
        if (refer == null || refer.getInviter_identity() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        refer.setInviter_identity(StringUtil.format(refer.getInviter_identity()));
        Dao<User, UUID> dao = getDao(database);
        User user = dao.queryBuilder().where().eq(User.SHARE_TOKEN, refer.getInviter_identity()).queryForFirst();
        return pass(HttpStatus.OK, user.getUsername());
    }

    public ResponseEntity init(Database database, UserRefer info) throws Throwable {
        if (info == null || info.getInviter_identity() == null || info.getInvitee_identity() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        info.setInviter_identity(StringUtil.format(info.getInviter_identity()));
        info.setInvitee_identity(StringUtil.format(info.getInvitee_identity()));
        Dao<User, UUID> dao = getDao(database);
        long countOf;
        if (StringUtil.isEmail(info.getInvitee_identity())) {
            countOf = dao.queryBuilder().where().eq(User.EMAIL, info.getInvitee_identity()).countOf();
        } else {
            countOf = dao.queryBuilder().where().eq(User.PHONE, info.getInvitee_identity()).countOf();
        }
        if (countOf > 0) {
            return pass(UserRefer.DUPLICATE_MESSAGE, HttpStatus.EXPECTATION_FAILED);
        }
        User inviter = dao.queryBuilder().where().eq(User.SHARE_TOKEN, info.getInviter_identity()).queryForFirst();
        if (inviter == null) {
            return pass(UserRefer.EXIST_MESSAGE, HttpStatus.EXPECTATION_FAILED);
        }

        Dao<UserRefer, UUID> userShares = getDao(database, UserRefer.class);
        Where<UserRefer, UUID> where = userShares.queryBuilder().where();
        UserRefer target = where.and(where.eq(UserRefer.INVITER, inviter.getUser_share_id())
                , where.eq(UserRefer.INVITEE, info.getInvitee_identity())).queryForFirst();
        if (target != null && target.getRecord_state() != UserRefer.STATE_INIT) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }

        String verifyCode = StringUtil.random(UserController.VERIFY_CODE_LENGTH);
        if (target == null) {
            target = new UserRefer();
            target.setInviter(inviter);
            target.setRecord_state(UserRefer.STATE_INIT);
            target.setInviter_identity(inviter.getUser_share_id());
            target.setRecord_score(0);
            target.setInvitee_identity(info.getInvitee_identity());
            target.setCaptcha(verifyCode);
            target = userShares.createIfNotExists(target);
        } else {
            target.setCaptcha(verifyCode);
            userShares.update(target);
        }
        if (StringUtil.isEmail(target.getInvitee_identity())) {
            MailCenter.send(target.getInvitee_identity(), verifyCode);
        } else {
            OTPCenter.send(target.getInvitee_identity(), verifyCode);
        }
        return pass(HttpStatus.OK, Application.ENABLE_OTP ? target.getRecord_id() : target);
    }

    public ResponseEntity verify(Database database, UserRefer info) throws Throwable {
        if (info == null || info.getCaptcha() == null || info.getRecord_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        info.setCaptcha(StringUtil.format(info.getCaptcha()));
        Dao<UserRefer, Long> dao = getDaoLong(database, UserRefer.class);
        UserRefer userShare = dao.queryForId(info.getRecord_id());
        if (userShare != null && userShare.getRecord_state() == UserRefer.STATE_INIT) {
            if (userShare.getCaptcha() == null) {
                return pass(UserTemp.CAPTCHA_MESSAGE, HttpStatus.EXPECTATION_FAILED);
            }
            if (userShare.getCaptcha().equals(info.getCaptcha())) {
                userShare.setRecord_state(UserRefer.STATE_VERIFY);
                return dao.update(userShare) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
            }
            userShare.setRecord_attempts(userShare.getRecord_attempts() + 1);
            if (userShare.getRecord_attempts() > UserController.MAXIMUM_WRONG_CAPTCHA) {
                userShare.setRecord_attempts(0);
                userShare.setCaptcha(null);
            }
            dao.update(userShare);
            return pass(User.CAPTCHA_MESSAGE, HttpStatus.EXPECTATION_FAILED);
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }

    public static void inviteRefer(User invitee, Database database, Dao<User, UUID> dao) throws Throwable {
        Dao<UserRefer, Long> userShares = getDaoLongStatic(database, UserRefer.class);
        Where<UserRefer, Long> where = userShares.queryBuilder().where();
        UserRefer userShare;
        if (!StringUtil.isEmpty(invitee.getPhone())) {
            userShare = where.and(where.eq(UserRefer.INVITEE, invitee.getPhone())
                    , where.eq(UserRefer.RECORD_STATE, UserRefer.STATE_VERIFY)).queryForFirst();
        } else {
            userShare = where.and(where.eq(UserRefer.INVITEE, invitee.getEmail())
                    , where.eq(UserRefer.RECORD_STATE, UserRefer.STATE_VERIFY)).queryForFirst();
        }
        if (userShare == null) {
            return;
        }
        User user = dao.queryForId(userShare.getInviter().getUser_id());
        if (user != null && !user.getLimit()) {
            userShare.setRecord_state(UserRefer.STATE_JOIN);
            userShare.setInvitee(invitee);
            userShares.update(userShare);

            Setting setting = SettingController.get(database);
            user.setUser_invites(user.getUser_invites() + 1);

            user.setUser_invite_wallet(user.getUser_invite_wallet() + setting.getFieldAsInt(UserRefer.INVITE_WALLET));
            user.setUser_wallet(user.getUser_wallet() + setting.getFieldAsInt(UserRefer.INVITE_WALLET));

            user.setUser_invite_score(user.getUser_invite_score() + setting.getFieldAsInt(UserRefer.INVITE_SCORE));
            user.setUser_score(user.getUser_score() + setting.getFieldAsInt(UserRefer.INVITE_SCORE));
            dao.update(user);
        }
    }

    public static void paymentRefer(Payment payment, Database database, Dao<User, UUID> daoUser) throws Throwable {
        Dao<UserRefer, Long> daoShare = getDaoLongStatic(database, UserRefer.class);
        Where<UserRefer, Long> shareWhere = daoShare.queryBuilder().where();
        UserRefer share = shareWhere.eq(UserRefer.INVITEE_ID, payment.getUser().getUser_id()).queryForFirst();
        if (share == null || share.getRecord_state() < UserRefer.STATE_JOIN) {
            return;
        }
        User user = daoUser.queryForId(share.getInviter().getUser_id());

        Setting setting = SettingController.get(database);
        if (user != null && !user.getLimit()) {
            if (share.getRecord_state() == UserRefer.STATE_JOIN) {
                share.setRecord_state(UserRefer.STATE_PURCHASED);
                daoShare.update(share);

                user.setUser_purchased_invites(user.getUser_purchased_invites() + 1);

                user.setUser_invite_score(user.getUser_invite_score() + setting.getFieldAsInt(UserRefer.PURCHASE_SCORE));
                user.setUser_score(user.getUser_score() + setting.getFieldAsInt(UserRefer.PURCHASE_SCORE));

                user.setUser_invite_wallet(user.getUser_invite_wallet() + setting.getFieldAsInt(UserRefer.PURCHASE_WALLET));
                user.setUser_wallet(user.getUser_wallet() + setting.getFieldAsInt(UserRefer.PURCHASE_WALLET));
            }

            int score = (int) ((payment.getPayment_amount() * setting.getFieldAsInt(UserRefer.PURCHASE_SCORE_PERCENT)) / 100);
            int wallet = (int) ((payment.getPayment_amount() * setting.getFieldAsInt(UserRefer.PURCHASE_WALLET_PERCENT)) / 100);

            user.setUser_invite_score(user.getUser_invite_score() + score);
            user.setUser_invite_wallet(user.getUser_invite_wallet() + wallet);

            user.setUser_score(user.getUser_score() + score);
            user.setUser_wallet(user.getUser_wallet() + wallet);

            daoUser.update(user);
        }
    }
}
