package server.controller.blog;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authorize;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.QueryBuilder;
import server.lib.orm.stmt.Where;
import server.lib.utils.*;
import server.model.Blog;
import server.model.BlogCategory;
import server.model.Doctor;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/blog")
public class BlogController extends Controller<Blog> {

    @Authorize
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Blog, UUID> dao = getDao(database);
        FilterModel<Blog, UUID> filter = filter(dao, request);
        Where<Blog, UUID> where = filter.where();
        if (filter.getParent_by() != null) {
            where.and().eq(Blog.CATEGORY_ID, filter.getParent_by());
        }
        if (filter.getSearch_by() != null) {
            where.and().like(Blog.TITLE, "%" + filter.getSearch_by() + "%");
        }
        Dao<BlogCategory, UUID> categories = getDao(database, BlogCategory.class);
        List<Blog> query = filter.builder().leftJoin(Blog.CATEGORY_ID, BlogCategory.ID, categories.queryBuilder()).query();
        Dao<Doctor, UUID> doctors = getDao(database, Doctor.class);
        for (Blog blog : query) {
            doctors.refresh(blog.getDoctor());
        }
        return pass(HttpStatus.OK, query);
    }

    @Authorize
    public ResponseEntity create(Database database, Blog info) throws Throwable {
        if (info.getCategory_id() != null) {
            BlogCategory parent = new BlogCategory();
            parent.setCategory_id(info.getCategory_id());
            info.setCategory(parent);
        }
        if (info.getBlog_content() != null) {
            BlogCategory parent = new BlogCategory();
            parent.setCategory_id(info.getCategory_id());
            info.setCategory(parent);
        }
        if (info.getDoctor_id() != null) {
            Doctor parent = new Doctor();
            parent.setDoctor_id(info.getDoctor_id());
            info.setDoctor(parent);
        }
        Dao<Blog, UUID> dao = getDao(database);
        Blog banner = dao.createIfNotExists(info);
        return banner == null ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, dao.queryForId(banner.getBlog_id()));
    }

    @Authorize
    public ResponseEntity update(Database database, Blog info) throws Throwable {
        if (info.getBlog_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        if (info.getCategory_id() != null) {
            BlogCategory parent = new BlogCategory();
            parent.setCategory_id(info.getCategory_id());
            info.setCategory(parent);
        }
        if (info.getDoctor_id() != null) {
            Doctor parent = new Doctor();
            parent.setDoctor_id(info.getDoctor_id());
            info.setDoctor(parent);
        }
        Dao<Blog, UUID> dao = getDao(database);
        Blog blog = dao.queryForId(info.getBlog_id());
        if (blog == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        blog.merge(info);
        return dao.update(blog) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, blog);
    }

    @Authorize
    public ResponseEntity delete(Database database, Blog info) throws Throwable {
        if (info.getBlog_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Blog, UUID> dao = getDao(database);
        return dao.delete(info) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
    }

    public ResponseEntity getList(Database database, Request request) throws Throwable {
        Dao<Blog, UUID> dao = getDao(database);
        FilterModel<Blog, UUID> filter = filter(dao, request);
        QueryBuilder<Blog, UUID> builder = filter.builder();
        if (filter.getOrder_by() == null) {
            builder.orderBy(Blog.PRIORITY, false);
        }
        Where<Blog, UUID> where = filter.where();
        where.and().eq(Blog.VISIBILITY, true);
        if (filter.getParent_by() != null) {
            where.and().eq(Blog.CATEGORY_ID, filter.getParent_by());
        }
        if (filter.getSearch_by() != null) {
            where.and().like(Blog.TITLE, "%" + filter.getSearch_by() + "%");
        }
        if (filter.getFilter_by() != null) {
            where.and().eq(Blog.DOCTOR, StringUtil.getUUID(filter.getFilter_by()));
            builder.limit(100L);
        }
        return pass(HttpStatus.OK, trim(filter.query()));
    }

    public ResponseEntity getOne(Database database, Request request) throws Throwable {
        Blog info = parse(request);
        if (info.getBlog_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<Blog, UUID> dao = getDao(database);
        Blog blog = dao.queryForId(info.getBlog_id());
        if (blog != null) {
            blog.setBlog_views(blog.getBlog_views() + 1);
            dao.update(blog);
            blog.setCreate_at_str(DateUtil.toJalali(blog.getCreate_at()));
            return pass(HttpStatus.OK, request.toFilter().getFilter_by() != null ? blog.trim() : blog);
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }
}
