package server.controller.doctor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authorize;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.Where;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.model.Doctor;
import server.model.DoctorCategory;

import java.util.*;

@RestController
@RequestMapping("/doctorCategory")
public class DoctorCategoryController extends Controller<DoctorCategory> {

    @Authorize
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<DoctorCategory, UUID> dao = getDao(database);
        FilterModel<DoctorCategory, UUID> filter = filter(dao, request);
        Where where = filter.where();
        if (filter.getParent_by() != null) {
            where.and().eq(DoctorCategory.PARENT_ID, filter.getParent_by());
        }
        if (filter.getSearch_by() != null) {
            where.and().like(DoctorCategory.NAME, "%" + filter.getSearch_by() + "%");
        }
        List<DoctorCategory> query = filter.builder().leftJoin(DoctorCategory.PARENT_ID, DoctorCategory.ID, dao.queryBuilder()).query();
        return pass(HttpStatus.OK, query);
    }

    @Authorize
    public ResponseEntity create(Database database, DoctorCategory info) throws Throwable {
        if (info.getParent_id() != null) {
            DoctorCategory parent = new DoctorCategory();
            parent.setCategory_id(info.getParent_id());
            info.setParent(parent);
        }
        Dao<DoctorCategory, UUID> dao = getDao(database);
        DoctorCategory banner = dao.createIfNotExists(info);
        return banner == null ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, dao.queryForId(banner.getCategory_id()));
    }

    @Authorize
    public ResponseEntity delete(Database database, DoctorCategory info) throws Throwable {
        if (info.getCategory_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<DoctorCategory, UUID> dao = getDao(database);
        return dao.delete(info) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
    }

    @Authorize
    public ResponseEntity update(Database database, DoctorCategory info) throws Throwable {
        if (info.getCategory_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        if (info.getParent_id() != null) {
            DoctorCategory parent = new DoctorCategory();
            parent.setCategory_id(info.getParent_id());
            info.setParent(parent);
        }
        Dao<DoctorCategory, UUID> dao = getDao(database);
        DoctorCategory category = dao.queryForId(info.getCategory_id());
        if (category == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        category.merge(info);
        return dao.update(category) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, category);
    }

    public ResponseEntity getList(Database database, Request request) throws Throwable {
        Dao<DoctorCategory, UUID> dao = getDao(database);
        FilterModel<DoctorCategory, UUID> filter = filter(dao, request);
        filter.builder().orderBy(DoctorCategory.PRIORITY, false);
        Where where = filter.where();
        if (filter.getParent_by() != null) {
            where.and().and(where.eq(DoctorCategory.PARENT_ID, filter.getParent_by()), where.eq(DoctorCategory.VISIBILITY, true));
        } else {
            where.and().eq(DoctorCategory.VISIBILITY, true).and().isNull(DoctorCategory.PARENT_ID);
        }
        List<DoctorCategory> query = filter.query();
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
        List<Doctor> allDoctors = doctors.queryForAll();
        for (DoctorCategory category : query) {
            ArrayList<Doctor> list = new ArrayList<>();
            for (Doctor doctor : allDoctors) {
                if (doctor.getVisibility() && doctor.getDoctor_categories() != null) {
                    for (UUID uuid : doctor.getDoctor_categories()) {
                        if (category.getCategory_id().equals(uuid)) {
                            if (list.size() < 9) {
                                list.add(doctor);
                            }
                            break;
                        }
                    }
                }
            }
//            Collections.sort(list, new Comparator<Doctor>() {
//                @Override
//                public int compare(Doctor o2, Doctor o1) {
//                    return Double.compare(o1.getPriority(), o2.getPriority());
//                }
//            });
            Collections.shuffle(list);
            category.setDoctors(list);
        }
        return pass(HttpStatus.OK, trim(query));
    }

    public ResponseEntity getOne(Database database, DoctorCategory info) throws Throwable {
        if (info.getCategory_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<DoctorCategory, UUID> dao = getDao(database);
        DoctorCategory category = dao.queryForId(info.getCategory_id());
        if (category != null) {
            category.setCategory_views(category.getCategory_views() + 1);
            dao.update(category);
            return pass(HttpStatus.OK, category);
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }
}
