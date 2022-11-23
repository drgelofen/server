package server.model;

import com.google.gson.JsonElement;
import server.lib.orm.field.DataType;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.ArrayPersister;
import server.lib.orm.persister.JsonPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.utils.SchemaUtil;

import java.util.ArrayList;
import java.util.UUID;

@Permission(permissionName = "مدیریت مالی", permissionBrief = "در این قسمت گزارشات مربوط به تراکنش های مالی را مشاهده کنید", permissionPriority = 250)
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "payments" + SchemaUtil.TABLE_POSTFIX)
public class Payment extends DatabaseModel<Payment> {

    public final static int STATE_INIT = 0;
    public final static int STATE_CANCEL = 1;
    public final static int STATE_DONE = 2;

    public final static int TYPE_CHARGE = 0;
    public final static int TYPE_BUY_DOCTOR = 1;

    public static final String PACKAGE_ID = "package_id";
    public static final String DOCTOR_ID = "doctor_id";
    public static final String USER_ID = "user_id";

    public static final String IDENTITY = "payment_identity";
    public static final String REF = "payment_ref_code";
    public static final String STATE = "payment_state";
    public static final String TOKEN = "payment_token";
    public static final String CODE = "payment_code";
    public static final String TYPE = "payment_type";
    public static final String MODE = "payment_mode";
    public static final String ID = "payment_id";

    public static final int MODE_GATEWAY = 0;
    public static final int MODE_CREDIT = 1;

    @DatabaseField(generatedId = true)
    private UUID payment_id;

    @DatabaseField(unique = true, canBeNull = false)
    private String payment_code;

    @DatabaseField()
    private String payment_redirect;

    @DatabaseField()
    private String payment_merchant;

    @DatabaseField()
    private String payment_trace;

    @DatabaseField()
    private String payment_tag;

    @DatabaseField()
    private String payment_token;

    @DatabaseField()
    private String payment_ref_code;

    @DatabaseField()
    private String payment_terminal;

    @DatabaseField()
    private String payment_result_code;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String payment_description;

    @DatabaseField(canBeNull = false)
    private String payment_identity;

    @DatabaseField()
    private String payment_url;

    @DatabaseField()
    private String payment_card;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer payment_state;

    @DatabaseField(canBeNull = false)
    private Integer payment_type;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer payment_mode;

    @DatabaseField(canBeNull = false, defaultValue = "ZarinPal")
    private String payment_gateway;

    @DatabaseField(canBeNull = false)
    private Double payment_amount;

    @DatabaseField(persisterClass = JsonPersister.class)
    private JsonElement payment_detail;

    @DatabaseField(foreign = true, canBeNull = false)
    private User user;

    @DatabaseField(foreign = true)
    private Doctor doctor;

    @DatabaseField(foreign = true)
    private SubPackage subPackage;

    private UUID subPackage_id, user_id, doctor_id;

    public String getPayment_tag() {
        return payment_tag;
    }

    public void setPayment_tag(String payment_tag) {
        this.payment_tag = payment_tag;
    }

    public Integer getPayment_mode() {
        return payment_mode;
    }

    public void setPayment_mode(Integer payment_mode) {
        this.payment_mode = payment_mode;
    }

    public String getPayment_code() {
        return payment_code;
    }

    public void setPayment_code(String payment_code) {
        this.payment_code = payment_code;
    }

    public UUID getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(UUID payment_id) {
        this.payment_id = payment_id;
    }

    public String getPayment_token() {
        return payment_token;
    }

    public void setPayment_token(String payment_token) {
        this.payment_token = payment_token;
    }

    public String getPayment_ref_code() {
        return payment_ref_code;
    }

    public void setPayment_ref_code(String payment_ref_code) {
        this.payment_ref_code = payment_ref_code;
    }

    public String getPayment_terminal() {
        return payment_terminal;
    }

    public void setPayment_terminal(String payment_terminal) {
        this.payment_terminal = payment_terminal;
    }

    public String getPayment_result_code() {
        return payment_result_code;
    }

    public void setPayment_result_code(String payment_result_code) {
        this.payment_result_code = payment_result_code;
    }

    public String getPayment_description() {
        return payment_description;
    }

    public void setPayment_description(String payment_description) {
        this.payment_description = payment_description;
    }

    public String getPayment_identity() {
        return payment_identity;
    }

    public void setPayment_identity(String payment_identity) {
        this.payment_identity = payment_identity;
    }

    public String getPayment_url() {
        return payment_url;
    }

    public void setPayment_url(String payment_url) {
        this.payment_url = payment_url;
    }

    public String getPayment_card() {
        return payment_card;
    }

    public void setPayment_card(String payment_card) {
        this.payment_card = payment_card;
    }

    public Integer getPayment_state() {
        return payment_state;
    }

    public void setPayment_state(Integer payment_state) {
        this.payment_state = payment_state;
    }

    public Integer getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(Integer payment_type) {
        this.payment_type = payment_type;
    }

    public String getPayment_gateway() {
        return payment_gateway;
    }

    public void setPayment_gateway(String payment_gateway) {
        this.payment_gateway = payment_gateway;
    }

    public Double getPayment_amount() {
        return payment_amount;
    }

    public void setPayment_amount(Double payment_amount) {
        this.payment_amount = payment_amount;
    }

    public JsonElement getPayment_detail() {
        return payment_detail;
    }

    public void setPayment_detail(JsonElement payment_detail) {
        this.payment_detail = payment_detail;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public SubPackage getSubPackage() {
        return subPackage;
    }

    public void setSubPackage(SubPackage subPackage) {
        this.subPackage = subPackage;
    }

    public UUID getSubPackage_id() {
        return subPackage_id;
    }

    public void setSubPackage_id(UUID subPackage_id) {
        this.subPackage_id = subPackage_id;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public UUID getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(UUID doctor_id) {
        this.doctor_id = doctor_id;
    }

    public String getPayment_redirect() {
        return payment_redirect;
    }

    public void setPayment_redirect(String payment_redirect) {
        this.payment_redirect = payment_redirect;
    }

    public String getPayment_merchant() {
        return payment_merchant;
    }

    public void setPayment_merchant(String payment_merchant) {
        this.payment_merchant = payment_merchant;
    }

    public String getPayment_trace() {
        return payment_trace;
    }

    public void setPayment_trace(String payment_trace) {
        this.payment_trace = payment_trace;
    }

    public Payment() {
        super();
    }

    @Override
    public void merge(Payment model) {
        super.merge(model);
        payment_state = model.payment_state;
        payment_token = model.payment_token;
        payment_ref_code = model.payment_ref_code;
        payment_terminal = model.payment_terminal;
        payment_result_code = model.payment_result_code;
        payment_card = model.payment_card;
        payment_url = model.payment_url;
    }

    @Override
    public Payment trim() {
        if (subPackage != null) subPackage.trim();
        if (doctor != null) doctor.trim();
        if (user != null) user.trim();
        payment_token = null;
        payment_ref_code = null;
        payment_terminal = null;
        payment_result_code = null;
        payment_state = null;
        return super.trim();
    }
}
