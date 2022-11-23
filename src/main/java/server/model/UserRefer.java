package server.model;

import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "اشتراک گذاری کاربران")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "user_refers" + SchemaUtil.TABLE_POSTFIX)
public class UserRefer extends DatabaseModel<UserRefer> {

    public static final String PURCHASE_WALLET_PERCENT = "purchase_wallet_percent";
    public static final String PURCHASE_SCORE_PERCENT = "purchase_score_percent";
    public static final String PURCHASE_WALLET = "purchase_wallet";
    public static final String PURCHASE_SCORE = "purchase_score";
    public static final String INVITE_WALLET = "invite_wallet";
    public static final String INVITE_SCORE = "invite_score";

    public static final int STATE_INIT = 0;
    public static final int STATE_VERIFY = 1;
    public static final int STATE_JOIN = 2;
    public static final int STATE_PURCHASED = 3;

    public static final String INVITEE = "invitee_identity";
    public static final String RECORD_STATE = "record_state";
    public static final String INVITER = "inviter_identity";
    public static final String INVITEE_ID = "invitee_id";

    public static final String DUPLICATE_MESSAGE = "شما قبلا ثبت نام کرده اید، لطفا شماره جدیدی را وارد نمایید.";
    public static final String EXIST_MESSAGE = "کاربر معرف شما معتبر نیست.";

    @DatabaseField(generatedId = true)
    private Long record_id;

    @DatabaseField(foreign = true, canBeNull = false)
    private User inviter;

    @DatabaseField(foreign = true)
    private User invitee;

    @DatabaseField
    private String invitee_identity;

    @DatabaseField
    private String inviter_identity;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer record_state;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer record_score;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer record_attempts;

    @DatabaseField()
    private String captcha;

    public Integer getRecord_attempts() {
        return record_attempts;
    }

    public void setRecord_attempts(Integer record_attempts) {
        this.record_attempts = record_attempts;
    }

    private UUID inviter_id;

    private UUID invitee_id;

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public Long getRecord_id() {
        return record_id;
    }

    public void setRecord_id(Long record_id) {
        this.record_id = record_id;
    }

    public User getInviter() {
        return inviter;
    }

    public void setInviter(User inviter) {
        this.inviter = inviter;
    }

    public String getInviter_identity() {
        return inviter_identity;
    }

    public void setInviter_identity(String inviter_identity) {
        this.inviter_identity = inviter_identity;
    }

    public String getInvitee_identity() {
        return invitee_identity;
    }

    public void setInvitee_identity(String invitee_identity) {
        this.invitee_identity = invitee_identity;
    }

    public Integer getRecord_state() {
        return record_state;
    }

    public void setRecord_state(Integer record_state) {
        this.record_state = record_state;
    }

    public Integer getRecord_score() {
        return record_score;
    }

    public void setRecord_score(Integer record_score) {
        this.record_score = record_score;
    }

    public UUID getInviter_id() {
        return inviter_id;
    }

    public void setInviter_id(UUID inviter_id) {
        this.inviter_id = inviter_id;
    }

    public User getInvitee() {
        return invitee;
    }

    public void setInvitee(User invitee) {
        this.invitee = invitee;
    }

    public UUID getInvitee_id() {
        return invitee_id;
    }

    public void setInvitee_id(UUID invitee_id) {
        this.invitee_id = invitee_id;
    }

    public UserRefer() {
        super();
    }

    @Override
    public void merge(UserRefer model) {
        super.merge(model);
    }
}
