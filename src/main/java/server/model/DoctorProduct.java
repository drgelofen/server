package server.model;

import server.lib.interfacing.Permission;
import server.lib.model.DatabaseModel;
import server.lib.orm.field.DatabaseField;
import server.lib.orm.persister.ArrayPersister;
import server.lib.orm.table.DatabaseTable;
import server.lib.utils.SchemaUtil;

import java.util.UUID;

@Permission(permissionName = "پیشنهاد دکتر به کاربران")
@DatabaseTable(tableName = SchemaUtil.TABLE_PREFIX + "doctor_products" + SchemaUtil.TABLE_POSTFIX)
public class DoctorProduct extends DatabaseModel<DoctorProduct> {

    public static final String PRODUCTS = "record_products";
    public static final String DOCTOR = "doctor_id";
    public static final String USER = "user_id";

    @DatabaseField(generatedId = true)
    private Long record_id;

    @DatabaseField(persisterClass = ArrayPersister.class)
    private UUID[] record_products;

    @DatabaseField(foreign = true, canBeNull = false)
    private Doctor doctor;

    @DatabaseField(foreign = true, canBeNull = false)
    private User user;

    public Long getRecord_id() {
        return record_id;
    }

    public void setRecord_id(Long record_id) {
        this.record_id = record_id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UUID[] getRecord_products() {
        return record_products;
    }

    public void setRecord_products(UUID[] record_products) {
        this.record_products = record_products;
    }

    public DoctorProduct() {
        super();
    }

    @Override
    public void merge(DoctorProduct info) {
        super.merge(info);
    }

    @Override
    public DoctorProduct trim() {
        if (doctor != null) doctor.trim();
        if (user != null) user.trim();
        return super.trim();
    }
}
