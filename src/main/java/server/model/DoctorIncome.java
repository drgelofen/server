package server.model;

import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

@Permission(permissionName = "درآمد پزشکان از محصولات")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "doctor_incomes" + SchemaUtil.TABLE_POSTFIX)
public class DoctorIncome extends DatabaseModel<DoctorIncome> {

    public static final String PRODUCT = "product_id";
    public static final String DOCTOR = "doctor_id";
    public static final String USER = "user_id";

    public static final int TYPE_PRODUCT = 1;
    public static final int TYPE_PACKAGE = 0;

    @DatabaseField(generatedId = true)
    private Long record_id;

    @DatabaseField(foreign = true, canBeNull = false)
    private Doctor doctor;

    @DatabaseField(foreign = true, canBeNull = false)
    private User user;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Float record_benefit;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer record_quantity;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private Integer record_type;

    @DatabaseField
    private String record_message;

    public Integer getRecord_type() {
        return record_type;
    }

    public void setRecord_type(Integer record_type) {
        this.record_type = record_type;
    }

    public Float getRecord_benefit() {
        return record_benefit;
    }

    public void setRecord_benefit(Float record_benefit) {
        this.record_benefit = record_benefit;
    }

    public Integer getRecord_quantity() {
        return record_quantity;
    }

    public void setRecord_quantity(Integer record_quantity) {
        this.record_quantity = record_quantity;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Long getRecord_id() {
        return record_id;
    }

    public void setRecord_id(Long record_id) {
        this.record_id = record_id;
    }

    public String getRecord_message() {
        return record_message;
    }

    public void setRecord_message(String record_message) {
        this.record_message = record_message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DoctorIncome() {
        super();
    }

    @Override
    public void merge(DoctorIncome model) {
        super.merge(model);
    }

    @Override
    public DoctorIncome trim() {
        if (doctor != null) doctor.trim();
        if (user != null) user.trim();
        return super.trim();
    }
}
